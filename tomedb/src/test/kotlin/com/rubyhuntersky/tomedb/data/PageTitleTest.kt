package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertNotNull
import org.junit.Test

class PageTitleTest {

    private val norway = Norway.toEnt(0)
    private val citizen17 = Citizen.toEnt(17)

    @Test
    fun childTitle() {
        val topic = TomeTopic.Parent(norway, Citizen.Country)
        val pageTitle = PageTitle.Child(citizen17, topic)
        assertNotNull(pageTitle)
    }

    @Test
    fun traitHolderTitle() {
        val topic = TomeTopic.Trait(Citizen.FullName)
        val pageTitle = PageTitle.TraitHolder(citizen17, "Frankie", topic)
        assertNotNull(pageTitle)
    }

    @Test
    fun entityTitle() {
        val topic = TomeTopic.Entity(citizen17)
        val pageTitle = PageTitle.Entity(citizen17, topic)
        assertNotNull(pageTitle)
    }
}