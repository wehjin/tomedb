package com.rubyhuntersky.tomedb

import com.rubyhuntersky.tomedb.basics.Keyword

data class Update(
    val ent: Long,
    val attr: Keyword,
    val value: Any,
    val action: UpdateType = UpdateType.Declare
)
