package com.rubyhuntersky.tomedb.datalog.pile

import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.basics.longFromBytes
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PileTest {

    @Test
    fun basic() {
        val testValues = 0L..101L
        val outputStream = ByteArrayOutputStream(100)
        val pileWriter = PileWriter(
            topBase = null,
            frameWriter = FrameWriter(outputStream, 0)
        )
        val topBase = testValues.fold(-1L) { _, next -> pileWriter.write(bytesFromLong(next)) }
        val pileReader = PileReader(
            topBase = topBase,
            frameReader = FrameReader(ByteArrayInputStream(outputStream.toByteArray()))
        )
        val pileValues = pileReader.read().map(::longFromBytes).toList()
        assertEquals(testValues.reversed().toList(), pileValues)
    }
}