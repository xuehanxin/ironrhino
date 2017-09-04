package org.ironrhino.core.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.util.ValueThenKeyComparator;

public interface FileStorage {

	ValueThenKeyComparator<String, Boolean> COMPARATOR = new ValueThenKeyComparator<String, Boolean>() {
		@Override
		protected int compareValue(Boolean a, Boolean b) {
			return b.compareTo(a);
		}
	};

	public default boolean isBucketBased() {
		try {
			return getClass().getMethod("getBucket") != null;
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
	}

	public void write(InputStream is, String path) throws IOException;

	public InputStream open(String path) throws IOException;

	public boolean mkdir(String path) throws IOException;

	public boolean delete(String path) throws IOException;

	public boolean exists(String path) throws IOException;

	public boolean rename(String fromPath, String toPath) throws IOException;

	public boolean isDirectory(String path) throws IOException;

	public long getLastModified(String path) throws IOException;

	public List<String> listFiles(String path) throws IOException;

	public Map<String, Boolean> listFilesAndDirectory(String path) throws IOException;

	public String getFileUrl(String path);

}
