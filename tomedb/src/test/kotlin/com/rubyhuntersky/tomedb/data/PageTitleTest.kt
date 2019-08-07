package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertNotNull
import org.junit.Test

class PageTitleTest {

    @Test
    fun titleIsComposedOfEntAndTopic() {
        val norway = Norway.toEnt(0)
        val norwayCitizens = TomeTopic.Parent(norway, Citizen.Country)
        val citizen17 = Citizen.toEnt(17)
        val citizen17OfNorway = PageTitle(citizen17, norwayCitizens)
        assertNotNull(citizen17OfNorway)
    }
}