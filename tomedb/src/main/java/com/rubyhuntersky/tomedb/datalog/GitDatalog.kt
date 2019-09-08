package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.basics.Keyword
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
            val nameValue = value(ent, Scheme.NAME.attrName)
            val cardinalityValue = value(ent, Scheme.CARDINALITY.attrName)
            it[nameValue] = cardinalityValue
        }
    }

    override fun append(entity: Long, attr: Keyword, value: Any, standing: Fact.Standing): Fact {
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

    private fun updateCardMap(entity: Long, attr: Keyword, value: Any, cardMap: CardinalityMap) {
        if (attr == Scheme.CARDINALITY.attrName) {
            val nameValue = value(entity, Scheme.NAME.attrName)
            cardMap[nameValue] = value
        }
        if (attr == Scheme.NAME.attrName) {
            val cardinalityValue = value(entity, Scheme.CARDINALITY.attrName)
            cardMap[value] = cardinalityValue
        }
    }

    private fun entDirs() = subFiles(eavtIndexDir).asSequence()
    private fun attrDirs(entity: Long) = subFiles(entityDir(entity)).asSequence()
    private fun valueDirs(entity: Long, attr: Keyword) =
        subFiles(entityAttrDir(entity, attr)).asSequence()

    override fun attrs(): Sequence<Keyword> = ents().flatMap { ent ->
        subFiles(entityDir(ent)).asSequence().map { it.name }.map(AttrCoder::attrFromFolderName)
    }

    override fun ents(): Sequence<Long> = entDirs().map(File::getName).map { it.toLong() }

    override fun values(): Sequence<Any> = entDirs()
        .map(Companion::subFiles).flatten()
        .map(Companion::subFiles).flatten()
        .filter(Companion::isStandingAssertedInDir)
        .map(File::getName).map(::valueOfFolderName)
        .distinct()

    override fun attrs(entity: Long): Sequence<Keyword> {
        return attrDirs(entity).map { AttrCoder.attrFromFolderName(it.name) }.map { (it) }
    }

    override fun values(entity: Long, attr: Keyword): Sequence<Any> {
        return valueDirs(entity, attr).map { valueOfFolderName(it.name) }
    }

    override fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean {
        val valueDir = entityAttrValueDir(entity, attr, value)
        return isStandingAssertedInDir(valueDir)
    }

    private fun entityAttrValueDir(entity: Long, attr: Keyword, value: Any): File =
        valueDir(entityAttrDir(entity, attr), value)

    private fun entityAttrDir(entity: Long, attr: Keyword): File = attrDir(entityDir(entity), attr)
    private fun entityDir(entity: Long): File = entityDir(eavtIndexDir, entity)

    override fun isAsserted(entity: Long, attr: Keyword): Boolean =
        valueDirs(entity, attr).map(Companion::isStandingAssertedInDir).fold(false, Boolean::or)

    override fun toString(): String {
        return "GitDatalog(repoDir=$repoDir, txnIdCounter=$txnIdCounter)"
    }

    companion object {

        private fun subFiles(folder: File): List<File> =
            (folder.listFiles() ?: emptyArray()).toList()

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

        private fun valueDir(aDir: File, value: Any): File {
            val folderName = value.toFolderName()
            return File(aDir, folderName)
        }

        private fun attrDir(eDir: File, attr: Keyword) =
            File(eDir, AttrCoder.folderNameFromAttr(attr))

        private fun entityDir(eavtDir: File, entity: Long) = File(eavtDir, entity.toString())
    }
}