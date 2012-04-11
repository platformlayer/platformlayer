package org.platformlayer.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

public class XmlHelper {
    static final Logger log = Logger.getLogger(XmlHelper.class);

    public static XMLStreamReader buildXmlStreamReader(String xml) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        return factory.createXMLStreamReader(new StringReader(xml));
    }

    public static Document parseXmlDocument(String xml, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder docBuilder = buildDocumentBuilder(namespaceAware);
        Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));

        // normalize text representation
        doc.getDocumentElement().normalize();

        return doc;
    }

    public static Document parseXmlDocument(File file, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder docBuilder = buildDocumentBuilder(namespaceAware);
        Document doc = docBuilder.parse(file);

        // normalize text representation
        doc.getDocumentElement().normalize();

        return doc;
    }

    public static Document parseXmlDocument(InputStream is, boolean namespaceAware) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder docBuilder = buildDocumentBuilder(namespaceAware);
        Document doc = docBuilder.parse(is);

        // normalize text representation
        doc.getDocumentElement().normalize();

        return doc;
    }

    private static DocumentBuilder buildDocumentBuilder(boolean namespaceAware) throws ParserConfigurationException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(namespaceAware);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        return docBuilder;
    }

    public static String getNodeContents(Node parentNode) {
        StringBuilder sb = new StringBuilder();

        NodeList childNodes = parentNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            String nodeValue = node.getNodeValue();
            if (nodeValue != null)
                sb.append(nodeValue);
        }

        return sb.toString();
    }

    // public static Date parseDate(String dateString) throws ParseException {
    // if (dateString == null)
    // return null;
    //
    // return DateUtils.smartParse(dateString);
    // }

    public static String toXml(Node node) throws TransformerException {
        Source src = new DOMSource(node);
        return toXml(src, -1);
    }

    public static String toXml(XMLStreamReader xml) throws TransformerException {
        Source src = new StAXSource(xml);
        return toXml(src, -1);
    }

    public static String toXml(Source src, int indent) throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {
        Transformer transformer = buildXmlTransformer();
        if (indent >= 0) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
        }

        StringWriter writer = new StringWriter();

        Result dest = new StreamResult(writer);
        transformer.transform(src, dest);

        return writer.getBuffer().toString();
    }

    public static String toXml(Document xmlDocument) throws TransformerException {
        return toXml(xmlDocument.getDocumentElement());
    }

    private static Transformer buildXmlTransformer() throws TransformerFactoryConfigurationError, TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        return transformer;
    }

    public static Node getChildElement(Node parent, String elementName) {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (elementName.equals(childNode.getNodeName()))
                return childNode;
        }
        return null;
    }

    public static String safeToXml(Element element) {
        try {
            return toXml(element);
        } catch (TransformerException e) {
            return "Error transforming to xml: " + e.toString();
        }
    }

    public static Node findUniqueChild(Element parent, String tagName) {
        return findUniqueChild(parent, tagName, true);
    }

    public static Node findUniqueChild(Element parent, String tagName, boolean create) {
        NodeList children = parent.getChildNodes();
        List<Node> matches = Lists.newArrayList();
        for (int i = 0; i < children.getLength(); i++) {
            Node item = children.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (tagName.equals(((Element) item).getTagName())) {
                    matches.add(item);
                }
            }
        }

        if (matches.size() == 0) {
            if (create) {
                Element element = parent.getOwnerDocument().createElement(tagName);
                parent.appendChild(element);
                return element;
            }
            return null;
        }

        if (matches.size() != 1) {
            try {
                String xml = XmlHelper.toXml(parent);
                log.warn("Multiple elements in XML: " + xml);
            } catch (TransformerException e) {
                throw new IllegalStateException("Error converting to XML", e);
            }
            throw new IllegalStateException("Found multiple elements of name: " + tagName);
        }

        Node child = matches.get(0);

        return child;
    }

    public static Object unmarshal(JAXBContext jaxbContext, XMLStreamReader xmlStreamReader) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(xmlStreamReader);
    }

    public static class ElementInfo {
        public final String namespace;
        public final String elementName;

        public ElementInfo(String namespace, String elementName) {
            super();
            this.namespace = namespace;
            this.elementName = elementName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((elementName == null) ? 0 : elementName.hashCode());
            result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ElementInfo other = (ElementInfo) obj;
            if (elementName == null) {
                if (other.elementName != null)
                    return false;
            } else if (!elementName.equals(other.elementName))
                return false;
            if (namespace == null) {
                if (other.namespace != null)
                    return false;
            } else if (!namespace.equals(other.namespace))
                return false;
            return true;
        }
        
        
    }

    public static ElementInfo getXmlElementInfo(Class<?> clazz) {
        String elementName = null;
        String namespace = null;
        XmlType xmlType = clazz.getAnnotation(XmlType.class);
        if (xmlType != null) {
            elementName = xmlType.name();
            namespace = xmlType.namespace();
        } else {
            XmlRootElement xmlRootElement = clazz.getAnnotation(XmlRootElement.class);
            if (xmlRootElement != null) {
                elementName = xmlRootElement.name();
                namespace = xmlRootElement.namespace();
            }
        }

        if ("##default".equals(elementName)) {
            elementName = StringUtils.uncapitalize(clazz.getSimpleName());
        }

        if ("##default".equals(namespace)) {
            namespace = null;
        }

        if (namespace == null) {
            Package itemPackage = clazz.getPackage();
            XmlSchema xmlSchema = itemPackage.getAnnotation(XmlSchema.class);
            if (xmlSchema != null) {
                namespace = xmlSchema.namespace();
            }
        }

        if (elementName != null && namespace != null) {
            return new ElementInfo(namespace, elementName);
        }

        return null;
    }

}
