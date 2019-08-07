package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.attributes.GroupedItem
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.basics.Value

data class Update(
    val entity: Long,
    val attr: Keyword,
    val value: Value<*>,
    val action: Action = Action.Declare
) {
    constructor(
        entity: Long,
        attr: Attribute,
        value: Value<*>,
        action: Action = Action.Declare
    ) : this(entity, attr.attrName, value, action)

    sealed class Action : GroupedItem {
        object Declare : Action()
        object Retract : Action()

        override fun toString(): String = toGroupedItemString()

        companion object {
            fun valueOf(assert: Boolean) = if (assert) Declare else Retract
        }
    }
}