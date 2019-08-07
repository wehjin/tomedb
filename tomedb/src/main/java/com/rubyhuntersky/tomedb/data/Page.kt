package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Keyword


data class Page<out TitleT : Any>(
    private val map: Map<Keyword, Projection<*>>,
    private val titleAttr: Keyword? = null
)