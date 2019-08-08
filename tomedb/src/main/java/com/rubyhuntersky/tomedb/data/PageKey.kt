package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent

data class PageKey<KeyT : Any>(val ent: Ent, val value: KeyT)