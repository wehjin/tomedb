package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.TimeClock

class TransientDatalog(private val timeClock: TimeClock = TimeClock.REALTIME) : Datalog {

    override fun commit() = Unit

    private val eavt =
        mutableMapOf<Long, MutableMap<Keyword, MutableMap<Any, MutableList<Txn>>>>()

    override fun ents(): Sequence<Long> = eavt.keys.asSequence()

    override fun attrs(): Sequence<Keyword> =
        TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun values(): Sequence<Any> = eavt.values.asSequence()
        .map(MutableMap<Keyword, MutableMap<Any, MutableList<Txn>>>::values).flatten()
        .map(MutableMap<Any, MutableList<Txn>>::entries).flatten()
        .filter { (_, txns) ->
            val latest = txns[0]
            latest.standing == Fact.Standing.Asserted
        }
        .map(MutableMap.MutableEntry<Any, MutableList<Txn>>::key).distinct()

    override fun attrs(entity: Long): Sequence<Keyword> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun values(entity: Long, attr: Keyword): Sequence<Any> {
        return (eavt[entity]?.get(attr)?.keys?.asSequence() ?: emptySequence())
    }

    override fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean {
        val txns = eavt[entity]?.get(attr)?.get(value)
        val firstTxn = txns?.get(0)
        return firstTxn?.isAsserted ?: false
    }

    override fun isAsserted(entity: Long, attr: Keyword): Boolean {
        val mapValueToTxnList = eavt[entity]?.get(attr)
        val txnLists = mapValueToTxnList?.values
        val firstTxns = txnLists?.map { it[0] }
        return firstTxns?.map(Txn::isAsserted)?.fold(initial = false, operation = Boolean::or)
            ?: false
    }

    override fun append(entity: Long, attr: Keyword, value: Any, standing: Fact.Standing): Fact {
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