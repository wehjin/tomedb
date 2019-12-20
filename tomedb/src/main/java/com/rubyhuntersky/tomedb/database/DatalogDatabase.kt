package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Datalist
import com.rubyhuntersky.tomedb.datalog.attrValues

class DatalogDatabase(private val datalist: Datalist) : Database {

    override fun find(query: Query.Find): FindResult {
        val rules = query.rules
        val inputs: List<Input<*>> = rules.mapNotNull {
            when (it) {
                is Query.Find.Rule2.SlipValue<*> -> Input(
                    it.slip.name,
                    it.value
                )
                else -> null
            }
        }
        val rules1: List<Rule> = rules.mapNotNull {
            when (it) {
                is Query.Find.Rule2.SlotAttrSlot -> Rule.SlotAttrSlot(
                    entityVar = it.eSlot.slotName,
                    valueVar = it.vSlot.slotName,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlotAttrValue -> Rule.SlotAttrValue(
                    entityVar = it.eSlot.slotName,
                    value = it.value,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlotAttrESlot -> Rule.SlotAttrESlot(
                    entityVar = it.eSlot.slotName,
                    entityValueVar = it.eSlot2.slotName,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlotAttr -> Rule.SlotAttr(
                    entityVar = it.slot.slotName,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlotSlotSlot -> Rule.SlotSlotSlot(
                    entityVar = it.eSlot.slotName,
                    attrVar = it.aSlot.slotName,
                    valueVar = it.vSlot.slotName
                )
                is Query.Find.Rule2.SlipValue<*> -> null
                is Query.Find.Rule2.Slide -> null
            }
        }
        val outputs: List<String> = rules.mapNotNull {
            when (it) {
                is Query.Find.Rule2.Slide -> it.keywordNames
                else -> null
            }
        }.flatten()
        val fixedSolvers = (if (inputs.isEmpty()) null else inputs)?.map { it.toSolver() }
        val found = BinderRack(fixedSolvers).stir(outputs, rules1, datalist)
        return FindResult(found.map(ResultRow.Companion::valueOf))
    }

    override fun getUntypedDbValue(ent: Long, attr: Keyword): Any? {
        return datalist.values(ent, attr).firstOrNull()
    }

    override fun getEntDataPairs(filter: Keyword): Sequence<Pair<Long, Map<Keyword, Any>>> {
        val ents = datalist.ents(filter)
        return ents.mapNotNull { ent ->
            val data = datalist.attrValues(ent).toMap()
            data[filter]?.let { Pair(ent, data) }
        }
    }
}