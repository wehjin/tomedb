package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent
import org.junit.Assert.assertNotNull
import org.junit.Test

class TomeTopicTest {

    @Test
    fun entityIsATopic() {
        val entityTopic = TomeTopic.Entity(Ent(1))
        assertNotNull(entityTopic)
    }

    @Test
    fun traitIsATopic() {
        val traitTopic = TomeTopic.Trait<String>(Citizen.FullName)
        assertNotNull(traitTopic)
    }

    @Test
    fun parentIsATopic() {
        val parentTopic = TomeTopic.Leader(Norway.toEnt(0), Citizen.Country)
        assertNotNull(parentTopic)
    }
}