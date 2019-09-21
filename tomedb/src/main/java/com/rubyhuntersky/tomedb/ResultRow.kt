package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.data.Projection

data class ResultRow(val row: Map<Query.Find.Slot, Any>) {

    fun toLegacy(): Map<String, Any> = row.mapKeys { it.key.slotName }

    operator fun invoke(slot: Query.Find.Slot): Any = row[slot] ?: error("No value for slot.")

    companion object {
        fun valueOf(legacy: Map<String, Any>): ResultRow {
            val values: Map<Query.Find.Slot, Any> = legacy.mapKeys {
                Query.CommonSlot(it.key)
            }
            return ResultRow(values)
        }
    }
}

data class FindResult(val rows: List<ResultRow>) {

    operator fun invoke(slot: Query.Find.Slot): List<Any> = rows.mapNotNull { it.row[slot] }
    operator fun invoke(): List<Any> = rows.mapNotNull { it.row.values.firstOrNull() }

    fun toLegacy() = rows.map(ResultRow::toLegacy)

    fun toProjections(
        eSlot: Query.Find.Slot,
        attr: Keyword,
        vSlot: Query.Find.Slot
    ): Sequence<Projection<Any>> = rows.asSequence().map {
        val ent = it(eSlot) as Long
        val value = it(vSlot)
        Projection(ent, attr, value)
    }

    fun toProjections(
        eSlot: Query.Find.Slot,
        aSlot: Query.Find.Slot,
        vSlot: Query.Find.Slot
    ): Sequence<Projection<Any>> = rows.asSequence().map {
        val ent = it(eSlot) as Long
        val attr = it(aSlot) as Keyword
        val value = it(vSlot)
        Projection(ent, attr, value)
    }

    fun toProjections(
        ent: Ent,
        aSlot: Query.Find.Slot,
        vSlot: Query.Find.Slot
    ): Sequence<Projection<Any>> = rows.asSequence().map {
        val attr = it(aSlot) as Keyword
        val value = it(vSlot)
        Projection(ent.long, attr, value)
    }

    fun toEnts(eSlot: Query.Find.Slot): Sequence<Ent> =
        rows.asSequence().map { it(eSlot) as Long }.map(::Ent)
}
