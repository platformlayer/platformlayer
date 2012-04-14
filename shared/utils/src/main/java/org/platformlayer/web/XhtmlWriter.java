package org.platformlayer.web;

import java.io.PrintWriter;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class XhtmlWriter extends XmlWriter {
	static final Logger log = Logger.getLogger(XhtmlWriter.class);

	public XhtmlWriter(PrintWriter out) throws SAXException {
		super(out);
	}

	boolean open = true;

	public boolean isOpen() {
		return open;
	}

	@Override
	public void close() throws SAXException {
		super.close();
		open = false;
	}

	Stack<String> openTags = new Stack<String>();

	public void openTag(String tag) throws SAXException {
		openTags.push(tag);
		startElement(tag);
	}

	public void closeTag() throws SAXException {
		String tag = openTags.pop();
		endElement(tag);
	}

	public void writeSelfCloseElement(String tag) throws SAXException {
		openTag(tag);
		closeTag();
	}

	public void writeHyperlink(String url, String text) throws SAXException {
		writeHyperlink(url, text, null);
	}

	public void writeHyperlink(String url, String text, String cssClass) throws SAXException {
		if (url == null) {
			// We can pass null to just render text; simplifies calling code
			writeText(text);
		} else {
			setAttribute("href", url);
			if (cssClass != null) {
				setAttribute("class", cssClass);
			}
			openTag("a");
			writeText(text);
			closeTag();
		}
	}
}