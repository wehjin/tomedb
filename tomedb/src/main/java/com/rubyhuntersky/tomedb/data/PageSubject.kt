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
    abstract val keyAttr: Attribute?
    abstract val keyValue: Any?

    data class Entity(val ent: Ent) : PageSubject<Ent>() {
        override val topic: TomeTopic.Entity = TomeTopic.Entity(ent)
        override val key: Ent get() = ent
        override val keyEnt: Ent get() = ent
        override val keyAttr: Attribute? get() = null
        override val keyValue: Any? get() = null
    }

    data class Follower(val follower: Ent, override val topic: TomeTopic.Leader) :
        PageSubject<Ent>() {
        override val key: Ent get() = follower
        override val keyEnt: Ent get() = follower
        override val keyAttr: Attribute? get() = topic.childAttr
        override val keyValue: Any? get() = topic.leader
    }

    data class TraitHolder<TraitT : Any>(
        val traitHolder: Ent,
        val traitValue: TraitT,
        override val topic: TomeTopic.Trait<TraitT>
    ) : PageSubject<TraitT>() {
        override val key: TraitT get() = traitValue
        override val keyEnt: Ent get() = traitHolder
        override val keyAttr: Attribute? get() = topic.attr
        override val keyValue: Any? get() = traitValue
    }
}