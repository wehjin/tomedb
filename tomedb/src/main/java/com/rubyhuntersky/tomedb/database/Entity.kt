package com.rubyhuntersky.tomedb.database

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.data.Page
import java.util.*

class Entity(val page: Page<Date>, private val keyAttr: Attribute) {

    val key: Date
        get() = this(keyAttr)!!

    inline operator fun <reified T : Any> invoke(attr: Attribute): T? {
        return page[attr] as? T
    }
}