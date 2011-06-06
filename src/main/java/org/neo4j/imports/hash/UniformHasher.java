package org.neo4j.imports.hash;

public class UniformHasher implements Hasher
{

    private final int mask;

    public UniformHasher()
    {
        this( Integer.SIZE );
    }

    public UniformHasher( int bitsToKeep )
    {
        if ( bitsToKeep > Integer.SIZE || bitsToKeep < 0 )
        {
            throw new IllegalArgumentException(
                    "bits to keep can be from 0 to " + Integer.SIZE
                    + " (inclusive)" );
        }
        if ( bitsToKeep == Integer.SIZE )
        {
            mask = -1;
        }
        else
        {
            mask = ( ( 1 << ( bitsToKeep ) ) - 1 );
        }
    }

    @Override
    public int hashFor( Object toHash )
    {
        /*
         * Shamelessly stolen from Cliff Click's Lock free hash map code.
         */
        int h = toHash.hashCode();
        h += ( h << 15 ) ^ 0xffffcd7d;
        h ^= ( h >>> 10 );
        h += ( h << 3 );
        h ^= ( h >>> 6 );
        h += ( h << 2 ) + ( h << 14 );
        return ( h ^ ( h >>> 16 ) ) & mask;
    }
}
