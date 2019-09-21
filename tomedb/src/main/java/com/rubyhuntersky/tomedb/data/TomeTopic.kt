package com.rubyhuntersky.tomedb.data

import com.rubyhuntersky.tomedb.attributes.Attribute
import com.rubyhuntersky.tomedb.basics.Ent


/**
 * A topic describes a set of entities in the database.
 */
sealed class TomeTopic<KeyT : Any> {

    abstract fun toSubject(key: KeyT): PageSubject<KeyT>

    /**
     * A topic composed of entities with a common parent. For example, if
     * the parent is (Norway, Citizen/Country), then the topic describes
     * all entities with a Norway value at Citizen/Country.
     */
    data class Leader(val leader: Ent, val childAttr: Attribute) : TomeTopic<Ent>() {
        override fun toSubject(key: Ent): PageSubject.Follower {
            return PageSubject.Follower(follower = key, topic = this)
        }
    }
}