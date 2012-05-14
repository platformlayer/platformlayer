package org.platformlayer.ops;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.openstack.utils.Utf8;

public class FileUpload {
	public File path;
	public DataSource data;
	public String mode = "0600";

	static interface DataSource {
		InputStream getInputStream() throws IOException;

		long getLength();

	}

	static class ByteArrayDataSource implements DataSource {
		final byte[] data;

		public ByteArrayDataSource(byte[] data) {
			this.data = data;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(data);
		}

		@Override
		public long getLength() {
			return data.length;
		}
	}

	static class FileDataSource implements DataSource {
		final File file;

		public FileDataSource(File file) {
			this.file = file;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new FileInputStream(file);
		}

		@Override
		public long getLength() {
			return file.length();
		}
	}

	public static void upload(OpsTarget target, File path, String data) throws OpsException {
		FileUpload upload = build(data);
		upload.path = path;
		target.doUpload(upload);
	}

	public static void upload(OpsTarget target, File path, byte[] data) throws OpsException {
		FileUpload upload = new FileUpload();
		upload.path = path;
		upload.data = new ByteArrayDataSource(data);
		target.doUpload(upload);
	}

	public static void upload(OpsTarget target, File path, File srcFile) throws OpsException {
		FileUpload upload = new FileUpload();
		upload.path = path;
		upload.data = new FileDataSource(srcFile);
		target.doUpload(upload);
	}

	public static FileUpload build(String data) {
		FileUpload upload = new FileUpload();
		upload.data = new ByteArrayDataSource(Utf8.getBytes(data));
		return upload;
	}

}
