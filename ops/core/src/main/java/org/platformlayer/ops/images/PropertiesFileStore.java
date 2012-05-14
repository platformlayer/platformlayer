package org.platformlayer.ops.images;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import org.platformlayer.Strings;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ops.FileUpload;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FilesystemInfo;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class PropertiesFileStore {
	final OpsTarget target;
	final File baseDir;

	static final String FILE_EXTENSION_PROPERTIES = ".properties";

	// private static final String PROPERTY_PREFIX_TAG = "tag-";

	public PropertiesFileStore(OpsTarget target, File baseDir) {
		this.target = target;
		this.baseDir = baseDir;
	}

	String getKey(FilesystemInfo file) {
		String name = file.name;
		int slashIndex = name.lastIndexOf('/');
		if (slashIndex != -1) {
			name = name.substring(slashIndex + 1);
		}
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex != -1) {
			name = name.substring(0, dotIndex);
		}
		return name;
	}

	public List<String> getKeys() throws OpsException {
		List<String> keys = Lists.newArrayList();

		List<FilesystemInfo> files = target.getFilesystemInfoDir(baseDir);

		for (FilesystemInfo file : files) {
			String fileName = file.name;
			if (!fileName.endsWith(FILE_EXTENSION_PROPERTIES)) {
				continue;
			}

			String key = getKey(file);

			keys.add(key);
		}

		return keys;
	}

	public Properties readProperties(String key) throws OpsException {
		// TODO: Caching??
		File path = getConfigFile(key);

		Properties properties = ConfigMap.read(target, path);
		return properties;
	}

	public void writeProperties(String key, Properties properties) throws OpsException {
		File path = getConfigFile(key);

		String contents;

		try {
			StringWriter writer = new StringWriter();
			properties.store(writer, null);
			contents = writer.toString();
		} catch (IOException e) {
			throw new OpsException("Error serializing properties", e);
		}

		FileUpload.upload(target, path, contents);
	}

	private File getConfigFile(String key) {
		return new File(baseDir, key + FILE_EXTENSION_PROPERTIES);
	}

	public String findFirst(List<Tag> tags) throws OpsException {
		List<String> keys = getKeys();

		for (String key : keys) {
			Properties properties = readProperties(key);

			boolean valid = true;

			for (Tag tag : tags) {
				// String tagKey = PropertiesFileStore.PROPERTY_PREFIX_TAG + tag.key;
				String tagKey = tag.key;
				String tagValue = properties.getProperty(tagKey);
				if (!Objects.equal(tagValue, tag.getValue())) {
					valid = false;
					break;
				}
			}

			if (valid) {
				return key;
			}
		}
		return null;
	}

	public List<String> find(List<Tag> tags) throws OpsException {
		List<String> matches = Lists.newArrayList();

		List<String> keys = getKeys();
		for (String key : keys) {
			Properties properties = readProperties(key);

			boolean valid = true;

			for (Tag tag : tags) {
				// String tagKey = PropertiesFileStore.PROPERTY_PREFIX_TAG + tag.key;
				String tagKey = tag.key;
				String tagValue = properties.getProperty(tagKey);
				if (!Objects.equal(tagValue, tag.getValue())) {
					valid = false;
					break;
				}
			}

			if (valid) {
				matches.add(key);
			}
		}

		return matches;
	}

	public Tags asTags(Properties properties) {
		Tags tags = new Tags();
		for (Object keyObject : properties.keySet()) {
			String key = (String) keyObject;
			// if (key.startsWith(PROPERTY_PREFIX_TAG)) {
			// String tagName = key.substring(PROPERTY_PREFIX_TAG.length());
			String tagName = key;
			Tag tag = new Tag(tagName, properties.getProperty(key));
			tags.add(tag);
			// }
		}
		return tags;
	}

	public Properties toProperties(Tags tags) {
		Properties properties = new Properties();
		for (Tag tag : tags) {
			// properties.put(PROPERTY_PREFIX_TAG + tag.getKey(), tag.getValue());
			properties.put(tag.getKey(), tag.getValue());
		}
		return properties;
	}
}
