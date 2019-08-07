package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent
import org.junit.Assert.assertNotNull
import org.junit.Test

class TopicTest {

    @Test
    fun entityIsATopic() {
        val entityTopic = Topic.Entity(Ent(1))
        assertNotNull(entityTopic)
    }

    @Test
    fun traitIsATopic() {
        val traitTopic = Topic.Trait(Citizen.FullName)
        assertNotNull(traitTopic)
    }

    @Test
    fun parentIsATopic() {
        val parentTopic = Topic.Parent(Norway.toEnt(0), Citizen.Country)
        assertNotNull(parentTopic)
    }
}