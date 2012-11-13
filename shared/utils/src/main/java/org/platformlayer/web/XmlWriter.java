package org.platformlayer.web;

import java.io.PrintWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class XmlWriter {
	static final Logger log = LoggerFactory.getLogger(XmlWriter.class);

	final TransformerHandler hd;
	final AttributesImpl atts = new AttributesImpl();

	public XmlWriter(PrintWriter out) throws SAXException {
		try {
			StreamResult streamResult = new StreamResult(out);
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
			// SAX2.0 ContentHandler.
			hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			// serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			hd.setResult(streamResult);
			hd.startDocument();
		} catch (Exception e) {
			throw new SAXException("Unexpected error in XML initialization", e);
		}
	}

	public void close() throws SAXException {
		hd.endDocument();
	}

	public void setAttribute(String name, String value) {
		atts.addAttribute("", "", name, "CDATA", value);
	}

	public void startElement(String name) throws SAXException {
		hd.startElement(null, null, name, atts);
		atts.clear();
	}

	public void endElement(String name) throws SAXException {
		hd.endElement(null, null, name);
	}

	public void writeText(String text) throws SAXException {
		if (text == null) {
			return;
		}
		char[] chars = text.toCharArray();
		hd.characters(chars, 0, chars.length);
	}

	public void writeSimpleElement(String name, String contents) throws SAXException {
		startElement(name);
		writeText(contents);
		endElement(name);
	}
}