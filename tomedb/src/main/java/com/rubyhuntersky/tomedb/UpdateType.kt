package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.attributes.GroupedItem
import com.rubyhuntersky.tomedb.attributes.fallbackGroupName
import com.rubyhuntersky.tomedb.attributes.fallbackItemName
import com.rubyhuntersky.tomedb.attributes.toGroupedItemString
import com.rubyhuntersky.tomedb.datalog.Standing

sealed class UpdateType : GroupedItem {

    object Declare : UpdateType()
    object Retract : UpdateType()

    override val groupName: String get() = fallbackGroupName
    override val itemName: String get() = fallbackItemName
    override fun toString(): String = toGroupedItemString()

    fun toStanding(): Standing = when (this) {
        Declare -> Standing.Asserted
        Retract -> Standing.Retracted
    }
}