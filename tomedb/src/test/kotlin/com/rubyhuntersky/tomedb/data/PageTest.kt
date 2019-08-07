package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PageTest {

    @Test
    fun composeFromTitleAndLines() {
        val norway = Norway.toEnt(0)
        val citizen17 = Citizen.toEnt(17)

        val citizen17Page = pageOf(
            Title(citizen17, Topic.Parent(norway, Citizen.Country)),
            listOf(
                Citizen.Country to norway,
                Citizen.FullName to "Benjy"
            )
        )
        assertEquals(
            Title(citizen17, Topic.Parent(norway, Citizen.Country)),
            citizen17Page.pageTitle
        )
        assertEquals(
            norway,
            citizen17Page(Citizen.Country)
        )
        assertEquals("Benjy", citizen17Page(Citizen.FullName))
    }
}