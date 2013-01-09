package org.platformlayer.service.jetty.ops;

import java.io.File;
import java.io.IOException;

import org.platformlayer.ops.Bound;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.DownloadFileByHash;
import org.platformlayer.ops.filesystem.ManagedSymlink;
import org.platformlayer.ops.filesystem.TemplatedFile;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleApp extends OpsTreeBase {

	private static final Logger log = LoggerFactory.getLogger(SimpleApp.class);

	public String key;
	public String source;

	@Bound
	JettyTemplate template;

	@Handler
	public void handler(OpsTarget target) throws IOException, OpsException {
	}

	@Override
	protected void addChildren() throws OpsException {
		DownloadFileByHash download = addChild(buildDownload());
		File deployed = new File(template.getWarsDeployDir(), getWarName());
		addChild(ManagedSymlink.build(deployed, download.filePath));

		File contextDir = template.getContextDir();
		addChild(TemplatedFile.build(template, new File(contextDir, "context.xml")));
	}

	protected DownloadFileByHash buildDownload() {
		DownloadFileByHash download = injected(DownloadFileByHash.class);
		download.filePath = new File(template.getWarsStagingDir(), getWarName());
		download.specifier = source;

		return download;
	}

	private String getWarName() {
		return key + ".war";
	}

}
