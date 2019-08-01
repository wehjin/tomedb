package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.ItemName
import com.rubyhuntersky.tomedb.basics.Value
import com.rubyhuntersky.tomedb.basics.stringToFolderName
import com.rubyhuntersky.tomedb.datalog.Fact.Standing.Asserted
import com.rubyhuntersky.tomedb.datalog.Fact.Standing.Retracted
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.*

class GitDatalog(private val repoPath: Path) : Datalog {

    private val repoDir = repoPath.toFile().also { it.mkdirs() }

    private val git = try {
        Git.open(repoDir).also { println("REPO FOUND: OPENING") }
    } catch (e: RepositoryNotFoundException) {
        Git.init().setDirectory(repoDir).call().also { println("REPO NOT FOUND: INITIALIZING") }
    }

    private val txnIdCounter = TxnIdCounter(repoDir)

    private val eavtIndexDir = File(repoDir, "eavt").also { it.mkdirs() }

    override fun append(entity: Long, attr: ItemName, value: Value, standing: Fact.Standing): Fact {
        val txnId = txnIdCounter.nextTxnId()
        val eDir = entityDir(eavtIndexDir, entity).also { it.mkdirs() }
        val eaDir = attrDir(eDir, attr).also { it.mkdirs() }
        val eavDir = valueDir(eaDir, value).also { it.mkdirs() }
        val eavtFile = standingFile(eavDir).apply { writeText("${standing.toContent()}\n") }
        val txnTime = Date(eavtFile.lastModified())
        return Fact(entity, attr, value, standing, txnTime, txnId)
            .also {
                println("APPEND $it")
                git.add().addFilepattern(".").call()
                git.commit().setMessage("TXN: ${txnId.height}").call()
                git.tag().setName("h${txnId.height}").call()
            }
    }

    private fun entityDirs() = subFiles(eavtIndexDir).asSequence()
    private fun valueDirs(entity: Long, attr: ItemName): List<File> {
        return subFiles(specificAttrDir(entity, attr))
    }

    override val allEntities: List<Long>
        get() = entityDirs().map(File::getName).map { it.toLong() }.toList()

    override val allAssertedValues: List<Value>
        get() = entityDirs()
            .map(Companion::subFiles).flatten()
            .map(Companion::subFiles).flatten()
            .filter(Companion::isStandingAssertedInDir)
            .map(File::getName).map(::valueOfFolderName)
            .distinct().toList()

    override fun entityAttrValues(entity: Long, attr: ItemName): List<Value> {
        return valueDirs(entity, attr).map(::valueOfFile)
    }

    override fun isEntityAttrValueAsserted(entity: Long, attr: ItemName, value: Value): Boolean {
        val valueDir = specificValueDir(entity, attr, value)
        return isStandingAssertedInDir(valueDir)
    }

    private fun specificValueDir(entity: Long, attr: ItemName, value: Value): File =
        valueDir(specificAttrDir(entity, attr), value)

    private fun specificAttrDir(entity: Long, attr: ItemName): File = attrDir(specificEntityDir(entity), attr)

    private fun specificEntityDir(entity: Long): File = entityDir(eavtIndexDir, entity)

    override fun isEntityAttrAsserted(entity: Long, attr: ItemName): Boolean =
        valueDirs(entity, attr).map(Companion::isStandingAssertedInDir).fold(false, Boolean::or)

    private var nextTxnId = TxnId(1)

    override fun toString(): String = "Datalog(nextTxnId=$nextTxnId, repoPath=$repoPath)"

    companion object {

        private fun subFiles(folder: File): List<File> = (folder.listFiles() ?: emptyArray()).toList()

        private fun ItemName.toFolderName(): String {
            val first = stringToFolderName(first)
            val last = stringToFolderName(last)
            return "$first,$last"
        }

        private fun Fact.Standing.toContent(): String = when (this) {
            Asserted -> "asserted"
            Retracted -> "retracted"
        }

        private fun valueOfFile(file: File): Value {
            return valueOfFolderName(file.name)
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
        private fun valueDir(aDir: File, value: Value): File {
            val folderName = value.toFolderName()
            return File(aDir, folderName)
        }

        private fun attrDir(eDir: File, attr: ItemName) = File(eDir, attr.toFolderName())
        private fun entityDir(eavtDir: File, entity: Long) = File(eavtDir, entity.toString())
    }
}