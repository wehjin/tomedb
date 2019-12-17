package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.toKeyword
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.attrValues

class DatalogDatabase(private val datalog: Datalog) :
    Database {
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
        val found = BinderRack(fixedSolvers)
            .stir(outputs, rules1, datalog)
        return FindResult(found.map(ResultRow.Companion::valueOf))
    }

    override fun getUntypedDbValue(ent: Long, attr: Keyword): Any? {
        return datalog.values(ent, attr).firstOrNull()
    }

    override fun <KeyT : Any> getDbEntitiesOfClass(
        attr: Attribute<*>,
        cls: Class<KeyT>
    ): Sequence<Entity<KeyT>> {
        val keyword = attr.toKeyword()

        val ents = datalog.ents(keyword)
        return ents.mapNotNull { ent ->
            val data = datalog.attrValues(ent).toMap()
            val value: KeyT? = cls.cast(data[keyword])
            value?.let {
                Entity.from(attr, it, data)
            }
        }
    }

    override fun getOwners(attrName: Keyword): Sequence<Pair<Long, Map<Keyword, Any>>> {
        val ents = datalog.ents(attrName)
        return ents.mapNotNull { ent ->
            val data = datalog.attrValues(ent).toMap()
            data[attrName]?.let { Pair(ent, data) }
        }
    }
}