package org.platformlayer.ops.filesystem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.platformlayer.IoUtils;
import org.platformlayer.crypto.CryptoUtils;
import org.platformlayer.crypto.Md5Hash;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

public abstract class SyntheticFile extends ManagedFile {
	static final Logger log = Logger.getLogger(SyntheticFile.class);

	// String contents;

	@Override
	protected Md5Hash getSourceMd5(OpsTarget target) throws OpsException {
		InputStream input = null;
		try {
			input = openSourceStream();
			return CryptoUtils.md5(input);
		} catch (IOException e) {
			throw new OpsException("Error computing hash", e);
		} finally {
			IoUtils.safeClose(input);
		}
	}

	protected abstract byte[] getContentsBytes() throws OpsException;

	// {
	// return getContents().getBytes();
	// }

	protected InputStream openSourceStream() throws OpsException {
		InputStream input = new ByteArrayInputStream(getContentsBytes());
		return input;
	}

	@Override
	protected void uploadFile(OpsTarget target, File remoteFilePath) throws OpsException {
		InputStream sourceStream = null;
		try {
			sourceStream = openSourceStream();

			byte[] data;
			try {
				data = IoUtils.readAllBinary(sourceStream);
			} catch (IOException e) {
				throw new OpsException("Error reading source stream", e);
			}

			// Md5Hash dataHash = CryptoUtils.md5(data);

			// smartGetServer(true).getAgent().uploadFile(bais, dataHash, remoteFilePath, getFileMetadata());
			FileUpload.upload(target, remoteFilePath, data);
		} finally {
			IoUtils.safeClose(sourceStream);
		}
	}

	// public String getContents() throws OpsException {
	// return contents;
	// }
	//
	// public void setContents(String contents) {
	// this.contents = contents;
	// }

	public static String getDefaultResourceName(Class<?> contextClass, File filePath) {
		return getDefaultResourceName(contextClass, filePath.getName());
	}

	public static String getDefaultResourceName(Class<?> contextClass, String name) {
		String templateName = contextClass.getPackage().getName().replace(".", "/");
		templateName += "/" + name;
		return templateName;
	}

}
