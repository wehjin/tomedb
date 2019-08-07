package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Ident
import org.junit.Assert.assertNotNull
import org.junit.Test

class TopicTest {

    @Test
    fun entityIsATopic() {
        val entityTopic = Topic.Entity(Ent(1))
        assertNotNull(entityTopic)
    }

    object Citizen : AttributeGroup {
        object FullName : Attribute {
            override val valueType: ValueType = ValueType.STRING
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The name of a citizen."
        }

        object Country : Attribute {
            override val valueType: ValueType = ValueType.LONG
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The country of residence for a citizen."
        }
    }

    @Suppress("unused")
    object Norway : AttributeGroup {
        object Fjords : Attribute {
            override val valueType: ValueType = ValueType.LONG
            override val cardinality: Cardinality = Cardinality.ONE
            override val description: String = "The number of fjords in a Norway."
        }
    }

    @Test
    fun traitIsATopic() {
        val traitTopic = Topic.Trait(Citizen.FullName)
        assertNotNull(traitTopic)
    }

    @Test
    fun parentIsATopic() {
        val parentTopic = Topic.Parent(Citizen.Country, Ident.of(Norway, 0).toEnt())
        assertNotNull(parentTopic)
    }
}