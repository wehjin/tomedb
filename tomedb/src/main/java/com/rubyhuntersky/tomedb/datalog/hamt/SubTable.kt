package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.basics.longFromBytes
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

interface SubTableReader {
    fun readSlot(position: Long): Slot2
    fun readSlots(slotMap: SlotMap, base: Long): ArrayList<Slot2>
}

class SubTableReadWrite(private val file: File) {

    private var end = file.length()
    private val fileOutputStream = FileOutputStream(file)

    val reader = object : SubTableReader {
        private val randomAccessFile = RandomAccessFile(file, "r")

        override fun readSlot(position: Long): Slot2 {
            randomAccessFile.seek(position)
            return readSlot()
        }

        private fun readSlot(): Slot2 {
            val bytes = ByteArray(Long.SIZE_BYTES)
            randomAccessFile.read(bytes)
            val mapOrKey = longFromBytes(bytes)
            return if (SlotMap.isMap(mapOrKey)) {
                randomAccessFile.read(bytes)
                val base = longFromBytes(bytes)
                Slot2.HardLink(SlotMap(mapOrKey), base, this)
            } else {
                randomAccessFile.read(bytes)
                val value = longFromBytes(bytes)
                Slot2.KeyValue(mapOrKey, value)
            }
        }

        override fun readSlots(slotMap: SlotMap, base: Long): ArrayList<Slot2> {
            randomAccessFile.seek(base)
            return (0 until SubTable.slotCount).fold(
                initial = SubTable.emptySlots(),
                operation = { slots, index ->
                    if (slotMap.isSlotPresent(index)) {
                        slots[index] = readSlot()
                    }
                    slots
                }
            )
        }
    }

    fun writeSlots(slots: List<Slot2>): Pair<SlotMap, Long> {
        require(slots.size == SubTable.slotCount)
        val base = end
        val slotMap = slots.foldIndexed(
            initial = SlotMap.empty(),
            operation = { index, slotMap, slot ->
                when (slot) {
                    Slot2.Empty -> slotMap
                    is Slot2.KeyValue -> {
                        writeBytes(bytesFromLong(slot.key) + bytesFromLong(slot.value))
                        slotMap.setIndex(index)
                    }
                    is Slot2.HardLink -> {
                        writeBytes(slot.slotMap.toBytes() + bytesFromLong(slot.base))
                        slotMap.setIndex(index)
                    }
                    is Slot2.SoftLink -> error("SoftLinks are un-writable.")
                }
            }
        )
        return Pair(slotMap, base)
    }

    private fun writeBytes(byteArray: ByteArray): Long {
        val start = end
        if (byteArray.isNotEmpty()) {
            fileOutputStream.write(byteArray)
            end += byteArray.size
        }
        return start
    }

    companion object {
        const val slotBytes = Long.SIZE_BYTES * 2
    }
}

sealed class SubTable(val depth: Int) {

    fun getValue(key: Long) = getValue(Hamt, key)
    fun getValue(keyBreaker: KeyBreaker, key: Long): Long? {
        require(isKey(key))
        val index = keyBreaker.slotIndex(key, depth)
        return when (val slot = getSlot(index)) {
            is Slot2.Empty -> null
            is Slot2.KeyValue -> if (key == slot.key) slot.value else null
            is Slot2.SoftLink, is Slot2.HardLink -> {
                slot.asSubTable(depth + 1).getValue(keyBreaker, key)
            }
        }
    }

    fun setValue(key: Long, value: Long) = setValue(Hamt, key, value)
    fun setValue(keyBreaker: KeyBreaker, key: Long, value: Long): SubTable {
        require(isKey(key))
        val index = keyBreaker.slotIndex(key, depth)
        return when (val slot = getSlot(index)) {
            Slot2.Empty -> setSlot(index, Slot2.KeyValue(key, value))
            is Slot2.KeyValue -> {
                if (key == slot.key) {
                    if (value == slot.value) {
                        this@SubTable
                    } else {
                        setSlot(index, Slot2.KeyValue(key, value))
                    }
                } else {
                    val keyValues = mapOf(slot.key to slot.value, key to value)
                    val subTable = keyValues.entries.fold(
                        initial = emptySubTable(depth + 1) as SubTable,
                        operation = { subTable, (key, value) ->
                            subTable.setValue(keyBreaker, key, value)
                        }
                    ) as Soft
                    setSlot(index, subTable.asSoftLink())
                }
            }
            is Slot2.SoftLink, is Slot2.HardLink -> {
                val oldSub = slot.asSubTable(depth + 1)
                when (val newSub = oldSub.setValue(keyBreaker, key, value)) {
                    is Soft -> setSlot(index, newSub.asSoftLink())
                    is Hard -> this
                }
            }
        }
    }

    abstract fun getSlot(index: Int): Slot2
    abstract fun setSlot(index: Int, slot: Slot2): Soft
    abstract fun harden(readWrite: SubTableReadWrite): Hard

    class Soft(depth: Int, private val slots: ArrayList<Slot2>) : SubTable(depth) {

        override fun getSlot(index: Int): Slot2 = slots[index]

        override fun setSlot(index: Int, slot: Slot2) = Soft(
            depth = depth,
            slots = ArrayList<Slot2>(slots.size).also {
                it.addAll(slots)
                it[index] = slot
            }
        )

        override fun harden(readWrite: SubTableReadWrite): Hard {
            val hardSlots = slots.map {
                when (it) {
                    Slot2.Empty -> it
                    is Slot2.KeyValue -> it
                    is Slot2.HardLink -> it
                    is Slot2.SoftLink -> it.subTable.harden(readWrite).asHardLink()
                }
            }
            val (slotMap, base) = readWrite.writeSlots(hardSlots)
            return Hard(depth, slotMap, base, readWrite.reader)
        }

        fun asSoftLink() = Slot2.SoftLink(this)
    }

    class Hard(
        depth: Int,
        private val slotMap: SlotMap,
        private val base: Long,
        private val reader: SubTableReader
    ) : SubTable(depth) {

        override fun getSlot(index: Int): Slot2 {
            return slotMap.getOffsetToSlot(index)?.let { reader.readSlot(base + it) } ?: Slot2.Empty
        }

        override fun setSlot(index: Int, slot: Slot2): Soft {
            return Soft(depth, reader.readSlots(slotMap, base)).setSlot(index, slot)
        }

        override fun harden(readWrite: SubTableReadWrite): Hard = this

        fun asHardLink() = Slot2.HardLink(slotMap, base, reader)
    }

    companion object {

        fun new() = emptySubTable(0)
        fun emptySubTable(depth: Int) = Soft(depth, emptySlots())
        fun isKey(candidate: Long) = !SlotMap.isMap(candidate)

        fun emptySlots() =
            arrayListOf<Slot2>().apply { addAll((0 until slotCount).map { Slot2.Empty }) }

        const val slotCount = 32

    }
}

