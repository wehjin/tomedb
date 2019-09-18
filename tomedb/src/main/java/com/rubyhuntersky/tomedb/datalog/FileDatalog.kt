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
import java.io.RandomAccessFile
import java.util.*

class FileDatalog(rootDir: File) : Datalog {

    private var nextHeight = TxnId(0)

    private val valueFile = File(rootDir.apply { mkdirs() }, "values")
    private val valueEnd = 0L
    private val valueFrameWriter = FrameWriter(RandomAccessFile(valueFile, "rw"), valueEnd)
    private val indexFile = File(rootDir.apply { mkdirs() }, "index")
    private val indexEnd = 0L
    private val indexFrameWriter = FrameWriter(RandomAccessFile(indexFile, "rw"), indexEnd)
    private var eavtBase: Long? = null
    private var aevtBase: Long? = null

    override fun append(entity: Long, attr: Keyword, value: Any, standing: Fact.Standing): Fact {
        val fact = Fact(entity, attr, value, standing, Date(), nextHeight++)
        val avtBase = HamtReader(getIndexFrameReader(), eavtBase)[fact.entity]
        val attrKey = fact.attr.toKey()
        val vtBase = avtBase?.let { HamtReader(getIndexFrameReader(), it) }?.get(attrKey)
        val newVtBase = PileWriter(vtBase, valueFrameWriter)
            .write(ValueLine.from(fact).toBytes())
        val newAvtBase =
            HamtWriter(this::getIndexFrameReader, avtBase, indexFrameWriter).write(
                key = attrKey,
                value = newVtBase
            )
        eavtBase =
            HamtWriter(this::getIndexFrameReader, eavtBase, indexFrameWriter).write(
                key = fact.entity,
                value = newAvtBase
            )
        updateAevtBase(fact, newVtBase)
        return fact
    }

    private fun updateAevtBase(fact: Fact, newVtBase: FramePosition) {
        val attrKey = fact.attr.toKey()
        val newEvtBase =
            HamtWriter(
                refreshFrameReader = this::getIndexFrameReader,
                rootBase = HamtReader(getIndexFrameReader(), aevtBase)[attrKey],
                frameWriter = indexFrameWriter
            ).write(fact.entity, newVtBase)
        aevtBase =
            HamtWriter(
                refreshFrameReader = this::getIndexFrameReader,
                rootBase = aevtBase,
                frameWriter = indexFrameWriter
            ).write(attrKey, newEvtBase)
    }

    private fun getIndexFrameReader() = FrameReader(RandomAccessFile(indexFile, "r"))

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

    private fun Keyword.toKey(): Long = this.groupedItemHashCode().toLong()

    override fun commit() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ents(): Sequence<Long> {
        return emptySequence()
    }

    override fun attrs(entity: Long): Sequence<Keyword> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun attrs(): Sequence<Keyword> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
