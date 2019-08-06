package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.datalog.Fact.Standing.Asserted
import com.rubyhuntersky.tomedb.datalog.Fact.Standing.Retracted
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class GitDatalog(private val repoDir: File) : Datalog {

    init {
        repoDir.mkdirs()
    }

    private val git = try {
        Git.open(repoDir).also { println("REPO FOUND: OPENING") }
    } catch (e: RepositoryNotFoundException) {
        Git.init().setDirectory(repoDir).call().also { println("REPO NOT FOUND: INITIALIZING") }
    }

    private val txnIdCounter = TxnIdCounter(repoDir)
    private val eavtIndexDir = File(repoDir, "eavt").also { it.mkdirs() }
    private var lastAppended: TxnId? = null

    private val cardinalityMap = CardinalityMap().also {
        entDirs().forEach { eDir ->
            val ent = eDir.name.toLong()
            val nameValue = assertedValueAtEntityAttr(ent, Scheme.NAME)
            val cardinalityValue = assertedValueAtEntityAttr(ent, Scheme.CARDINALITY)
            it[nameValue] = cardinalityValue
        }
    }

    override fun append(entity: Long, attr: Keyword, value: Value<*>, standing: Fact.Standing): Fact {
        updateCardMap(entity, attr, value, cardinalityMap)
        val txnId = txnIdCounter.txnId
        val eDir = entityDir(eavtIndexDir, entity).also { it.mkdirs() }
        val eaDir = attrDir(eDir, attr).also { it.mkdirs() }
        if (cardinalityMap[attr] == Cardinality.ONE) eaDir.deleteRecursively()
        val eavDir = valueDir(eaDir, value).also { it.mkdirs() }
        val eavtFile = standingFile(eavDir).apply { writeText("${standing.toContent()}\n") }
        val txnTime = Date(eavtFile.lastModified())
        return Fact(entity, attr, value, standing, txnTime, txnId).also {
            this.lastAppended = txnId
            txnIdCounter.advance()
            println("APPEND: $it")
        }
    }


    override fun commit() {
        lastAppended?.let {
            git.add().addFilepattern(".").call()
            val message = "COMMIT: $it"
            git.commit().setMessage(message).call()
            git.tag().setName("h${it.height}").call()
            println(message)
            lastAppended = null
        }
    }

    private fun updateCardMap(entity: Long, attr: Keyword, value: Value<*>, cardMap: CardinalityMap) {
        if (attr.keywordEquals(Scheme.CARDINALITY)) {
            val nameValue = assertedValueAtEntityAttr(entity, Scheme.NAME)
            cardMap[nameValue] = value
        }
        if (attr.keywordEquals(Scheme.NAME)) {
            val cardinalityValue = assertedValueAtEntityAttr(entity, Scheme.CARDINALITY)
            cardMap[value] = cardinalityValue
        }
    }

    private fun entDirs() = subFiles(eavtIndexDir).asSequence()
    private fun attrDirs(entity: Long) = subFiles(entityDir(entity)).asSequence()
    private fun valueDirs(entity: Long, attr: Keyword) = subFiles(entityAttrDir(entity, attr)).asSequence()

    override val allEntities: List<Long>
        get() = entDirs().map(File::getName).map { it.toLong() }.toList()

    override val attrs: Sequence<Keyword>
        get() = ents.flatMap { ent ->
            subFiles(entityDir(ent)).asSequence().map { it.name }.map(AttrCoder::attrFromFolderName)
        }

    override val ents: Sequence<Long>
        get() = allEntities.asSequence()

    override val allAssertedValues: List<Value<*>>
        get() = entDirs()
            .map(Companion::subFiles).flatten()
            .map(Companion::subFiles).flatten()
            .filter(Companion::isStandingAssertedInDir)
            .map(File::getName).map(::valueOfFolderName)
            .distinct().toList()

    override fun attrs(entity: Long): Sequence<Value<Keyword>> {
        return attrDirs(entity).map { AttrCoder.attrFromFolderName(it.name) }.map { Value.of(it) }
    }

    override fun values(entity: Long, attr: Keyword): Sequence<Value<*>> {
        return valueDirs(entity, attr).map { valueOfFolderName(it.name) }
    }

    override fun isAsserted(entity: Long, attr: Keyword, value: Value<*>): Boolean {
        val valueDir = entityAttrValueDir(entity, attr, value)
        return isStandingAssertedInDir(valueDir)
    }

    private fun entityAttrValueDir(entity: Long, attr: Keyword, value: Value<*>): File =
        valueDir(entityAttrDir(entity, attr), value)

    private fun entityAttrDir(entity: Long, attr: Keyword): File = attrDir(entityDir(entity), attr)
    private fun entityDir(entity: Long): File = entityDir(eavtIndexDir, entity)

    override fun isAsserted(entity: Long, attr: Keyword): Boolean =
        valueDirs(entity, attr).map(Companion::isStandingAssertedInDir).fold(false, Boolean::or)

    override fun toString(): String {
        return "GitDatalog(repoDir=$repoDir, txnIdCounter=$txnIdCounter)"
    }

    companion object {

        private fun subFiles(folder: File): List<File> = (folder.listFiles() ?: emptyArray()).toList()

        private fun Fact.Standing.toContent(): String = when (this) {
            Asserted -> "asserted"
            Retracted -> "retracted"
        }

        private fun standingOfFile(file: File): Fact.Standing {
            var standing: Fact.Standing = Retracted
            file.forEachLine { if (it == "asserted") standing = Asserted }
            return standing
        }

        private fun isStandingAssertedInDir(dir: File): Boolean = try {
            standingOfFile(standingFile(dir)).isAsserted
        } catch (e: FileNotFoundException) {
            false
        }

        private fun standingFile(vDir: File) = File(vDir, "standing")

        private fun valueDir(aDir: File, value: Value<*>): File {
            val folderName = value.toFolderName()
            return File(aDir, folderName)
        }

        private fun attrDir(eDir: File, attr: Keyword) = File(eDir, AttrCoder.folderNameFromAttr(attr))
        private fun entityDir(eavtDir: File, entity: Long) = File(eavtDir, entity.toString())
    }
}