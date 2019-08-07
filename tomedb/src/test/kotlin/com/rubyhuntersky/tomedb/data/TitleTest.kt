package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertNotNull
import org.junit.Test

class TitleTest {

    @Test
    fun titleIsComposedOfEntAndTopic() {
        val norway = Norway.toEnt(0)
        val norwayCitizens = Topic.Parent(norway, Citizen.Country)
        val citizen17 = Citizen.toEnt(17)
        val citizen17OfNorway = Title(citizen17, norwayCitizens)
        assertNotNull(citizen17OfNorway)
    }
}