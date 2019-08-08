package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Keyword

data class Projection<out T : Any>(val ent: Long, val attr: Keyword, val value: T)