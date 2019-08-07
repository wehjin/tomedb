package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.basics.Ent

/**
 * A title is a entity bound to a topic. It represents an entity
 * found to be described by the topic. When a tome is created from
 * a topic, the pages in the tome are indexed by title.
 */
sealed class PageTitle {

    abstract val topic: TomeTopic

    data class Entity(val entityEnt: Ent, override val topic: TomeTopic.Entity) : PageTitle()
    data class Child(val childEnt: Ent, override val topic: TomeTopic.Parent) : PageTitle()
    data class TraitHolder(val holderEnt: Ent, val traitValue: Any, override val topic: TomeTopic.Trait) : PageTitle()
}