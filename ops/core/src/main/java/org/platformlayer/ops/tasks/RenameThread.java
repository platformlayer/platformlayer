package org.platformlayer.ops.tasks;

import java.io.Closeable;
import java.io.IOException;

public class RenameThread implements Closeable {

	private String oldName;

	public RenameThread(String name) {
		this.oldName = Thread.currentThread().getName();
		Thread.currentThread().setName(name);
	}

	@Override
	public void close() throws IOException {
		if (oldName != null) {
			Thread.currentThread().setName(oldName);
			oldName = null;
		}
	}
}
