package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import kotlin.math.absoluteValue


sealed class Ident {

    abstract fun toEnt(): Ent

    abstract fun addParent(parent: Local): Composite
    abstract fun addParents(parents: Composite): Composite

    fun addParent(group: String, index: Long, label: String? = null) = addParent(Local(group, index, label))

    data class Local(val group: String, val index: Long, val label: String? = null) : Ident() {
        override fun toString(): String = "$group${label?.let { ":$label" } ?: ""}-$index"
        override fun toEnt(): Ent {
            var result = label?.hashCode() ?: 0
            result = 31 * result + group.hashCode()
            result = 31 * result + index.hashCode()
            return Ent(result.toLong().absoluteValue)
        }

        override fun addParent(parent: Local) = Composite(parent, this)
        override fun addParents(parents: Composite) = Composite(parents.locals, this)
    }

    data class Composite(val locals: List<Local>) : Ident() {
        constructor(parent: Local, descendant: List<Local>) : this(listOf(parent) + descendant)
        constructor(parent: Local, child: Local) : this(listOf(parent, child))
        constructor(ancestors: List<Local>, descendants: List<Local>) : this(ancestors + descendants)
        constructor(ancestors: List<Local>, child: Local) : this(ancestors + child)

        override fun toString(): String = locals.joinToString(".", transform = Local::toString)
        override fun toEnt(): Ent = locals.asSequence().map(Local::toEnt).fold(Ent(0), Ent::mix)
        override fun addParent(parent: Local) = Composite(parent, locals)
        override fun addParents(parents: Composite) = Composite(parents.locals, locals)
    }

    companion object {
        fun of(attribute: Attribute<*>, index: Long, label: String? = null): Local {
            return Local(attribute.groupName, index, label)
        }

        fun of(group: AttributeGroup, index: Long, label: String? = null): Local {
            return Local(group.groupName, index, label)
        }
    }
}