package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import java.io.File
import java.nio.file.Path
import java.util.*

class GitDatalog(
    private val timeClock: TimeClock,
    folderPath: Path
) : Datalog {

    private val gitFolder = folderPath.toFile().also { it.mkdirs() }

    private val git = try {
        Git.open(gitFolder).also { println("REPO FOUND: OPENING") }
    } catch (e: RepositoryNotFoundException) {
        Git.init().setDirectory(gitFolder).call().also { println("REPO NOT FOUND: INITIALIZING") }
    }

    private val txnIdCounter = TxnIdCounter(gitFolder)

    private val eavtFolder = File(gitFolder, "eavt").also { it.mkdirs() }

    override fun append(entity: Long, attr: ItemName, value: Value, standing: Fact.Standing): Fact {
        val instant = timeClock.now
        val txnId = txnIdCounter.nextTxnId()
        val txn = Txn(instant, standing, txnId)

        val entityHex = entity.toString(16)
        val e = File(eavtFolder, entityHex).also { it.mkdirs() }
        val attrCode = Base64.getEncoder().encodeToString(attr.toString().toByteArray())
        val ea = File(e, attrCode).also { it.mkdirs() }

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
        return Fact(entity, attr, value, standing, instant, txnId)
            .also {
                println("APPEND $it")
                git.add().addFilepattern(".").call()
                git.commit().setMessage("TXN: ${txnId.height}").call()
            }
    }

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

    private var nextTxnId = TxnId(1)

    override fun toString(): String = "Datalog(nextTxnId=$nextTxnId, eavt=$eavt)"
}