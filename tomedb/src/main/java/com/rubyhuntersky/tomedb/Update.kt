package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.GroupedItem
import com.rubyhuntersky.tomedb.basics.Keyword

data class Update(
    val entity: Long,
    val attr: Keyword,
    val value: Any,
    val action: Action = Action.Declare
) {
    constructor(
        entity: Long,
        attr: Attribute<*>,
        value: Any,
        action: Action = Action.Declare
    ) : this(entity, attr.attrName, value, action)

    fun retract(): Update = copy(action = Action.Retract)

    sealed class Action : GroupedItem {
        object Declare : Action()
        object Retract : Action()

        override fun toString(): String = toGroupedItemString()
    }
}