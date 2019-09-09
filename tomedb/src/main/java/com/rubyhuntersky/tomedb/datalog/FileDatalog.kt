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

sealed class HamtTableType {
    object Root : HamtTableType()
    data class Sub(val map: Long) : HamtTableType()
}

class HamtTable(bytes: ByteArray, type: HamtTableType) {

    sealed class SlotContent {
        data class KeyValue(val key: Long, val value: Long) : SlotContent()
        data class MapBase(val map: Long, val base: Long) : SlotContent() {
            fun toSubTable(frameReader: FrameReader): HamtTable {
                return HamtTable(frameReader.read(base), HamtTableType.Sub(map))
            }
        }

        object Empty : SlotContent()
    }

    fun getSlotContent(index: Byte): SlotContent {
        return SlotContent.Empty
    }

    fun toBytes(): ByteArray {
        TODO()
    }

    fun fillSlot(index: Byte, key: Long, value: Long): HamtTable {
        TODO()
    }

    companion object {
        fun createRoot(index: Byte, key: Long, value: Long): HamtTable {
            TODO()
        }

        fun createSub(indexKeyValues: Set<Triple<Byte, Long, Long>>): HamtTable {
            TODO()
        }
    }
}

class HamtWriter(
    inputStream: InputStream,
    private var rootBase: Long?,
    private val frameWriter: FrameWriter
) {

    private val frameReader = FrameReader(inputStream)

    private data class Conflict(
        val key: Long,
        val value: Long,
        val indices: Sequence<Byte>
    )

    private sealed class Insert {
        data class Descend(
            val table: HamtTable,
            val ancestors: List<Pair<HamtTable, Byte>>
        ) : Insert()

        data class DescendConflicted(
            val conflict: Conflict,
            val ancestors: List<Pair<HamtTable?, Byte>>
        ) : Insert()

        data class Ascend(
            val revisedTable: HamtTable,
            val rewriteTables: List<Pair<HamtTable?, Byte>>
        ) : Insert()
    }

    fun put(key: Long, value: Long) {
        rootBase = rootBase?.let {
            val rootTable = HamtTable(frameReader.read(it), HamtTableType.Root)
            val insert = TrieKey(key).toIndices().fold(
                initial = Insert.Descend(rootTable, emptyList()) as Insert,
                operation = { insert, index ->
                    when (insert) {
                        is Insert.Descend -> {
                            when (val slotContent = insert.table.getSlotContent(index)) {
                                is HamtTable.SlotContent.MapBase -> {
                                    val subTable = slotContent.toSubTable(frameReader)
                                    Insert.Descend(
                                        table = subTable,
                                        ancestors = insert.ancestors + Pair(insert.table, index)
                                    )
                                }
                                is HamtTable.SlotContent.Empty -> {
                                    val revisedTable = insert.table.fillSlot(index, key, value)
                                    Insert.Ascend(revisedTable, insert.ancestors.reversed())
                                }
                                is HamtTable.SlotContent.KeyValue -> {
                                    if (slotContent.key == key) {
                                        val revisedTable = insert.table.fillSlot(index, key, value)
                                        Insert.Ascend(revisedTable, insert.ancestors.reversed())
                                    } else {
                                        val extendedAncestors =
                                            insert.ancestors + Pair(insert.table, index)
                                        val conflictIndices = TrieKey(slotContent.key).toIndices()
                                            .drop(extendedAncestors.size)
                                        val conflict = Conflict(
                                            slotContent.key,
                                            slotContent.value,
                                            conflictIndices
                                        )
                                        Insert.DescendConflicted(
                                            conflict = conflict,
                                            ancestors = extendedAncestors
                                        )
                                    }
                                }
                            }
                        }
                        is Insert.DescendConflicted -> {
                            val conflictIndex = insert.conflict.indices.first()
                            if (conflictIndex == index) {
                                Insert.DescendConflicted(
                                    conflict = insert.conflict,
                                    ancestors = insert.ancestors + Pair(null, index)
                                )
                            } else {
                                val revisedTable = HamtTable.createSub(
                                    setOf(
                                        Triple(index, key, value),
                                        Triple(
                                            conflictIndex,
                                            insert.conflict.key,
                                            insert.conflict.value
                                        )
                                    )
                                )
                                Insert.Ascend(revisedTable, insert.ancestors.reversed())
                            }
                        }
                        is Insert.Ascend -> insert
                    }
                }
            )
            when (insert) {
                is Insert.Descend -> error("Subtable found at final index from key: $key")
                is Insert.DescendConflicted -> error("Hash collision for keys: $key, ${insert.conflict.key}")
                is Insert.Ascend -> {
                    val topTable = insert.rewriteTables.fold(
                        initial = insert.revisedTable,
                        operation = { subTable, (supTable, index) ->
                            TODO()
                        }
                    )
                    TODO()
                }
            }
            TODO()
        } ?: createRootTable(key, value)
    }

    private fun createRootTable(key: Long, value: Long): Long {
        val index = TrieKey(key).toIndices().first()
        val rootTable = HamtTable.createRoot(index, key, value)
        val bytes = rootTable.toBytes()
        return frameWriter.write(bytes)
    }
}

class HamtReader(inputStream: InputStream, private val rootBase: Long?) {

    private val frameReader = FrameReader(inputStream)

    private sealed class Search {
        data class Continue(val table: HamtTable) : Search()
        object Failure : Search()
        data class Success(val value: Long) : Search()
    }

    operator fun get(key: Long): Long? {
        return rootBase?.let {
            val rootTable = HamtTable(frameReader.read(it), HamtTableType.Root)
            val search = TrieKey(key).toIndices().fold(
                initial = Search.Continue(rootTable) as Search,
                operation = { search, index ->
                    when (search) {
                        is Search.Continue -> {
                            when (val slotContent = search.table.getSlotContent(index)) {
                                is HamtTable.SlotContent.MapBase -> {
                                    Search.Continue(slotContent.toSubTable(frameReader))
                                }
                                is HamtTable.SlotContent.KeyValue -> {
                                    if (slotContent.key == key) {
                                        Search.Success(slotContent.value)
                                    } else {
                                        Search.Failure
                                    }
                                }
                                is HamtTable.SlotContent.Empty -> {
                                    Search.Failure
                                }
                            }
                        }
                        is Search.Failure -> search
                        is Search.Success -> search
                    }
                }
            )
            val success = search as? Search.Success
            success?.value
        }
    }
}

class FactWriter {
    private val outputStream = ByteArrayOutputStream()
    private val writer = FrameWriter(outputStream, outputStream.size().toLong())
    private val eavtReader = HamtReader(ByteArrayInputStream(outputStream.toByteArray()), -1)
}


