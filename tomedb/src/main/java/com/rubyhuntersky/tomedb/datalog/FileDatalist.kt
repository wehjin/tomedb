package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.attributes.groupedItemHashCode
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.hamt.HamtReader
import com.rubyhuntersky.tomedb.datalog.hamt.UniHash
import com.rubyhuntersky.tomedb.datalog.pile.PileReader
import java.io.File

class FileDatalist(
    rootDir: File,
    entTableBase: Long?,
    attrTableBase: Long?,
    eavtBase: Long?,
    aevtBase: Long?,
    override val height: Long
) : Datalist {

    private val valueFile = File(rootDir.apply { mkdirs() }, "values")
    private val valueFrameReader = FrameReader.new(valueFile)

    private val entFile = File(rootDir.apply { mkdirs() }, "ent")
    private val entFrameReader = FrameReader.new(entFile)
    private val entTableReader = HamtReader(entFrameReader, entTableBase)

    private val indexFile = File(rootDir.apply { mkdirs() }, "index")
    private val indexFrameReader = FrameReader.new(indexFile)
    private fun getIndexReader(indexBase: Long?) = HamtReader(indexFrameReader, indexBase)

    private val attrFile = File(rootDir.apply { mkdirs() }, "attrs")
    private val attrTableFrameReader = FrameReader.new(attrFile)
    private val attrTableReader = HamtReader(attrTableFrameReader, attrTableBase)
    private val eavtReader = getIndexReader(eavtBase)
    private val aevtReader = getIndexReader(aevtBase)

    private fun entToKey(ent: Long): Long = UniHash.hashLong(ent)

    private fun Keyword.toKey() = UniHash.hashLong(groupedItemHashCode().toLong())

    override fun factsOfAttr(attr: Keyword, minHeight: Long, maxHeight: Long): Sequence<Fact> {
        val evtBase = aevtReader[attr.toKey()]
        val evtReader = getIndexReader(evtBase)
        return evtReader.entries().flatMap { (key, vtBase) ->
            val ent = entFromEntKey(key, entTableReader)
            readValueLines(vtBase)
                .dropWhile { it.height.height > maxHeight }
                .takeWhile { it.height.height > minHeight }
                .map { it.toFact(ent, attr) }
        }
    }

    override fun ents(attr: Keyword): Sequence<Long> {
        val evtBase = aevtReader[attr.toKey()]
        val evtReader = getIndexReader(evtBase)
        val entReader = entTableReader
        val ents = evtReader.keys().map { entFromEntKey(it, entReader) }
        return ents.filter { isAsserted(it, attr) }
    }

    private fun entFromEntKey(entKey: Long, entReader: HamtReader) = entReader[entKey]!!

    override fun ents(): Sequence<Long> {
        val entFromKeyReader = entTableReader
        return eavtReader.keys().map { entFromEntKey(it, entFromKeyReader) }
    }

    override fun factsOfEnt(ent: Long, minHeight: Long, maxHeight: Long): Sequence<Fact> {
        val avtBase = eavtReader[entToKey(ent)]
        val avtReader = getIndexReader(avtBase)
        return avtReader.entries().flatMap { (key, vtBase) ->
            val keyword = keywordFromAttrKey(key, attrTableReader)
            readValueLines(vtBase)
                .dropWhile { it.height.height > maxHeight }
                .takeWhile { it.height.height > minHeight }
                .map { it.toFact(ent, keyword) }
        }
    }

    override fun attrs(entity: Long): Sequence<Keyword> {
        val avtBase = eavtReader[entToKey(entity)]
        val avtReader = getIndexReader(avtBase)
        val attrReader = attrTableReader
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
            .dropWhile { it.height.height > height }
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
