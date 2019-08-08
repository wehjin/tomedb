package com.rubyhuntersky.tomedb.basics

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.AttributeGroup
import kotlin.math.absoluteValue

data class Ent(val long: Long) {
    init {
        require(long == long.absoluteValue)
    }

    fun mix(ent: Ent): Ent = Ent((31 * this.long + ent.long).absoluteValue)

    companion object {

        fun of(group: AttributeGroup, index: Long): Ent {
            return Ident.Local(group.groupName, index).toEnt()
        }

        fun <T : Any> of(attr: Attribute, value: T): Ent {
            return of(attr.attrName, value)
        }

        fun <T : Any> of(attr: Keyword, value: T): Ent {
            return Ent(attr.keywordGroup.hashCode().let { 31 * it + value.hashCode() }.toLong().absoluteValue)
        }
    }
}