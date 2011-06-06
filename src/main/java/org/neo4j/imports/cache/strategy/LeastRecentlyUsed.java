package org.neo4j.imports.cache.strategy;


public class LeastRecentlyUsed<K> implements ReplacementStrategy<K>
{
    private final Node<K> head = new Node<K>();
    private Node<K> lru = head;

    @Override
    public void hit( K key )
    {
        if ( head.next != null && key.equals( head.next.content ) )
        {
            // Already the mru, nothing to do
            return;
        }
        // System.out.print( "LRU hit on key " + key );
        Node<K> previous = findPrevious( key );
        if ( previous == null )
        {
            Node<K> toInsert = new Node<K>();
            toInsert.content = key;
            toInsert.next = head.next;
            head.next = toInsert;
            if ( lru == head )
            {
                lru = toInsert;
            }
            // System.out.println( " inserted as new" );
        }
        else
        {
            Node<K> actual = previous.next;
            previous.next = actual.next;
            actual.next = head.next;
            head.next = actual;
            if ( lru == actual )
            {
                lru = previous;
            }
            // System.out.println( " was already in, moved to mru" );
        }
    }

    @Override
    public void remove( K key )
    {
        Node<K> previous = findPrevious( key );
        if ( previous == null )
        {
            return;
        }
        Node<K> actual = previous.next;
        previous.next = actual.next;
        if ( lru == actual )
        {
            lru = previous;
        }
    }

    @Override
    public void evict( K key )
    {
        remove( key );
    }

    @Override
    public K suggest()
    {
        return lru.content;
    }

    /*
     * If this returns null then the key was not found
     * Else it returns the previous node to the one it hosts it.
     */
    private Node<K> findPrevious( K forKey )
    {
        Node<K> current = head;
        while ( true )
        {
            if ( current.next == null )
            {
                return null;
            }
            if ( current.next.content.equals( forKey ) )
            {
                break;
            }
            current = current.next;
        }
        return current;
    }

    private class Node<K>
    {
        Node<K> next;
        K content;
    }
}
