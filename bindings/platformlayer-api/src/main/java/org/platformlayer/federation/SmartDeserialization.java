package org.platformlayer.federation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.bind.UnmarshalException;

import org.platformlayer.ops.OpsException;
import org.platformlayer.xml.JaxbHelper;

import com.fathomdb.io.IoUtils;

public class SmartDeserialization {

	enum Format {
		XML, JSON, PROPERTIES,
	}

	public static <T> T deserialize(Class<T> c, InputStream is) throws OpsException {
		// TODO: Auto-detect XML, JSON, others?

		String data;
		try {
			data = IoUtils.readAll(is);
		} catch (IOException e) {
			throw new OpsException("Error reading data", e);
		}

		Format format = null;

		for (int i = 0; i < data.length(); i++) {
			char firstChar = data.charAt(i);

			switch (firstChar) {
			case ' ':
				continue;
			case '<':
				format = Format.XML;
				break;
			case '{':
				format = Format.JSON;
				break;
			default: {
				if (Character.isLetter(firstChar)) {
					format = Format.PROPERTIES;
				} else {
					throw new IllegalArgumentException("Unhandled character: " + ((int) firstChar));
				}
				break;
			}
			}

			if (format != null) {
				break;
			}
		}

		if (format == null) {
			throw new IllegalStateException("Could not determine format");
		}

		if (format == Format.XML) {
			JaxbHelper jaxb = JaxbHelper.get(c);
			try {
				return jaxb.deserialize(new StringReader(data), c);
			} catch (UnmarshalException e) {
				throw new OpsException("Error deserializing item", e);
			}
		} else {
			throw new UnsupportedOperationException();
		}

	}
}
