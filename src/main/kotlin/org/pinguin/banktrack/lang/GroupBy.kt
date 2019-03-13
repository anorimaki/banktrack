package org.pinguin.banktrack.lang



interface Group<T> {
    fun add( element: T ): Boolean
}


open class GroupBy<K, T, C: Group<T>>( private val items: MutableMap<K, C>,
                                       private val collectionFactory: () -> C,
                                       private val keySelector: (T) -> K ): Group<T>, Map<K, C> by items {

    constructor( collectionFactory: () -> C, keySelector: (T) -> K ):
            this( mutableMapOf(), collectionFactory, keySelector )

    override fun add( element : T ): Boolean {
        val key = keySelector(element)
        var coll = items[key]
        if ( coll == null ) {
            coll = collectionFactory()
            items[key] = coll
        }
        coll.add(element)
        return true
    }
}

class GroupListAdaptor<T>( list: MutableList<T> ): Group<T>, MutableList<T> by list {
    constructor(): this( mutableListOf() )
}

