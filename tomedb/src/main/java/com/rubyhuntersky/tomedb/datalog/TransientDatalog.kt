package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value

class TransientDatalog(private val timeClock: TimeClock) :
    Datalog {

    override val allEntities: List<Long>
        get() = eavt.keys.toList()

    private val eavt =
        mutableMapOf<Long, MutableMap<ItemName, MutableMap<Value, MutableList<Txn>>>>()

    override val allValues: List<Value>
        get() = eavt.values.asSequence()
            .map(MutableMap<ItemName, MutableMap<Value, MutableList<Txn>>>::values).flatten()
            .map(MutableMap<Value, MutableList<Txn>>::entries).flatten()
            .filter { (_, txns) ->
                val latest = txns[0]
                latest.standing == Fact.Standing.Asserted
            }
            .map(MutableMap.MutableEntry<Value, MutableList<Txn>>::key).distinct()
            .toList()

    override fun entityAttrValues(entity: Long, attr: ItemName): List<Value> {
        return eavt[entity]?.get(attr)?.keys?.toList() ?: emptyList()
    }

    override fun isEntityAttrValueAsserted(entity: Long, attr: ItemName, value: Value): Boolean {
        val txns = eavt[entity]?.get(attr)?.get(value)
        val firstTxn = txns?.get(0)
        return firstTxn?.isAsserted ?: false
    }

    override fun isEntityAttrAsserted(entity: Long, attr: ItemName): Boolean {
        val mapValueToTxnList = eavt[entity]?.get(attr)
        val txnLists = mapValueToTxnList?.values
        val firstTxns = txnLists?.map { it[0] }
        return firstTxns?.map(Txn::isAsserted)?.fold(initial = false, operation = Boolean::or) ?: false
    }

    override fun append(entity: Long, attr: ItemName, value: Value, standing: Fact.Standing): Fact {
        val instant = timeClock.now
        val txnId = nextTxnId++
        val txn = Txn(standing, instant, txnId)
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