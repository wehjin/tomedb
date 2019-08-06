package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword


data class Page<out TitleT : Any>(
    private val map: Map<Keyword, Projection<*>>,
    private val titleAttr: Keyword? = null
)

data class Tome<out TitleT : Any>(
    private val map: Map<Ent, Page<TitleT>>,
    private val titleAttr: Keyword? = null
)

data class Projection<out T : Any>(val ent: Long, val attr: Keyword, val value: T) {

    fun toEntValue(): EntValue<T> = EntValue(this.ent, this.value)
}