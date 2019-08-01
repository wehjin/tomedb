package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Meter
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value

class TransientDatalog(private val timeClock: TimeClock) :
    Datalog {

    override val allEntities: List<Long>
        get() = eavt.keys.toList()

    private val eavt =
        mutableMapOf<Long, MutableMap<Meter, MutableMap<Value, MutableList<Txn>>>>()

    override val allAssertedValues: List<Value>
        get() = eavt.values.asSequence()
            .map(MutableMap<Meter, MutableMap<Value, MutableList<Txn>>>::values).flatten()
            .map(MutableMap<Value, MutableList<Txn>>::entries).flatten()
            .filter { (_, txns) ->
                val latest = txns[0]
                latest.standing == Fact.Standing.Asserted
            }
            .map(MutableMap.MutableEntry<Value, MutableList<Txn>>::key).distinct()
            .toList()

    override fun entityMeterValues(entity: Long, meter: Meter): List<Value> {
        return eavt[entity]?.get(meter)?.keys?.toList() ?: emptyList()
    }

    override fun isEntityMeterValueAsserted(entity: Long, meter: Meter, value: Value): Boolean {
        val txns = eavt[entity]?.get(meter)?.get(value)
        val firstTxn = txns?.get(0)
        return firstTxn?.isAsserted ?: false
    }

    override fun isEntityMeterAsserted(entity: Long, meter: Meter): Boolean {
        val mapValueToTxnList = eavt[entity]?.get(meter)
        val txnLists = mapValueToTxnList?.values
        val firstTxns = txnLists?.map { it[0] }
        return firstTxns?.map(Txn::isAsserted)?.fold(initial = false, operation = Boolean::or) ?: false
    }

    override fun append(entity: Long, meter: Meter, value: Value, standing: Fact.Standing): Fact {
        val instant = timeClock.now
        val txnId = nextTxnId++
        val txn = Txn(standing, instant, txnId)
        val t = (eavt[entity]?.get(meter)?.get(value) ?: mutableListOf())
            .also {
                it.add(0, txn)
            }
        val vt = (eavt[entity]?.get(meter) ?: mutableMapOf())
            .also {
                it[value] = t
            }
        val avt = (eavt[entity] ?: mutableMapOf())
            .also {
                it[meter] = vt
            }
        eavt[entity] = avt
        return Fact(entity, meter, value, standing, instant, txnId).also { println("APPEND $it") }
    }

    private var nextTxnId = TxnId(1)

    override fun toString(): String = "Datalog(nextTxnId=$nextTxnId, eavt=$eavt)"
}