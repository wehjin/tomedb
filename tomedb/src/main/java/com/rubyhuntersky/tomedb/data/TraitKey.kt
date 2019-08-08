package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent

data class TraitKey<TraitT : Any>(val ent: Ent, val trait: TraitT)