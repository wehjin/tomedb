package com.rubyhuntersky.tomedb.webcore

import com.rubyhuntersky.tomedb.basics.Keyword
import com.rubyhuntersky.tomedb.datalog.Datalist

interface Datacache {
    fun liftEntToHeight(ent: Long, height: Long): Datalist
    fun liftAttrToHeight(attr: Keyword, height: Long): Datalist
    fun toDatalist(): Datalist
}

