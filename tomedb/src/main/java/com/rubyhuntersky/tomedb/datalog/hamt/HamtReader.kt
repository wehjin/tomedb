package com.rubyhuntersky.tomedb.datalog.hamt

import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import java.io.BufferedInputStream
import java.io.InputStream

class HamtReader(inputStream: InputStream, private val rootBase: Long?) {

    private val frameReader = FrameReader(BufferedInputStream(inputStream))

    private sealed class Search {
        data class Continue(val table: HamtTable) : Search()
        object Failure : Search()
        data class Success(val value: Long) : Search()
    }

    operator fun get(key: Long): Long? {
        return rootBase?.let {
            val rootTable = HamtTable.fromRootBytes(frameReader.read(it))
            val search = Hamt.toIndices(key).fold(
                initial = Search.Continue(rootTable) as Search,
                operation = { search, index ->
                    when (search) {
                        is Search.Continue -> {
                            when (val slotContent = search.table.getSlot(index)) {
                                is HamtTable.Slot.MapBase -> {
                                    Search.Continue(
                                        slotContent.toSubTable(frameReader)
                                    )
                                }
                                is HamtTable.Slot.KeyValue -> {
                                    if (slotContent.key == key) {
                                        Search.Success(
                                            slotContent.value
                                        )
                                    } else {
                                        Search.Failure
                                    }
                                }
                                is HamtTable.Slot.Empty -> {
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