package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.*
import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Standing

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
        override val groupName: String
            get() = fallbackGroupName
        override val itemName: String
            get() = fallbackItemName

        object Declare : Action()
        object Retract : Action()

        override fun toString(): String = toGroupedItemString()

        fun toStanding(): Standing = when (this) {
            Declare -> Standing.Asserted
            Retract -> Standing.Retracted
        }
    }
}