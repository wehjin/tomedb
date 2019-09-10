package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

class HamtTable private constructor(val bytes: ByteArray, val map: Long) {
    init {
        require(isMap(map))
    }

    private val slots by lazy {
        Array<Slot>(slotCount) { Slot.Empty }
            .also { slots ->
                var slotBase = 0
                val buffer = ByteBuffer.allocate(slotBytes).order(ByteOrder.BIG_ENDIAN)
                (0 until slotCount).forEach { slotIndex ->
                    val slotIsPresent = ((map shr slotIndex) and 0x1L) == 1L
                    if (slotIsPresent) {
                        buffer.rewind()
                        buffer.put(bytes, slotBase, slotBytes)
                        slotBase += slotBytes
                        val firstLong = buffer.getLong(0)
                        val secondLong = buffer.getLong(Long.SIZE_BYTES)
                        slots[slotIndex] = if (firstLong < 0) {
                            Slot.MapBase(firstLong, secondLong)
                        } else {
                            Slot.KeyValue(firstLong, secondLong)
                        }
                    }
                }
            }
    }


    fun getSlot(index: Byte): Slot = slots[index.toInt()]

    fun toRootBytes(): ByteArray = Hamt.bytesFromLong(map) + bytes

    fun fillSlotWithKeyValue(index: Byte, key: Long, value: Long): HamtTable {
        val newSlots = slots.copyOf().also { it[index.toInt()] = Slot.KeyValue(key, value) }
        return createTable(newSlots)
    }

    fun fillSlotWithMapBase(index: Byte, map: Long, base: Long): HamtTable {
        require(isMap(map))
        val newSlots = slots.copyOf().also { it[index.toInt()] = Slot.KeyValue(map, base) }
        return createTable(newSlots)
    }

    sealed class Slot {

        object Empty : Slot()

        data class KeyValue(val key: Long, val value: Long) : Slot()

        data class MapBase(val map: Long, val base: Long) : Slot() {
            fun toSubTable(frameReader: FrameReader): HamtTable {
                return HamtTable(frameReader.read(base), map)
            }
        }
    }

    companion object {

        fun fromRootBytes(rootBytes: ByteArray): HamtTable {
            val map = Hamt.longFromBytes(rootBytes)
            val bytes = rootBytes.sliceArray(Long.SIZE_BYTES..rootBytes.size)
            return HamtTable(bytes, map)
        }

        fun createWithKeyValue(index: Byte, key: Long, value: Long): HamtTable {
            return createEmpty().fillSlotWithKeyValue(index, key, value)
        }

        private fun createEmpty() = HamtTable(ByteArray(0), mapBit)

        fun createWithKeyValues(initValues: Set<Triple<Byte, Long, Long>>): HamtTable {
            return initValues.fold(
                initial = createEmpty(),
                operation = { table, (index, key, value) ->
                    table.fillSlotWithKeyValue(index, key, value)
                }
            )
        }

        fun createSubWithMapBase(index: Byte, map: Long, base: Long): HamtTable {
            return createEmpty().fillSlotWithMapBase(index, map, base)
        }

        private fun createTable(newSlots: Array<Slot>): HamtTable {
            val buffer = ByteBuffer.allocate(slotCount * slotBytes).order(ByteOrder.BIG_ENDIAN)
            val (newMap, byteCount) = newSlots.foldIndexed(
                initial = Pair(mapBit, 0),
                operation = { slotIndex, (map, byteCount), slot ->
                    val slotBit = 1L shl slotIndex
                    when (slot) {
                        Slot.Empty -> Pair(map and slotBit.inv(), byteCount)
                        is Slot.KeyValue -> {
                            buffer.putLong(slot.key)
                            buffer.putLong(slot.value)
                            Pair(map or slotBit, byteCount + slotBytes)
                        }
                        is Slot.MapBase -> {
                            buffer.putLong(slot.map)
                            buffer.putLong(slot.base)
                            Pair(map or slotBit, byteCount + slotBytes)
                        }
                    }
                }
            )
            val newBytes = ByteArray(byteCount).also { buffer.get(it, 0, byteCount) }
            return HamtTable(newBytes, newMap)
        }

        private fun isMap(maybe: Long) = (maybe and mapBit) == mapBit

        private const val slotCount = 32
        private const val slotBytes = Long.SIZE_BYTES * 2
        private const val mapBit = 1L shl (Long.SIZE_BITS - 1)
    }
}