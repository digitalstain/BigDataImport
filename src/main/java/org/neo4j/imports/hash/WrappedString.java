package org.neo4j.imports.hash;

public final class WrappedString
{
    final String wrapped;

    static Hasher hasher = new IgnoreLowBitsHasher();

    public static void setHasher( Hasher toUse )
    {
        hasher = toUse;
    }

    public WrappedString( String toWrap )
    {
        this.wrapped = toWrap;
    }

    @Override
    public boolean equals( Object obj )
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof WrappedString))
        {
            return false;
        }
        WrappedString other = (WrappedString) obj;
        return this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode()
    {
        return hasher.hashFor( wrapped );
    }

    @Override
    public String toString()
    {
        return wrapped;
    }
}