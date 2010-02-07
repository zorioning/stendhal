/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package games.stendhal.common.resource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author silvio
 */
public class File implements Resource
{
	java.io.File mFile = null;
	String       mPath = null;

	public File(String pathName, boolean fromClassPath)
	{
		assert pathName != null;
		
		if(!fromClassPath)
			mFile = new java.io.File(pathName);

		mPath = pathName;
	}

	public InputStream getInputStream()
	{
		try
		{
			if(mFile != null)
				return new FileInputStream(mFile);

			return this.getClass().getResourceAsStream("/" + mPath);
		}
		catch (FileNotFoundException exception)
		{
			assert false: exception;
		}

		return null;
	}

	public String getURI()
	{
		if(mFile != null)
			return "file://" + mFile.getAbsolutePath();

		return mPath;
	}

	public boolean exists()
	{
		if(mFile != null)
			return mFile.exists();

		InputStream stream = this.getClass().getResourceAsStream("/" + mPath);

		try
		{
			stream.close();
		}
		catch(IOException exception)
		{
			assert false: exception;
			stream = null;
		}

		return stream != null;
	}
}
