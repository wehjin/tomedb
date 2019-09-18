package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.Scheme
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
import java.io.RandomAccessFile
import java.util.*

class FileDatalog(rootDir: File) : Datalog {

    private var nextHeight = TxnId(0)
    private var entTableBase: Long? = null
    private var attrTableBase: Long? = null
    private var eavtBase: Long? = null
    private var aevtBase: Long? = null
    private val headFile = RandomAccessFile(File(rootDir.apply { mkdirs() }, "head"), "rw")

    init {
        fun RandomAccessFile.readLongOrNull(): Long? {
            return readLong().let { if (it == -1L) null else it }
        }
        if (headFile.length() > 0) {
            with(headFile) {
                seek(0)
                nextHeight = TxnId(readLong())
                entTableBase = readLongOrNull()
                attrTableBase = readLongOrNull()
                eavtBase = readLongOrNull()
                aevtBase = readLongOrNull()
            }
        }
    }

    private val valueFile = File(rootDir.apply { mkdirs() }, "values")
    private val valueFrameWriter = FrameWriter.new(valueFile)
    private val valueFrameReader get() = FrameReader.new(valueFile)

    private val entFile = File(rootDir.apply { mkdirs() }, "ent")
    private val entFrameWriter = FrameWriter.new(entFile)
    private val entFrameReader get() = FrameReader.new(entFile)
    private val entTableWriter = HamtWriter({ entFrameReader }, entTableBase, entFrameWriter)
    private val entTableReader get() = HamtReader(entFrameReader, entTableBase)

    private val indexFile = File(rootDir.apply { mkdirs() }, "index")
    private val indexFrameWriter = FrameWriter.new(indexFile)
    private val indexFrameReader get() = FrameReader.new(indexFile)

    private val attrFile = File(rootDir.apply { mkdirs() }, "attrs")
    private val attrTableFrameWriter = FrameWriter.new(attrFile)
    private val attrTableFrameReader get() = FrameReader.new(attrFile)
    private val attrTableWriter =
        HamtWriter({ attrTableFrameReader }, attrTableBase, attrTableFrameWriter)

    private val attrTableReader get() = HamtReader(attrTableFrameReader, attrTableBase)
    private val eavtReader get() = getAvtReader(eavtBase)
    private val aevtReader get() = getAvtReader(aevtBase)

    private val cardinalityMap = CardinalityMap().also { cardMap ->
        ents().forEach { ent ->
            val nameValue = value(ent, Scheme.NAME.attrName)
            val cardinalityValue = value(ent, Scheme.CARDINALITY.attrName)
            cardMap[nameValue] = cardinalityValue
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

    override fun append(entity: Long, attr: Keyword, value: Any, standing: Fact.Standing): Fact {
        updateCardMap(entity, attr, value, cardinalityMap)
        val instant = Date()
        val fact = Fact(entity, attr, value, standing, instant, nextHeight++)
        val entKey = addEntToTable(fact.entity)
        val attrKey = addAttrToTable(fact.attr)

        val avtBase = eavtReader[entKey]
        val vtBase = getAvtReader(avtBase)[attrKey]
        val retracts =
            if (cardinalityMap[attr] == Cardinality.ONE) readValueLines(vtBase) else emptySequence()
        val retractBase = retracts.fold(vtBase) { nextBase, retract ->
            PileWriter(nextBase, valueFrameWriter)
                .write(retract.flip(instant, nextHeight++).toBytes())
        }
        val newVtBase = PileWriter(retractBase, valueFrameWriter)
            .write(ValueLine.from(fact).toBytes())
        val newAvtBase = HamtWriter(this::indexFrameReader, avtBase, indexFrameWriter).write(
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
        fun flip(instant: Date, height: TxnId): ValueLine {
            return copy(standing = standing.flip(), instant = instant, height = height)
        }

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
                val value = valueOfFolderName(String(valueBytes))
                return ValueLine(
                    value = value,
                    standing = Fact.Standing.from(standingByte),
                    instant = Date(longFromBytes(instantBytes)),
                    height = TxnId.from(heightBytes)
                )
            }
        }
    }

    override fun commit() {
        with(headFile) {
            seek(0)
            writeLong(nextHeight.height)
            writeLong(entTableBase ?: -1L)
            writeLong(attrTableBase ?: -1L)
            writeLong(eavtBase ?: -1L)
            writeLong(aevtBase ?: -1L)
        }
    }

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
        val retracted = mutableSetOf<Any>()
        return PileReader(vtBase, valueFrameReader).read()
            .map(ValueLine.Companion::from)
            .filter {
                if (it.standing.isRetracted) {
                    retracted.add(it.value)
                }
                !retracted.contains(it.value)
            }
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
