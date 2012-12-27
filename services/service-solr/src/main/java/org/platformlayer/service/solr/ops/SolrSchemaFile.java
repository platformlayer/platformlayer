package org.platformlayer.service.solr.ops;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import com.fathomdb.Utf8;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.solr.model.SolrSchemaField;
import org.platformlayer.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SolrSchemaFile extends SyntheticXmlFile {

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		InputStream is = getClass().getResourceAsStream("schema.xml");

		Document dom;
		try {
			boolean isNamespaceAware = true;
			dom = XmlHelper.parseXmlDocument(is, isNamespaceAware);
		} catch (ParserConfigurationException e) {
			throw new OpsException("Error parsing XML template", e);
		} catch (SAXException e) {
			throw new OpsException("Error parsing XML template", e);
		} catch (IOException e) {
			throw new OpsException("Error parsing XML template", e);
		}

		SolrTemplateData template = OpsContext.get().getInjector().getInstance(SolrTemplateData.class);

		Element fieldsElement;
		{
			NodeList fieldsList = dom.getElementsByTagName("fields");
			if (fieldsList.getLength() != 1) {
				throw new OpsException("Expected exactly one fields element");
			}

			fieldsElement = (Element) fieldsList.item(0);
		}

		// TODO: Turn off default dynamic fields??
		for (SolrSchemaField field : template.getFields()) {
			boolean isDynamic = field.name.contains("*");

			Element el = dom.createElement(isDynamic ? "dynamicField" : "field");

			el.setAttribute("name", field.name);
			el.setAttribute("type", field.type);
			el.setAttribute("indexed", String.valueOf(field.indexed));
			el.setAttribute("stored", String.valueOf(field.stored));
			el.setAttribute("multiValued", String.valueOf(field.multiValued));

			// Compression removed in 1.4.1
			// if (field.compressThreshold >= 0) {
			// el.setAttribute("compressed", "true");
			// el.setAttribute("compressThreshold", String.valueOf(field.compressThreshold));
			// }

			fieldsElement.appendChild(el);
		}

		return Utf8.getBytes(XmlHelper.toXml(dom));
	}

}
