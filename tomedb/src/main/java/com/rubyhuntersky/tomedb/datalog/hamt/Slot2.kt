package com.rubyhuntersky.tomedb.datalog.hamt

sealed class Slot2 {

    abstract fun asSubTable(depth: Int): SubTable

    object Empty : Slot2() {
        override fun asSubTable(depth: Int): SubTable = error("Not supported")
    }

    data class KeyValue(val key: Long, val value: Long) : Slot2() {
        override fun asSubTable(depth: Int): SubTable = error("Not supported")
    }

    data class SoftLink(val subTable: SubTable.Soft) : Slot2() {
        override fun asSubTable(depth: Int): SubTable = subTable.also { require(depth == it.depth) }
    }

    data class HardLink(
        val slotMap: SlotMap,
        val base: Long,
        val reader: SubTableReader
    ) : Slot2() {
        override fun asSubTable(depth: Int): SubTable =
            SubTable.Hard(depth, slotMap, base, reader)
    }
}