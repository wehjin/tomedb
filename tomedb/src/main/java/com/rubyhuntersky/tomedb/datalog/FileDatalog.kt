package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

class FileDatalog : Datalog {

    override fun append(entity: Long, attr: Keyword, value: Any, standing: Fact.Standing): Fact {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun commit() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun ents(): Sequence<Long> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

data class TrieNode(
    val subTries: LongArray = LongArray(32)
)

class TrieReader(inputStream: InputStream, private val rootStart: Long?) {
    private val frameReader = FrameReader(inputStream)

    fun findValue(key: Long): Long? {
        return rootStart?.let { nodeStart ->
            null
        }
    }

}

class FactWriter {
    private val outputStream = ByteArrayOutputStream()
    private val writer = FrameWriter(outputStream, outputStream.size().toLong())
    private val eavtReader = TrieReader(ByteArrayInputStream(outputStream.toByteArray()), -1)
}


