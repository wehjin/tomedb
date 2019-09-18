package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.datalog.framing.FramePosition
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import com.rubyhuntersky.tomedb.datalog.hamt.HamtReader
import com.rubyhuntersky.tomedb.datalog.hamt.HamtWriter
import com.rubyhuntersky.tomedb.datalog.pile.PileWriter
import java.io.File
import java.util.*

class FileDatalog(rootDir: File) : Datalog {

    private var nextHeight = TxnId(0)

    private val valueFile = File(rootDir.apply { mkdirs() }, "values")
    private val valueEnd = 0L
    private val valueFrameWriter = FrameWriter.new(valueFile, valueEnd)
    private val indexFile = File(rootDir.apply { mkdirs() }, "index")
    private val indexEnd = 0L
    private val indexFrameWriter = FrameWriter.new(indexFile, indexEnd)
    private val indexFrameReader get() = FrameReader.new(indexFile)
    private val attrFile = File(rootDir.apply { mkdirs() }, "attrs")
    private val attrTableEnd = 0L
    private val attrTableFrameWriter = FrameWriter.new(attrFile, attrTableEnd)
    private val attrTableFrameReader get() = FrameReader.new(attrFile)
    private var attrTableBase: Long? = null
    private val attrTableWriter = HamtWriter(
        refreshFrameReader = { attrTableFrameReader },
        rootBase = attrTableBase,
        frameWriter = attrTableFrameWriter
    )
    private val attrTableReader get() = HamtReader(attrTableFrameReader, attrTableBase)
    private var eavtBase: Long? = null
    private val eavtReader get() = getAvtReader(eavtBase)
    private var aevtBase: Long? = null
    private val aevtReader get() = getAvtReader(aevtBase)

    override fun append(entity: Long, attr: Keyword, value: Any, standing: Fact.Standing): Fact {
        val fact = Fact(entity, attr, value, standing, Date(), nextHeight++)
        val avtBase = eavtReader[fact.entity]
        val attrKey = addAttrToTable(fact.attr)
        val vtBase = getAvtReader(avtBase)[attrKey]
        val newVtBase = PileWriter(vtBase, valueFrameWriter)
            .write(ValueLine.from(fact).toBytes())
        val newAvtBase =
            HamtWriter(this::indexFrameReader, avtBase, indexFrameWriter).write(
                key = attrKey,
                value = newVtBase
            )
        eavtBase =
            HamtWriter(this::indexFrameReader, eavtBase, indexFrameWriter).write(
                key = fact.entity,
                value = newAvtBase
            )
        updateAevtBase(fact, newVtBase, attrKey)
        return fact
    }

    private fun addAttrToTable(attr: Keyword): Long {
        return attr.groupedItemHashCode().toLong().also { attrKey ->
            if (attrTableReader[attrKey] == null) {
                AttrCoder.folderNameFromAttr(attr).let { folderName ->
                    val base = attrTableFrameWriter.write(folderName.toByteArray())
                    attrTableBase = attrTableWriter.write(attrKey, base)
                }
            }
        }
    }

    private fun updateAevtBase(fact: Fact, newVtBase: FramePosition, attrKey: Long) {
        val newEvtBase =
            HamtWriter(
                refreshFrameReader = this::indexFrameReader,
                rootBase = HamtReader(indexFrameReader, aevtBase)[attrKey],
                frameWriter = indexFrameWriter
            ).write(fact.entity, newVtBase)
        aevtBase =
            HamtWriter(
                refreshFrameReader = this::indexFrameReader,
                rootBase = aevtBase,
                frameWriter = indexFrameWriter
            ).write(attrKey, newEvtBase)
    }

    private fun getAvtReader(avtBase: Long?) = HamtReader(indexFrameReader, avtBase)

    private data class ValueLine(
        val value: Any,
        val standing: Fact.Standing,
        val instant: Date,
        val height: TxnId
    ) {

        fun toBytes(): ByteArray {
            val valueBytes = value.toFolderName().toByteArray()
            val standingByte = standing.asByte()
            val instantBytes = bytesFromLong(instant.time)
            val heightBytes = height.toBytes()
            return valueBytes + standingByte + instantBytes + heightBytes
        }

        companion object {
            fun from(fact: Fact): ValueLine =
                ValueLine(fact.value, fact.standing, fact.inst, fact.txn)
        }

    }

    override fun commit() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ents(): Sequence<Long> = eavtReader.keys()

    override fun attrs(entity: Long): Sequence<Keyword> {
        val attrReader = attrTableReader
        return getAvtReader(eavtReader[entity]).keys().map { keywordFromAttrKey(it, attrReader) }
    }

    private fun keywordFromAttrKey(attrKey: Long, attrReader: HamtReader): Keyword {
        val base = attrReader[attrKey] ?: error("Attribute missing from table")
        val bytes = attrTableFrameReader.read(base)
        return AttrCoder.attrFromFolderName(String(bytes))
    }

    override fun attrs(): Sequence<Keyword> {
        val attrReader = attrTableReader
        return aevtReader.keys().map { keywordFromAttrKey(it, attrReader) }
    }

    override fun values(entity: Long, attr: Keyword): Sequence<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun values(): Sequence<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isAsserted(entity: Long, attr: Keyword): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
