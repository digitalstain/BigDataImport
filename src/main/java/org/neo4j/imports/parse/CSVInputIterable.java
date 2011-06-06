package org.neo4j.imports.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * An Iterable view over an input file.
 */
public class CSVInputIterable implements InputIterable<String>
{
	private final BufferedReader in;
	private final String delimiter;
	private final InputIterator theIterator;

	public CSVInputIterable(BufferedReader reader)
	{
		// We assume in most cases input will be CSV (and friends).
		this(reader, "|,-;");
	}

	public CSVInputIterable(BufferedReader reader, String delimiter)
	{
		this.in = reader;
		this.delimiter = delimiter;
		this.theIterator = new InputIterator();
	}
	
	public Iterator<ParseResult<String>> iterator()
	{
		return theIterator;
	}
	
	/**
	 * An iterator over the contents of a text file that has one line per
	 * (possible) relationship, expressed as one, two or three strings, the first being the
	 * key property of the originating node, the second the key property of
	 * the destination node and a possible third, which is the name of the
	 * RelationshipType for the relationship.
	 * If there is only one string, it is considered a single node entry while
	 * two strings is a relationship. Existence (as in prior encounter) of the
	 * entries is not something we concern ourselves with, it is a problem for
	 * our users.
	 * However, state is kept in three places:
	 * <ol>
	 * <li>The file descriptor for the reader</li>
	 * <li>The internal state of the Buffered reader</li>
	 * <li>The next field of the iterator</li>
	 * </ol>
	 * <p>
	 * The first cannot be avoided given my timebox. The second is 
	 * because of the translation from bytes to Strings, it could be worked
	 * around of course but i am lazy. The third is because of the requirements
	 * of the iterator interface - there has to be a way to deal with multiple 
	 * successive calls to next().
	 */
	private class InputIterator implements Iterator<ParseResult<String>>
	{
		private ParseResult<String> next;
		
		public InputIterator()
		{
			prepareNext();
		}

		@Override
		public boolean hasNext()
		{
			return next != null;
		}

		/**
		 * Here we want to be forgiving - this is a long running operation so
		 * if for some reason there is an error in the input (such as 1 field)
		 * we try not to die - actually, the only reason to stop is an IO error.
		 * The convention is that if 
		 */
		@Override
		public ParseResult<String> next()
		{
			ParseResult<String> toReturn = next;
			if (toReturn == null)
			{
				throw new NoSuchElementException();
			}
			prepareNext();
			return toReturn;
		}

		private void prepareNext()
		{
			String nextLine = null;
			try
			{	
				nextLine = in.readLine();
				if (nextLine == null)
				{
					next = null;
					return;
				}
			}
			catch (IOException e)
			{
				next = null;
			}
			// Just in case someone wants to tokenize on whitespace
			StringTokenizer tokenizer = delimiter == null ? 
					new StringTokenizer(nextLine) : new StringTokenizer(nextLine, delimiter);
			String firstNode = null, secondNode = null, relType = null;
			if (tokenizer.hasMoreTokens())
			{
				firstNode = tokenizer.nextToken();
			}
			if (tokenizer.hasMoreTokens())
			{
				secondNode = tokenizer.nextToken();
			}
			if (tokenizer.hasMoreTokens())
			{
				relType = tokenizer.nextToken();
			}
			next = new ParseResult<String>(firstNode, secondNode, relType);
		}
		
		/**
		 * Whatever. I will never understand what remove has to do with anything in the
		 * <i>iterator</i> interface.
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Read only iterator");
		}
	}
}
