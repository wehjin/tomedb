package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.Cardinality
import com.rubyhuntersky.tomedb.attributes.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IdentTest {

    enum class Counter : Attribute {
        COUNT {
            override val valueType = ValueType.LONG
            override val cardinality = Cardinality.ONE
            override val description = "The count of a counter."
        };

        companion object : AttributeGroup
    }

    @Test
    fun createFromAttribute() {
        val ident = Ident.of(Counter.COUNT, 0)
        assertEquals(Ident.Local(Counter.groupName, 0), ident)
    }

    @Test
    fun createFromGroup() {
        val ident = Ident.of(Counter, 0)
        assertEquals(Ident.Local("Counter", 0), ident)
    }

    @Test
    fun differentIndexProducesDifferentEnts() {
        val ident1 = Ident.of(Counter, 0)
        val ident2 = Ident.of(Counter, 1)
        assertNotEquals(ident1.toEnt(), ident2.toEnt())
    }

    @Test
    fun localProducesDashString() {
        val ident = Ident.of(Counter, 0)
        assertEquals("Counter-0", ident.toString())
    }

    @Test
    fun compositeProducesDotString() {
        val ident = Ident.of(Counter, 0).addParent("Parent", 0)
        assertEquals("Parent-0.Counter-0", ident.toString())
    }

    @Test
    fun localEntMatchesSingleCompositeEnt() {
        val local = Ident.Local("A", 0)
        val composite = Ident.Composite(listOf(local))
        assertEquals(local.toEnt(), composite.toEnt())
    }

    @Test
    fun parentEntDiffersFromChildEnt() {
        val child = Ident.Local("Child", 0)
        val parent = child.addParent("Parent", 0)
        assertNotEquals(parent.toEnt(), child.toEnt())
    }

    @Test
    fun addingParentsInBatchIsSameAsAddingSeparately() {
        val child = Ident.Local("Child", 0)
        val parent = Ident.Local("Parent", 0)
        val grandparent = Ident.Local("Grandparent", 0)
        val ancestors = Ident.Composite(grandparent, parent)
        val batch = child.addParents(ancestors)
        val individual = child.addParent(parent).addParent(grandparent)
        assertEquals(batch.toEnt(), individual.toEnt())
    }
}