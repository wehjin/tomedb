package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PageTest {

    private val norway = Norway.toEnt(0)
    private val citizen17 = Citizen.toEnt(17)

    @Test
    fun composeFromTitleAndLines() {
        val topic = TomeTopic.Parent(norway, Citizen.Country)
        val page = pageOf(
            title = PageTitle.Child(citizen17, topic),
            lines = setOf(
                Citizen.Country to norway,
                Citizen.FullName to "Benjy"
            )
        )
        assertEquals(PageTitle.Child(citizen17, topic), page.title)
        assertEquals(norway, page(Citizen.Country))
        assertEquals("Benjy", page(Citizen.FullName))
    }
}