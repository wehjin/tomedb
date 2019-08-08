package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TomeTest {

    @Test
    fun composeFromPages() {
        val norway = Norway.toEnt(0)
        val citizen17 = Citizen.toEnt(17)

        val topic = TomeTopic.Parent(norway, Citizen.Country)
        val title1 = PageTitle.Child(citizen17, topic)
        val page1 = pageOf(
            title = title1,
            lines = setOf(Citizen.Country to norway, Citizen.FullName to "Benjy")
        )
        val tome = tomeOf(topic, setOf(page1))
        assertEquals(topic, tome.topic)
        assertEquals(page1, tome(title1))
    }
}