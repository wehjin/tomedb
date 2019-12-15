package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.toEnt
import org.junit.Assert.assertNotNull
import org.junit.Test

class TomeTopicTest {

    @Test
    fun parentIsATopic() {
        val parentTopic = TomeTopic.Leader(Norway.toEnt(0), Citizen.Country)
        assertNotNull(parentTopic)
    }
}