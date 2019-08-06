package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value

class TransientDatalog(private val timeClock: TimeClock = TimeClock.REALTIME) : Datalog {

    override fun commit() = Unit

    override val allEntities: List<Long>
        get() = eavt.keys.toList()

    private val eavt =
        mutableMapOf<Long, MutableMap<Keyword, MutableMap<Value<*>, MutableList<Txn>>>>()

    override val ents: Sequence<Long>
        get() = allEntities.asSequence()

    override val attrs: Sequence<Keyword>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val allAssertedValues: List<Value<*>>
        get() = eavt.values.asSequence()
            .map(MutableMap<Keyword, MutableMap<Value<*>, MutableList<Txn>>>::values).flatten()
            .map(MutableMap<Value<*>, MutableList<Txn>>::entries).flatten()
            .filter { (_, txns) ->
                val latest = txns[0]
                latest.standing == Fact.Standing.Asserted
            }
            .map(MutableMap.MutableEntry<Value<*>, MutableList<Txn>>::key).distinct()
            .toList()

    override fun attrs(entity: Long): Sequence<Value<Keyword>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun values(entity: Long, attr: Keyword): Sequence<Value<*>> {
        return (eavt[entity]?.get(attr)?.keys?.asSequence() ?: emptySequence())
    }

    override fun isAsserted(entity: Long, attr: Keyword, value: Value<*>): Boolean {
        val txns = eavt[entity]?.get(attr)?.get(value)
        val firstTxn = txns?.get(0)
        return firstTxn?.isAsserted ?: false
    }

    override fun isAsserted(entity: Long, attr: Keyword): Boolean {
        val mapValueToTxnList = eavt[entity]?.get(attr)
        val txnLists = mapValueToTxnList?.values
        val firstTxns = txnLists?.map { it[0] }
        return firstTxns?.map(Txn::isAsserted)?.fold(initial = false, operation = Boolean::or) ?: false
    }

    override fun append(entity: Long, attr: Keyword, value: Value<*>, standing: Fact.Standing): Fact {
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