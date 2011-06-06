package org.neo4j.imports.hash;

import java.util.HashMap;

import org.junit.Test;

public class TestWrappedString
{
    @Test
    public void testInMaps()
    {
        HashMap<WrappedString, Object> map = new HashMap<WrappedString, Object>();
        WrappedString first = new WrappedString("123456");
        System.out.println( "123456789".hashCode() & 0x3c );
        System.out.println( "223456789".hashCode() & 0x3c );
    }

}
