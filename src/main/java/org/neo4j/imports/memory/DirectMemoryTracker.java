package org.neo4j.imports.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class DirectMemoryTracker
{
	public static final long TotalMachineMemory;
	
	private static OperatingSystemMXBean OsBean;
	private static Method getTotalPhysicalMemorySize;
	private static Method getFreePhysicalMemorySize;

	static
	{
		long mem = -1;
		OsBean = ManagementFactory.getOperatingSystemMXBean();
		try
		{
	        Class<?> osBeanClass = Class.forName( "com.sun.management.OperatingSystemMXBean" );
	        getTotalPhysicalMemorySize = osBeanClass.getMethod( "getTotalPhysicalMemorySize" );
	        getFreePhysicalMemorySize  = osBeanClass.getMethod( "getFreePhysicalMemorySize" );
	        mem = (Long) getTotalPhysicalMemorySize.invoke(OsBean);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			getTotalPhysicalMemorySize = null;
			getFreePhysicalMemorySize = null;
		}
		TotalMachineMemory = mem;
	}
	
	public static long getFreeMemorySize()
	{
		long result = -1;
		try {
			result = (Long) getFreePhysicalMemorySize.invoke(OsBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private long mark = -1;

	public long setMark()
	{
		try {
			mark = (Long) getFreePhysicalMemorySize.invoke(OsBean);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mark;
	}
	
	public long getMark()
	{
		return mark;
	}
	
	public long diff()
	{
		long now = mark;
		try {
			now = (Long) getFreePhysicalMemorySize.invoke(OsBean);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return now - mark;
	}
}
