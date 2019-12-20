package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.basics.bytesFromLong
import java.io.File
import java.io.FileOutputStream

class SubTableIo(file: File) {

    private var end = file.length()
    private val fileOutputStream = FileOutputStream(file)

    val reader = subTableReader(file)

    fun writeSlots(slots: List<Slot2>): Pair<SlotMap, Long> {
        require(slots.size == SubTable.slotCount)
        val base = end
        val slotMap = slots.foldIndexed(
            initial = SlotMap.empty(),
            operation = { index, slotMap, slot ->
                when (slot) {
                    Slot2.Empty -> slotMap
                    is Slot2.KeyValue -> {
                        writeBytes(
                            bytesFromLong(slot.key) + bytesFromLong(
                                slot.value
                            )
                        )
                        slotMap.setIndex(index)
                    }
                    is Slot2.HardLink -> {
                        writeBytes(
                            slot.slotMap.toBytes() + bytesFromLong(
                                slot.base
                            )
                        )
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