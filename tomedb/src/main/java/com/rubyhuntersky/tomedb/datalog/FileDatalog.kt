package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.basics.longFromBytes
import com.rubyhuntersky.tomedb.datalog.framing.FramePosition
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import com.rubyhuntersky.tomedb.datalog.hamt.HamtReader
import com.rubyhuntersky.tomedb.datalog.hamt.HamtWriter
import com.rubyhuntersky.tomedb.datalog.hamt.UniHash
import com.rubyhuntersky.tomedb.datalog.pile.PileReader
import com.rubyhuntersky.tomedb.datalog.pile.PileWriter
import java.io.File
import java.util.*

class FileDatalog(rootDir: File) : Datalog {

    private var nextHeight = TxnId(0)

    private val valueFile = File(rootDir.apply { mkdirs() }, "values")
    private val valueEnd = 0L
    private val valueFrameWriter = FrameWriter.new(valueFile, valueEnd)
    private val valueFrameReader get() = FrameReader.new(valueFile)

    private val entFile = File(rootDir.apply { mkdirs() }, "ent")
    private val entEnd = 0L
    private val entFrameWriter = FrameWriter.new(entFile, entEnd)
    private val entFrameReader get() = FrameReader.new(entFile)
    private var entTableBase: Long? = null
    private val entTableWriter = HamtWriter({ entFrameReader }, entTableBase, entFrameWriter)
    private val entTableReader get() = HamtReader(entFrameReader, entTableBase)

    private val indexFile = File(rootDir.apply { mkdirs() }, "index")
    private val indexEnd = 0L
    private val indexFrameWriter = FrameWriter.new(indexFile, indexEnd)
    private val indexFrameReader get() = FrameReader.new(indexFile)

    private val attrFile = File(rootDir.apply { mkdirs() }, "attrs")
    private val attrTableEnd = 0L
    private val attrTableFrameWriter = FrameWriter.new(attrFile, attrTableEnd)
    private val attrTableFrameReader get() = FrameReader.new(attrFile)
    private var attrTableBase: Long? = null
    private val attrTableWriter =
        HamtWriter({ attrTableFrameReader }, attrTableBase, attrTableFrameWriter)
    private val attrTableReader get() = HamtReader(attrTableFrameReader, attrTableBase)

    private var eavtBase: Long? = null
    private val eavtReader get() = getAvtReader(eavtBase)
    private var aevtBase: Long? = null
    private val aevtReader get() = getAvtReader(aevtBase)

    override fun append(entity: Long, attr: Keyword, value: Any, standing: Fact.Standing): Fact {
        val fact = Fact(entity, attr, value, standing, Date(), nextHeight++)
        val entKey = addEntToTable(fact.entity)
        val attrKey = addAttrToTable(fact.attr)

        val avtBase = eavtReader[entKey]
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
                key = entKey,
                value = newAvtBase
            )
        updateAevtBase(newVtBase, attrKey, entKey)
        return fact
    }

    private fun addEntToTable(ent: Long): Long {
        val entKey = entToKey(ent)
        return entKey.also {
            if (entTableReader[entKey] == null) {
                entTableBase = entTableWriter.write(entKey, ent)
            }
        }
    }

    private fun entToKey(ent: Long): Long = UniHash.hashLong(ent)

    private fun addAttrToTable(attr: Keyword): Long {
        return attr.toKey().also { attrKey ->
            if (attrTableReader[attrKey] == null) {
                AttrCoder.folderNameFromAttr(attr).let { folderName ->
                    val base = attrTableFrameWriter.write(folderName.toByteArray())
                    attrTableBase = attrTableWriter.write(attrKey, base)
                }
            }
        }
    }

    private fun Keyword.toKey() = UniHash.hashLong(groupedItemHashCode().toLong())

    private fun updateAevtBase(newVtBase: FramePosition, attrKey: Long, entKey: Long) {
        val newEvtBase =
            HamtWriter(
                refreshFrameReader = this::indexFrameReader,
                rootBase = HamtReader(indexFrameReader, aevtBase)[attrKey],
                frameWriter = indexFrameWriter
            ).write(entKey, newVtBase)
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

            fun from(byteArray: ByteArray): ValueLine {
                val valueLen = byteArray.size - 1 - Long.SIZE_BYTES - TxnId.bytesLen
                val instantStart = valueLen + 1
                val heightStart = instantStart + TxnId.bytesLen
                val valueBytes = byteArray.sliceArray(0 until valueLen)
                val standingByte = byteArray[valueLen]
                val instantBytes = byteArray.sliceArray(instantStart until heightStart)
                val heightBytes = byteArray.sliceArray(heightStart until byteArray.size)
                return ValueLine(
                    value = valueOfFolderName(String(valueBytes)),
                    standing = Fact.Standing.from(standingByte),
                    instant = Date(longFromBytes(instantBytes)),
                    height = TxnId.from(heightBytes)
                )
            }
        }
    }

    override fun commit() = Unit

    override fun ents(): Sequence<Long> {
        val entFromKeyReader = entTableReader
        return eavtReader.keys().map { entFromKeyReader[it]!! }
    }

    override fun attrs(entity: Long): Sequence<Keyword> {
        val entKey = entToKey(entity)
        val attrReader = attrTableReader
        return getAvtReader(eavtReader[entKey]).keys().map { keywordFromAttrKey(it, attrReader) }
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
        return readValueLines(entity, attr).map(ValueLine::value)
    }

    private fun readValueLines(entity: Long, attr: Keyword): Sequence<ValueLine> {
        val avtBase = eavtReader[entToKey(entity)]
        val vtBase = getAvtReader(avtBase)[attr.toKey()]
        return readValueLines(vtBase)
    }

    private fun readValueLines(vtBase: Long?): Sequence<ValueLine> {
        return PileReader(vtBase, valueFrameReader).read().map(ValueLine.Companion::from)
            .filter { it.standing.isAsserted }
    }

    override fun values(): Sequence<Any> {
        return eavtReader.values()
            .flatMap { getAvtReader(it).values() }
            .flatMap { readValueLines(it) }
            .map(ValueLine::value)
    }

    override fun isAsserted(entity: Long, attr: Keyword, value: Any): Boolean {
        return values(entity, attr).toSet().contains(value)
    }

    override fun isAsserted(entity: Long, attr: Keyword): Boolean {
        return readValueLines(entity, attr).count() > 0
    }
}
