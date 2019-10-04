package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.basics.longFromBytes
import java.io.File
import java.io.RandomAccessFile

interface SubTableReader {
    fun readSlot(position: Long): Slot2
    fun readSlots(slotMap: SlotMap, base: Long): ArrayList<Slot2>
}

fun subTableReader(file: File): SubTableReader {
    return object : SubTableReader {
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
                Slot2.HardLink(
                    SlotMap(
                        mapOrKey
                    ), base, this
                )
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
}