package org.platformlayer.ops.filesystem;

public class FileAccess {
	public static final FileAccess ROOT_OWNED_READABLE_DIRECTORY = new FileAccess("0755", "root", "root");
	public static final FileAccess ROOT_CAN_EXECUTE = new FileAccess("0700", "root", "root");

	public final String mode;
	public final String owner;
	public final String group;

	public FileAccess(String mode, String owner, String group) {
		this.mode = mode;
		this.owner = owner;
		this.group = group;
	}

	public FileAccess(String mode) {
		this(mode, null, null);
	}

}
