package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import com.rubyhuntersky.tomedb.attributes.attrName
import com.rubyhuntersky.tomedb.attributes.groupName
import kotlin.math.absoluteValue

data class Ent(val number: Long) {
    init {
        require(number == number.absoluteValue)
    }

    fun mix(ent: Ent): Ent = Ent((31 * this.number + ent.number).absoluteValue)

    companion object {

        @Deprecated("No replacement")
        fun of(group: AttributeGroup, index: Long): Ent {
            return Ident.Local(group.groupName, index).toEnt()
        }

        @Deprecated("No replacement")
        fun <T : Any> of(attr: Attribute<*>, value: T): Ent {
            return of(attr.attrName, value)
        }

        @Deprecated("No replacement")
        fun <T : Any> of(attr: Keyword, value: T): Ent {
            return Ent(attr.keywordGroup.hashCode().let { 31 * it + value.hashCode() }.toLong().absoluteValue)
        }
    }
}