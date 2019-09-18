package com.rubyhuntersky.tomedb.datalog.pile

import com.rubyhuntersky.tomedb.TempDirFixture
import com.rubyhuntersky.tomedb.basics.bytesFromLong
import com.rubyhuntersky.tomedb.basics.longFromBytes
import com.rubyhuntersky.tomedb.datalog.framing.FrameReader
import com.rubyhuntersky.tomedb.datalog.framing.FrameWriter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class PileTest {

    @Test
    fun basic() {
        val framesFile = TempDirFixture.initDir("basicPileTest").toFile()
            .also { it.mkdirs() }
            .let { File(it, "frames") }

        val testValues = 0L..101L
        val pileWriter = PileWriter(
            topBase = null,
            frameWriter = FrameWriter.new(framesFile, 0)
        )
        val topBase = testValues.fold(-1L) { _, next -> pileWriter.write(bytesFromLong(next)) }
        val pileReader = PileReader(
            topBase = topBase,
            frameReader = FrameReader.new(framesFile)
        )
        val pileValues = pileReader.read().map(::longFromBytes).toList()
        assertEquals(testValues.reversed().toList(), pileValues)
    }
}