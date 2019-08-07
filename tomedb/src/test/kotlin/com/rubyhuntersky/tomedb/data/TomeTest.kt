package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TomeTest {

    @Test
    fun composeFromPages() {
        val norway = Norway.toEnt(0)
        val citizen17 = Citizen.toEnt(17)

        val topic = TomeTopic.Parent(norway, Citizen.Country)
        val page = pageOf(
            PageTitle(citizen17, topic),
            setOf(
                Citizen.Country to norway,
                Citizen.FullName to "Benjy"
            )
        )
        val tome = tomeOf(setOf(page))
        assertEquals(topic, tome.tomeTopic)
        assertEquals(page, tome(tome.pageTitles.first()))
    }
}