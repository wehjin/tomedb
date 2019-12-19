package com.rubyhuntersky.tomedb.webcore

import com.rubyhuntersky.tomedb.datalog.Datalist

interface Datacache {
    fun liftEntToHeight(ent: Long, height: Long) : Boolean
    fun toDatalist(): Datalist
}

