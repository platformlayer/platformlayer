package org.platformlayer.ops.backups;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

public class DirectoryBackup extends Backup {
	public File rootDirectory;
	public List<File> exclude = Lists.newArrayList();
}