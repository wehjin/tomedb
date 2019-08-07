package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent
import com.rubyhuntersky.tomedb.basics.Keyword

data class Tome<out TitleT : Any>(
    private val map: Map<Ent, Page<TitleT>>,
    private val titleAttr: Keyword? = null
)