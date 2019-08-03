package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value

data class ResultRow(val row: Map<Query.Find2.Slot, Value<*>>) {

    fun toLegacy(): Map<String, Value<*>> = row.mapKeys { it.key.keywordName }


    companion object {
        fun valueOf(legacy: Map<String, Value<*>>): ResultRow {
            val values: Map<Query.Find2.Slot, Value<*>> = legacy.mapKeys {
                Query.Find2.CommonSlot(it.key)
            }
            return ResultRow(values)
        }
    }
}

data class FindResult(val rows: List<ResultRow>) {

    operator fun invoke(slot: Query.Find2.Slot): List<Value<*>> = rows.mapNotNull { it.row[slot] }
    operator fun invoke(): List<Value<*>> = rows.mapNotNull { it.row.values.firstOrNull() }

    fun toLegacy() = rows.map(ResultRow::toLegacy)
}
