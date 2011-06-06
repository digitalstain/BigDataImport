package org.neo4j.imports.hash;

import java.util.HashMap;

import org.neo4j.imports.parse.InputIterable;
import org.neo4j.imports.parse.ParseResult;

public class TrainableTwoStreamHasher implements Hasher
{
    private final HashMap<Object, Integer> hash;

    public TrainableTwoStreamHasher( InputIterable<String> trainData )
    {
        hash = new HashMap<Object, Integer>();
        train( trainData );
    }

    private void train( InputIterable<String> trainData )
    {
        int firstHash = 1;
        int secondHash = -1;
        int firstInsertCount = 0;
        int secondInsertCount = 0;

        WrappedObject first, second;

        for ( ParseResult<String> row : trainData )
        {
            first = new WrappedObject( row.getFirstNode() );
            second = new WrappedObject( row.getSecondNode() );

            if ( !hash.containsKey( first ) )
            {
                if ( firstInsertCount > 10000 )
                {
                    firstInsertCount = 0;
                    firstHash++;
                }
                else
                {
                    firstInsertCount++;
                }
                hash.put( first, firstHash );
            }
            if ( !hash.containsKey( second ) )
            {
                if ( secondInsertCount > 10000 )
                {
                    secondInsertCount = 0;
                    secondHash--;
                }
                else
                {
                    secondInsertCount++;
                }
                hash.put( second, secondHash );
            }
        }
    }

    @Override
    public int hashFor( Object toHash )
    {
        Integer result = hash.get( new WrappedObject( toHash ) );
        if (result == null)
        {
            return 0;
        }
        return result;
    }

    private static final class WrappedObject
    {
        final Object wrapped;

        public WrappedObject( Object toWrap )
        {
            this.wrapped = toWrap;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( obj == null )
            {
                return false;
            }
            if ( obj == this )
            {
                return true;
            }
            if ( !( obj instanceof WrappedObject ) )
            {
                return false;
            }
            WrappedObject other = (WrappedObject) obj;
            return this.hashCode() == other.hashCode();
        }

        @Override
        public int hashCode()
        {
            int value = wrapped.hashCode();
            return ( value & ( -1 ^ ( ( 1 << 13 ) - 1 ) ) ) >> 13;
        }

        @Override
        public String toString()
        {
            return wrapped.toString();
        }
    }

}
