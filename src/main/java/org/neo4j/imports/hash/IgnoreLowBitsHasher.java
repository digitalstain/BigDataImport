package org.neo4j.imports.hash;

public class IgnoreLowBitsHasher implements Hasher
{

    @Override
    public int hashFor( Object toHash )
    {
        int value = 0;
        boolean done = false;
        if ( toHash instanceof String )
        {
            try
            {
                value = Integer.parseInt( (String) toHash );
                done = true;
            }
            catch ( NumberFormatException e )
            {
            }
        }
        if ( !done )
        {
            value = toHash.hashCode();
        }
        return ( value & ( -1 ^ ( ( 1 << 13 ) - 1 ) ) ) >> 13;
    }

}
