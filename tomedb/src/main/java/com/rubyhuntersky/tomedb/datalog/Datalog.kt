package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import java.util.*

class Datalog(private val timeClock: TimeClock) {

    val allEntities: List<Long>
        get() = eavt.keys.toList()

    private val eavt =
        mutableMapOf<Long, MutableMap<ItemName, MutableMap<Value, MutableList<Txn>>>>()

    private data class Txn(val time: Date, val standing: Standing, val txnId: TxnId) {
        val isAsserted: Boolean
            get() = standing == Standing.Asserted
    }

    val allValues: List<Value>
        get() = eavt.values.asSequence()
            .map(MutableMap<ItemName, MutableMap<Value, MutableList<Txn>>>::values).flatten()
            .map(MutableMap<Value, MutableList<Txn>>::entries).flatten()
            .filter { (_, txns) ->
                val latest = txns[0]
                latest.standing == Standing.Asserted
            }
            .map(MutableMap.MutableEntry<Value, MutableList<Txn>>::key).distinct()
            .toList()

    fun entityAttrValues(entity: Long, attr: ItemName): List<Value> {
        return eavt[entity]?.get(attr)?.keys?.toList() ?: emptyList()
    }

    fun isEntityAttrValueAsserted(entity: Long, attr: ItemName, value: Value): Boolean {
        val txns = eavt[entity]?.get(attr)?.get(value)
        val firstTxn = txns?.get(0)
        return firstTxn?.isAsserted ?: false
    }

    fun isEntityAttrAsserted(entity: Long, attr: ItemName): Boolean {
        val mapValueToTxnList = eavt[entity]?.get(attr)
        val txnLists = mapValueToTxnList?.values
        val firstTxns = txnLists?.map { it[0] }
        return firstTxns?.map(Txn::isAsserted)?.fold(initial = false, operation = Boolean::or) ?: false
    }

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
        return Fact(entity, attr, value, standing, instant, txnId).also { println("APPEND $it") }
    }

    private var nextTxnId = TxnId(1)

    override fun toString(): String = "Datalog(nextTxnId=$nextTxnId, eavt=$eavt)"
}