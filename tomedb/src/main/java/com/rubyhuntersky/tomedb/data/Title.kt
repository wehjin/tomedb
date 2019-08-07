package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent

/**
 * A title is a entity bound to a topic. It represents an entity
 * found to be described by the topic. When a tome is created from
 * a topic, the pages in the tome are indexed by title.
 */
data class Title(val ent: Ent, val topic: Topic)