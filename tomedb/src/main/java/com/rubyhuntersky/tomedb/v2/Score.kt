package com.rubyhuntersky.tomedb.v2

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Standing
import java.util.*

data class Score(
    val ent: Long,
    val attr: Keyword,
    val quant: String,
    val time: Date,
    val standing: Standing
)
