package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import java.util.*

class Datalog(private val timeClock: TimeClock) {

    fun append(entity: Long, attr: ItemName, value: Value, standing: Standing): Fact {
        val instant = timeClock.now
        val txnId = nextTxnId++
        val txn = Txn(instant, standing, txnId)
        val t = (eavt[entity]?.get(attr)?.get(value) ?: mutableListOf())
            .also {
                it.add(0, txn)
            }
        val vt = (eavt[entity]?.get(attr) ?: mutableMapOf())
            .also {
                it[value] = t
            }
        val avt = (eavt[entity] ?: mutableMapOf())
            .also {
                it[attr] = vt
            }
        eavt[entity] = avt
        val fact = Fact(entity, attr, value, standing, instant, txnId)
        println("APPEND $fact")
        return fact
    }

    private var nextTxnId = TxnId(1)

    private val eavt =
        mutableMapOf<Long, MutableMap<ItemName, MutableMap<Value, MutableList<Txn>>>>()

    private data class Txn(val time: Date, val standing: Standing, val txnId: TxnId) {

        val isAsserted: Boolean
            get() = standing == Standing.Asserted
    }

    val entities: List<Long>
        get() = eavt.keys.toList()

    val values: List<Value>
        get() = eavt.values.asSequence()
            .map(MutableMap<ItemName, MutableMap<Value, MutableList<Txn>>>::values).flatten()
            .map(MutableMap<Value, MutableList<Txn>>::entries).flatten()
            .filter { (_, postFacts) ->
                val latest = postFacts[0]
                latest.standing == Standing.Asserted
            }
            .map(MutableMap.MutableEntry<Value, MutableList<Txn>>::key).distinct()
            .toList()

    fun listValues(entity: Long, attrName: ItemName): List<Value> {
        return eavt[entity]?.get(attrName)?.keys?.toList() ?: emptyList()
    }

    fun checkAsserted(entity: Long, attrName: ItemName, value: Value): Boolean {
        val postFacts = eavt[entity]?.get(attrName)?.get(value)
        return postFacts?.get(0)?.standing == Standing.Asserted
    }

    fun checkAttributeAsserted(entity: Long, attrName: ItemName): Boolean {
        val mapValueToFactList = eavt[entity]?.get(attrName)
        val factLists = mapValueToFactList?.values
        val firstFacts = factLists?.map { it[0] }
        return firstFacts?.map(Txn::isAsserted)?.fold(initial = false, operation = Boolean::or) ?: false
    }

    override fun toString(): String {
        return "Datalog(eavt=$eavt)"
    }
}