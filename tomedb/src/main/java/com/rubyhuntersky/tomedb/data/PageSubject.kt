package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent

/**
 * A title is a entity bound to a topic. It represents an entity
 * found to be described by the topic. When a tome is created from
 * a topic, the pages in the tome are indexed by title.
 */
sealed class PageSubject<KeyT : Any> {

    abstract val topic: TomeTopic<KeyT>
    abstract val key: KeyT
    abstract val keyEnt: Ent
    abstract val keyAttr: Attribute<*>?
    abstract val keyValue: Any?

    data class Follower(val follower: Ent, override val topic: TomeTopic.Leader) :
        PageSubject<Ent>() {
        override val key: Ent get() = follower
        override val keyEnt: Ent get() = follower
        override val keyAttr: Attribute<*>? get() = topic.childAttr
        override val keyValue: Any? get() = topic.leader
    }
}