package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.Scheme
import com.rubyhuntersky.tomedb.attributes.attrName
import com.rubyhuntersky.tomedb.attributes.groupedItemHashCode
import com.rubyhuntersky.tomedb.basics.Keyword
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
import kotlin.math.max

class FileDatalog(private val rootDir: File) : Datalog {
    // TODO Refactor with FileDatalist

    private var nextHeight = TxnId(0)
    private var appendable = true
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
    private fun getIndexReader(indexBase: Long?) = HamtReader(indexFrameReader, indexBase)

    private val attrFile = File(rootDir.apply { mkdirs() }, "attrs")
    private val attrTableFrameWriter = FrameWriter.new(attrFile)
    private val attrTableFrameReader get() = FrameReader.new(attrFile)
    private val attrTableWriter =
        HamtWriter({ attrTableFrameReader }, attrTableBase, attrTableFrameWriter)

    private val attrTableReader get() = HamtReader(attrTableFrameReader, attrTableBase)
    private val eavtReader get() = getIndexReader(eavtBase)
    private val aevtReader get() = getIndexReader(aevtBase)

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

    override val height: Long
        get() = nextHeight.height - 1

    override fun append(entity: Long, attr: Keyword, quant: Any, standing: Standing): Fact {
        check(appendable)
        val instant = Date()
        return Fact(entity, attr, quant, standing, instant, nextHeight++).also { addFact(it) }
    }

    override fun addFactsCommit(facts: Sequence<Fact>) {
        val maxHeightAfterNewFacts = facts.fold(
            initial = nextHeight.height - 1,
            operation = { maxHeight, fact ->
                addFact(fact)
                max(maxHeight, fact.txn.height)
            }
        )
        nextHeight = TxnId(maxHeightAfterNewFacts + 1)
        appendable = false
        commit()
    }

    private fun addFact(fact: Fact) {
        updateCardMap(fact.entity, fact.attr, fact.quant, cardinalityMap)
        val entKey = addEntToTable(fact.entity)
        val attrKey = addAttrToTable(fact.attr)

        val avtBase = eavtReader[entKey]
        val vtBase = getIndexReader(avtBase)[attrKey]
        val retracts =
            if (cardinalityMap[fact.attr] == Cardinality.ONE) readValueLines(vtBase) else emptySequence()
        val retractBase = retracts.fold(vtBase) { nextBase, retract ->
            PileWriter(nextBase, valueFrameWriter)
                .write(retract.flip(fact.inst, nextHeight++).toBytes())
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

    override fun toDatalist(height: Long): Datalist {
        return FileDatalist(rootDir, entTableBase, attrTableBase, eavtBase, aevtBase, height)
    }

    override fun factsOfAttr(attr: Keyword, minHeight: Long, maxHeight: Long): Sequence<Fact> {
        // TODO Decide if Datalog should implement Datalist
        return toDatalist().factsOfAttr(attr, minHeight, maxHeight)
    }

    override fun ents(attr: Keyword): Sequence<Long> {
        val entReader = entTableReader
        val evtBase = aevtReader[attr.toKey()]
        val evtReader = getIndexReader(evtBase)
        val ents = evtReader.keys().map { entFromEntKey(it, entReader) }
        return ents.filter { isAsserted(it, attr) }
    }

    private fun entFromEntKey(entKey: Long, entReader: HamtReader) = entReader[entKey]!!

    override fun ents(): Sequence<Long> {
        val entFromKeyReader = entTableReader
        return eavtReader.keys().map { entFromEntKey(it, entFromKeyReader) }
    }

    override fun factsOfEnt(ent: Long, minHeight: Long, maxHeight: Long): Sequence<Fact> {
        // TODO Should this be implemented here?
        return toDatalist().factsOfEnt(ent, minHeight, maxHeight)
    }

    override fun attrs(entity: Long): Sequence<Keyword> {
        val attrReader = attrTableReader
        val avtBase = eavtReader[entToKey(entity)]
        val avtReader = getIndexReader(avtBase)
        val attrs = avtReader.keys().map { keywordFromAttrKey(it, attrReader) }
        return attrs.filter { attr -> isAsserted(entity, attr) }
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
        val vtBase = getIndexReader(avtBase)[attr.toKey()]
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
            .flatMap { getIndexReader(it).values() }
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
