package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.*
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Datalog
import com.rubyhuntersky.tomedb.datalog.Fact
import com.rubyhuntersky.tomedb.datalog.GitDatalog
import java.io.File

class MutableDatabase(dataDir: File) : Database {
    private var nextEntity: Long = 1
    internal fun nextEntity(): Long = nextEntity++

    internal fun update(updates: List<Update>): List<Fact> {
        return updates.map(this::update).also { commit() }
    }

    private fun update(update: Update): Fact {
        val (entity, attr, value, type) = update
        require(value !is Value.DATA)
        return datalog.append(entity, attr, value, type.toStanding())
    }

    private val datalog: Datalog = GitDatalog(dataDir)

    private fun Update.Action.toStanding(): Fact.Standing = when (this) {
        Update.Action.Declare -> Fact.Standing.Asserted
        Update.Action.Retract -> Fact.Standing.Retracted
    }

    internal fun commit() = datalog.commit()

    operator fun invoke(init: Query.Find.() -> Unit): List<Map<String, Value<*>>> = this(Query.build(init))
    operator fun invoke(query: Query): List<Map<String, Value<*>>> = find(query as Query.Find).toLegacy()

    override fun find(query: Query.Find): FindResult {
        val rules = query.rules
        val inputs: List<Input<*>> = rules.mapNotNull {
            when (it) {
                is Query.Find.Rule2.SlipValue -> Input(
                    it.slip.name,
                    it.value
                )
                else -> null
            }
        }
        val rules1: List<Rule> = rules.mapNotNull {
            when (it) {
                is Query.Find.Rule2.SlotAttrSlot -> Rule.EntityContainsAnyValueAtAttr(
                    entityVar = it.eSlot.keywordName,
                    valueVar = it.vSlot.keywordName,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlotAttrValue -> Rule.EntityContainsExactValueAtAttr(
                    entityVar = it.eSlot.keywordName,
                    value = it.value,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlotAttrESlot -> Rule.EntityContainsAnyEntityAtAttr(
                    entityVar = it.eSlot.keywordName,
                    entityValueVar = it.eSlot2.keywordName,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlotAttr -> Rule.EntityContainsAttr(
                    entityVar = it.slot.keywordName,
                    attr = it.attr
                )
                is Query.Find.Rule2.SlipValue -> null
                is Query.Find.Rule2.Slide -> null
            }
        }
        val outputs: List<String> = rules.mapNotNull {
            when (it) {
                is Query.Find.Rule2.Slide -> it.keywordNames
                else -> null
            }
        }.flatten()
        val fixedSolvers = (if (inputs.isEmpty()) null else inputs)?.map(Input<*>::toSolver)
        val found = BinderRack(fixedSolvers).stir(outputs, rules1, datalog)
        return FindResult(found.map(ResultRow.Companion::valueOf))
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}