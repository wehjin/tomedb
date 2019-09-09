package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.datalog.framing.FrameReader

class HamtTable(bytes: ByteArray, type: HamtTableType) {

    sealed class SlotContent {
        data class KeyValue(val key: Long, val value: Long) : SlotContent()
        data class MapBase(val map: Long, val base: Long) : SlotContent() {
            fun toSubTable(frameReader: FrameReader): HamtTable {
                return HamtTable(
                    frameReader.read(base),
                    HamtTableType.Sub(map)
                )
            }
        }

        object Empty : SlotContent()
    }

    fun getSlotContent(index: Byte): SlotContent {
        return SlotContent.Empty
    }

    val map: Long
        get() {
            TODO()
        }

    fun toBytes(): ByteArray {
        TODO()
    }

    fun toRootBytes(): ByteArray {
        TODO()
    }

    fun fillSlotWithKeyValue(index: Byte, key: Long, value: Long): HamtTable {
        TODO()
    }

    fun fillSlotWithMapBase(index: Byte, map: Long, base: Long): HamtTable {
        TODO()
    }

    companion object {
        fun createRoot(index: Byte, key: Long, value: Long): HamtTable {
            TODO()
        }

        fun createSubWithKeyValues(indexKeyValues: Set<Triple<Byte, Long, Long>>): HamtTable {
            TODO()
        }

        fun createSubWithMapBase(index: Byte, map: Long, base: Long): HamtTable {
            TODO()
        }
    }
}