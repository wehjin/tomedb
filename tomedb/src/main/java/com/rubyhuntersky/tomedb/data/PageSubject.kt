package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent

/**
 * A title is a entity bound to a topic. It represents an entity
 * found to be described by the topic. When a tome is created from
 * a topic, the pages in the tome are indexed by title.
 */
sealed class PageSubject<KeyT : Any> {

    abstract val topic: TomeTopic<KeyT>
    abstract val dataEnt: Ent
    abstract val dataKey: KeyT

    data class Entity(val ent: Ent) : PageSubject<Ent>() {
        override val topic: TomeTopic.Entity = TomeTopic.Entity(ent)
        override val dataEnt: Ent get() = ent
        override val dataKey: Ent get() = ent
    }

    data class Follower(val follower: Ent, override val topic: TomeTopic.Leader) : PageSubject<Ent>() {
        override val dataEnt: Ent get() = follower
        override val dataKey: Ent get() = follower
    }

    data class TraitHolder<TraitT : Any>(
        val traitHolder: Ent,
        val traitValue: TraitT,
        override val topic: TomeTopic.Trait<TraitT>
    ) : PageSubject<TraitT>() {
        override val dataEnt: Ent get() = traitHolder
        override val dataKey: TraitT get() = traitValue
    }
}