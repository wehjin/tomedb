package com.rubyhuntersky.tomedb.data

import org.junit.Assert.assertNotNull
import org.junit.Test

class PageSubjectTest {

    private val norway = Norway.toEnt(0)
    private val citizen17 = Citizen.toEnt(17)

    @Test
    fun follower() {
        val topic = TomeTopic.Leader(norway, Citizen.Country)
        val pageTitle = PageSubject.Follower(citizen17, topic)
        assertNotNull(pageTitle)
    }
}