package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.TimeClock
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.b64Encoder
import com.rubyhuntersky.tomedb.datalog.Fact.Standing.Asserted
import com.rubyhuntersky.tomedb.datalog.Fact.Standing.Retracted
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
        val txnId = txnIdCounter.nextTxnId()

        val eDir = File(eavtFolder, entity.toFolderName()).also { it.mkdirs() }
        val eaDir = File(eDir, attr.toFolderName()).also { it.mkdirs() }
        val eavDir = File(eaDir, value.toFolderName()).also { it.mkdirs() }
        val txnFile = File(eavDir, txnId.height.toString()).apply { writeText("${standing.toContent()}\n") }
        val txnTime = Date(txnFile.lastModified())
        val txn = Txn(standing, txnTime, txnId)

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
        return Fact(entity, attr, value, standing, timeClock.now, txnId)
            .also {
                println("APPEND $it")
                git.add().addFilepattern(".").call()
                git.commit().setMessage("TXN: ${txnId.height}").call()
            }
    }

    private fun Long.toFolderName(): String = this.toString()

    private fun ItemName.toFolderName(): String {
        val first = b64Encoder.encodeToString(first.toByteArray())
        val last = b64Encoder.encodeToString(last.toByteArray())
        return "$first,$last"
    }

    private fun Fact.Standing.toContent(): String = when (this) {
        Asserted -> "asserted"
        Retracted -> "retracted"
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
                latest.standing == Asserted
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