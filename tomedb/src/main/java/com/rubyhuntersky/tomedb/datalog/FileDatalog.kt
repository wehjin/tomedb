package com.rubyhuntersky.tomedb.datalog

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import com.rubyhuntersky.tomedb.datalog.trie.TrieKey
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

class TrieNode(bytes: ByteArray) {

    operator fun get(index: Byte): Long? {
        return null
    }
}

class TrieReader(inputStream: InputStream, private val rootStart: Long?) {
    private val frameReader = FrameReader(inputStream)

    fun trace(key: Long): List<Pair<Byte, TrieNode?>> {
        val rootToValue = TrieKey(key).toIndices().fold(
            initial = Pair(rootStart, emptyList<Pair<Byte, TrieNode?>>()),
            operation = { (start, out), index ->
                start?.let {
                    val node = TrieNode(frameReader.read(start))
                    Pair(node[index], out + Pair(index, node))
                } ?: Pair(null, out + Pair(index, null))
            }
        ).second
        return rootToValue.reversed()
    }

    fun find(key: Long): Long? {
        return TrieKey(key).toIndices().fold(
            initial = rootStart,
            operation = { start, index ->
                start?.let {
                    val node = TrieNode(frameReader.read(start))
                    node[index]
                }
            }
        )
    }
}

class FactWriter {
    private val outputStream = ByteArrayOutputStream()
    private val writer = FrameWriter(outputStream, outputStream.size().toLong())
    private val eavtReader = TrieReader(ByteArrayInputStream(outputStream.toByteArray()), -1)
}


