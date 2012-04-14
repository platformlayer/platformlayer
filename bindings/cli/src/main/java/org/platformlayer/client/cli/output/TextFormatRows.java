package org.platformlayer.client.cli.output;

import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.bind.JAXBException;

import org.platformlayer.xml.JaxbHelper;

public class TextFormatRows {
	final PrintWriter out;

	public TextFormatRows(PrintWriter out) {
		super();
		this.out = out;
	}

	public void doPrint(Object results) throws IOException {
		try {
			out.println(JaxbHelper.toXml(results, true));
		} catch (JAXBException e) {
			throw new IOException("Error serializing to XML", e);
		}
	}
}
