package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TagList
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.FileDatalog
import com.rubyhuntersky.tomedb.datalog.attrValues
import java.io.File

class MutableDatabase(dataDir: File) : Database {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun update(updates: List<Update>): List<Fact> {
        return updates.map(this::update).also {
            if (it.isNotEmpty()) {
                commit()
            }
        }
    }

    private fun update(update: Update): Fact {
        val (entity, attr, value, type) = update
        require(value !is TagList)
        return datalog.append(entity, attr, value, type.toStanding())
    }

    private val datalog: Datalog = FileDatalog(dataDir)

    private fun Update.Action.toStanding(): Fact.Standing = when (this) {
        Update.Action.Declare -> Fact.Standing.Asserted
        Update.Action.Retract -> Fact.Standing.Retracted
    }

    internal fun commit() = datalog.commit()

    override fun getUntypedDbValue(entity: Long, attr: Keyword): Any? {
        return datalog.values(entity, attr).firstOrNull()
    }

    override fun <KeyT : Any> getDbEntitiesOfClass(
        attr: Attribute<*>,
        cls: Class<KeyT>
    ): Sequence<Entity<KeyT>> {
        val ents = datalog.ents(attr.toKeyword())
        return ents.mapNotNull { ent ->
            val data = datalog.attrValues(ent).toMap()
            val value: KeyT? = cls.cast(data[attr.toKeyword()])
            value?.let { Entity.from(attr, it, data) }
        }
    }

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
        val found = BinderRack(fixedSolvers).stir(outputs, rules1, datalog)
        return FindResult(found.map(ResultRow.Companion::valueOf))
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}