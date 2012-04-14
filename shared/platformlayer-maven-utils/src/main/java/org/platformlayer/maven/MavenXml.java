package org.platformlayer.maven;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MavenXml {
	// public static final JaxbHelper JAXB;
	//
	// static {
	// JAXB = JaxbHelper.get(org.apache.maven.pom._4_0.ObjectFactory.class);
	// }

	public static Metadata readMetadata(InputStream is) throws IOException {
		MetadataXpp3Reader reader = new MetadataXpp3Reader();
		try {
			return reader.read(is);
		} catch (XmlPullParserException e) {
			throw new IOException("Error parsing XML", e);
		}
	}

	public static Metadata readMetadata(String xml) throws IOException {
		MetadataXpp3Reader reader = new MetadataXpp3Reader();
		try {
			return reader.read(new StringReader(xml));
		} catch (XmlPullParserException e) {
			throw new IOException("Error parsing XML", e);
		}
	}
}
