package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.connection.Database
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

    operator fun invoke(query: Query): List<Map<String, Value<*>>> {
        return when (query) {
            is Query.Find -> find1(query)
            is Query.Find2 -> find2(query).toLegacy()
        }
    }

    operator fun invoke(init: Query.Find2.() -> Unit): List<Map<String, Value<*>>> = this(Query.Find2(init))

    private fun find1(query: Query.Find): List<Map<String, Value<*>>> {
        val (inputs, rules, outputs) = query
        return find(inputs, outputs, rules)
    }

    private fun find(inputs: List<Input<*>>?, outputs: List<String>, rules: List<Rule>): List<Map<String, Value<*>>> {
        val initBinders = inputs?.map(Input<*>::toBinder)
        return BinderRack(initBinders).stir(outputs, rules, datalog)
    }

    override fun find2(query: Query.Find2): FindResult {
        val rules = query.rules
        val inputs: List<Input<*>> = rules.mapNotNull {
            when (it) {
                is Query.Find2.Rule2.SlipValue -> Input(it.slip.name, it.value)
                else -> null
            }
        }
        val rules1: List<Rule> = rules.mapNotNull {
            when (it) {
                is Query.Find2.Rule2.SlotAttrSlot -> Rule.EntityContainsAnyValueAtAttr(
                    entityVar = it.eSlot.keywordName,
                    valueVar = it.vSlot.keywordName,
                    attr = it.attr
                )
                is Query.Find2.Rule2.SlotAttrValue -> Rule.EntityContainsExactValueAtAttr(
                    entityVar = it.eSlot.keywordName,
                    value = it.value,
                    attr = it.attr
                )
                is Query.Find2.Rule2.SlotAttrESlot -> Rule.EntityContainsAnyEntityAtAttr(
                    entityVar = it.eSlot.keywordName,
                    entityValueVar = it.eSlot2.keywordName,
                    attr = it.attr
                )
                is Query.Find2.Rule2.SlotAttr -> Rule.EntityContainsAttr(
                    entityVar = it.slot.keywordName,
                    attr = it.attr
                )
                is Query.Find2.Rule2.SlipValue -> null
                is Query.Find2.Rule2.Slide -> null
            }
        }
        val outputs: List<String> = rules.mapNotNull {
            when (it) {
                is Query.Find2.Rule2.Slide -> it.keywordNames
                else -> null
            }
        }.flatten()
        val found = find(if (inputs.isEmpty()) null else inputs, outputs, rules1)
        return FindResult(found.map(ResultRow.Companion::valueOf))
    }

    override fun toString(): String {
        return "MutableDatabase(nextEntity=$nextEntity, datalog=$datalog)"
    }
}