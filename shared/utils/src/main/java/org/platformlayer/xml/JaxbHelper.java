package org.platformlayer.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.openstack.utils.Utf8;
import org.platformlayer.CastUtils;
import org.platformlayer.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

public class JaxbHelper {
	static final Logger log = LoggerFactory.getLogger(JaxbHelper.class);

	static ConcurrentMap<Class<?>, JaxbHelper> jaxbHelpers = new MapMaker()
			.makeComputingMap(new Function<Class<?>, JaxbHelper>() {

				@Override
				public JaxbHelper apply(Class<?> from) {
					try {
						return new JaxbHelper(from, null);
					} catch (JAXBException e) {
						throw new RuntimeException("Error building JAXB context", e);
					}
				}

			});

	public static JaxbHelper get(Class<?> jaxbRootElement) {
		return get(jaxbRootElement, null);
	}

	public static JaxbHelper get(Class<?> jaxbRootElement, List<Class<?>> extraClasses) {
		try {
			if (extraClasses == null || extraClasses.isEmpty()) {
				return jaxbHelpers.get(jaxbRootElement);
			} else {
				return new JaxbHelper(jaxbRootElement, extraClasses);
			}
		} catch (JAXBException e) {
			throw new RuntimeException("Error building JAXB context", e);
		}
	}

	final JAXBContext jaxbContext;
	Schema schema;

	/**
	 * If this is true, we buffer the response before attempting a decode; this allows us to include the response in
	 * error messages
	 */
	static final boolean DUMP_BAD_XML = false;
	private final Class<?> primaryClass;

	protected JaxbHelper(Class<?> primaryClass, List<Class<?>> extraClasses) throws JAXBException {
		this.primaryClass = primaryClass;

		if (primaryClass == Class.class) {
			throw new IllegalArgumentException();
		}

		if (extraClasses == null || extraClasses.isEmpty()) {
			jaxbContext = JAXBContext.newInstance(primaryClass);
		} else {
			List<Class<?>> classes = Lists.newArrayList();
			classes.add(primaryClass);
			classes.addAll(extraClasses);

			jaxbContext = JAXBContext.newInstance(classes.toArray(new Class<?>[classes.size()]));
		}
	}

	private final TrivialPool<Unmarshaller> unmarshallerPool = new TrivialPool<Unmarshaller>(8);

	protected Unmarshaller borrowUnmarshaller() throws JAXBException {
		Unmarshaller unmarshaller = unmarshallerPool.tryBorrow();
		if (unmarshaller == null) {
			unmarshaller = createUnmarshaller();
		}
		return unmarshaller;
	}

	/**
	 * Potential extension point
	 * 
	 * @throws JAXBException
	 */
	protected Unmarshaller createUnmarshaller() throws JAXBException {
		return getJAXBContext().createUnmarshaller();
	}

	protected JAXBContext getJAXBContext() {
		return jaxbContext;
	}

	protected void returnToPool(Unmarshaller unmarshaller) {
		unmarshallerPool.returnToPool(unmarshaller);
	}

	private final TrivialPool<Marshaller> marshallerPool = new TrivialPool<Marshaller>(8);

	protected Marshaller borrowMarshaller(boolean formatted) throws JAXBException {
		Marshaller marshaller = marshallerPool.tryBorrow();
		if (marshaller == null) {
			marshaller = createMarshaller();
		}

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(formatted));

		return marshaller;
	}

	public Marshaller createMarshaller() throws JAXBException {
		return getJAXBContext().createMarshaller();
	}

	protected void returnToPool(Marshaller marshaller) {
		marshallerPool.returnToPool(marshaller);
	}

	public Object unmarshal(InputStream is, Class<?> clazz) throws JAXBException {
		Unmarshaller unmarshaller = borrowUnmarshaller();
		try {
			return unmarshaller.unmarshal(new StreamSource(is), clazz);
		} finally {
			returnToPool(unmarshaller);
		}
	}

	public Object unmarshal(XMLStreamReader is) throws JAXBException {
		Unmarshaller unmarshaller = borrowUnmarshaller();
		try {
			return unmarshaller.unmarshal(is);
		} finally {
			returnToPool(unmarshaller);
		}
	}

	public Object unmarshal(StreamSource source) throws JAXBException {
		Unmarshaller unmarshaller = borrowUnmarshaller();
		try {
			return unmarshaller.unmarshal(source);
		} finally {
			returnToPool(unmarshaller);
		}
	}

	public Object unmarshal(Node node) throws JAXBException {
		Unmarshaller unmarshaller = borrowUnmarshaller();
		try {
			return unmarshaller.unmarshal(node);
		} finally {
			returnToPool(unmarshaller);
		}
	}

	public <T> T unmarshal(Node node, Class<T> jaxbClass) throws JAXBException {
		Unmarshaller unmarshaller = borrowUnmarshaller();
		try {
			JAXBElement<T> jaxb = unmarshaller.unmarshal(node, jaxbClass);
			return jaxb.getValue();
		} finally {
			returnToPool(unmarshaller);
		}
	}

	public Object unmarshal(StreamSource source, Class<?> clazz) throws JAXBException {
		Unmarshaller unmarshaller = borrowUnmarshaller();
		try {
			return unmarshaller.unmarshal(source, clazz);
		} finally {
			returnToPool(unmarshaller);
		}
	}

	public Object unmarshal(String xml) throws JAXBException {
		return unmarshal(new StreamSource(new StringReader(xml)));
	}

	public Object unmarshal(byte[] xml) throws JAXBException {
		return unmarshal(new StreamSource(new ByteArrayInputStream(xml)));
	}

	public Object unmarshal(String xml, Class<?> clazz) throws JAXBException {
		return unmarshal(new StreamSource(new StringReader(xml)), clazz);
	}

	public void marshal(Object object, boolean formatted, OutputStream os) throws JAXBException {
		Marshaller marshaller = borrowMarshaller(formatted);
		try {
			marshal(marshaller, object, os);
		} finally {
			returnToPool(marshaller);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void marshal(Marshaller marshaller, Object object, OutputStream os) throws JAXBException {
		Class<?> clazz = object.getClass();
		XmlRootElement xmlRootElement = clazz.getAnnotation(XmlRootElement.class);
		if (xmlRootElement != null) {
			marshaller.marshal(object, os);
		} else {
			String elementName = getXmlElementName(clazz);
			marshaller.marshal(new JAXBElement(new QName(getPrimaryNamespace(), elementName), clazz, object), os);
		}
	}

	public void marshal(Object object, boolean formatted, XMLStreamWriter os) throws JAXBException {
		Marshaller marshaller = borrowMarshaller(formatted);
		try {
			marshaller.marshal(object, os);
		} finally {
			returnToPool(marshaller);
		}
	}

	public Document marshalToDom(Object object) throws JAXBException {
		Marshaller marshaller = borrowMarshaller(false);
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document doc;
			try {
				doc = dbf.newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e) {
				throw new IllegalStateException("Error creating XML document", e);
			}

			marshaller.marshal(object, doc);
			return doc;
		} finally {
			returnToPool(marshaller);
		}
	}

	// public void marshal(Object object, boolean formatted, OutputStream os)
	// throws JAXBException {
	// Marshaller marshaller = borrowMarshaller(formatted);
	// try {
	// Class<?> xmlClass = object.getClass();
	// xmlClass = xmlClass.getSuperclass();
	// JAXBElement<?> jaxbElement = buildJaxbElement(xmlClass, object);
	// marshaller.marshal(jaxbElement, os);
	// } finally {
	// returnToPool(marshaller);
	// }
	// }
	//
	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// private JAXBElement<?> buildJaxbElement(Class<?> xmlClass, Object object)
	// {
	// String localPart = xmlClass.getSimpleName().toLowerCase();
	// return new JAXBElement(new QName(API_XML_NAMESPACE, localPart), xmlClass,
	// object);
	// }

	public String marshal(Object object, boolean formatted) throws JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshal(object, formatted, baos);
		return Utf8.toString(baos.toByteArray());
	}

	public static String debugDumpXml(Object o) {
		if (o == null) {
			return "null";
		}
		Class<? extends Object> clazz = o.getClass();
		JaxbHelper jaxbHelper = get(clazz);
		try {
			return jaxbHelper.marshal(o, true);
		} catch (JAXBException e) {
			e.printStackTrace();
			return "Error serializing: " + e.getMessage();
		}
	}

	public static <T> T deserializeXmlObject(String s, Class<T> clazz) throws UnmarshalException {
		return deserializeXmlObject(new StringReader(s), clazz, false);
	}

	public static <T> T deserializeXmlObject(InputStream is, Class<T> clazz, boolean recordXml)
			throws UnmarshalException {
		return deserializeXmlObject(Utf8.openReader(is), clazz, recordXml);
	}

	public static <T> T deserializeXmlObject(Reader reader, Class<T> clazz, boolean recordXml)
			throws UnmarshalException {
		JaxbHelper helper = JaxbHelper.get(clazz);
		return helper.deserialize(reader, clazz, recordXml);
	}

	public <T> T deserialize(Reader reader, Class<T> clazz) throws UnmarshalException {
		return deserialize(reader, clazz, false);
	}

	public <T> T deserialize(Reader reader, Class<T> clazz, boolean recordXml) throws UnmarshalException {
		String xml = null;
		Object jaxbObject;
		try {
			if (recordXml) {
				xml = IoUtils.readAll(reader);
				jaxbObject = unmarshal(new StreamSource(new StringReader(xml)), clazz);
			} else {
				jaxbObject = unmarshal(new StreamSource(reader), clazz);

			}
		} catch (JAXBException e) {
			throw new UnmarshalException("Error reading XML", e, xml);
		} catch (IOException e) {
			throw new UnmarshalException("Error reading XML", e, xml);
		}

		if (jaxbObject instanceof JAXBElement<?>) {
			jaxbObject = ((JAXBElement<?>) jaxbObject).getValue();
		}

		if (jaxbObject == null) {
			return null;
		}
		T value = CastUtils.as(jaxbObject, clazz);
		if (value == null) {
			throw new UnmarshalException("Expected response to be of type: " + clazz.getSimpleName() + ", was "
					+ jaxbObject.getClass().getSimpleName(), null, xml);
		}
		return value;
	}

	/**
	 * A utility function for easy dumping of our objects to XML
	 * 
	 * @param object
	 * @param formatted
	 * @return
	 * @throws JAXBException
	 */
	public static String toXml(Object object, boolean formatted) throws JAXBException {
		Class<?> clazz = object.getClass();
		JaxbHelper jaxbHelper = get(clazz);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		jaxbHelper.marshal(object, formatted, baos);
		return Utf8.toString(baos.toByteArray());
	}

	public static String toStringHelper(Object object) {
		// TODO: This is an evil hack, and we probably would prefer JSON output
		// for readability anyway....
		String xml;
		try {
			xml = toXml(object, true);
		} catch (JAXBException e) {
			return "<Errror: " + e.toString() + ">";
		}
		xml = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
		xml = xml.replace("\t", " ");
		xml = xml.replace("\n", " ");
		xml = xml.trim();
		while (xml.contains("  ")) {
			xml = xml.replace("  ", " ");
		}
		return xml;
	}

	// public Schema getSchema() throws IOException, SAXException {
	// if (this.schema == null) {
	// MemorySchemaOutputResolver outputResolver = new
	// MemorySchemaOutputResolver();
	// getJAXBContext().generateSchema(outputResolver);
	//
	// if (outputResolver.getWriters().size() != 1)
	// throw new IllegalStateException();
	//
	// StringWriter primaryStringWriter =
	// Iterables.get(outputResolver.getWriters().values(), 0);
	//
	// // StAX for some reason redefines this class...?
	// String W3C_XML_SCHEMA_INSTANCE_NS_URI =
	// "http://www.w3.org/2001/XMLSchema-instance"; //
	// XMLConstants.W3C_XML_SCHEMA_NS_URI
	//
	// SchemaFactory schemaFactory =
	// SchemaFactory.newInstance(W3C_XML_SCHEMA_INSTANCE_NS_URI);
	// this.schema = schemaFactory.newSchema(new StreamSource(new
	// StringReader(primaryStringWriter.toString())));
	// }
	// return this.schema;
	// }

	public String getPrimaryNamespace() {
		XmlSchema annotation = primaryClass.getPackage().getAnnotation(javax.xml.bind.annotation.XmlSchema.class);
		String namespace = null;
		if (annotation != null) {
			namespace = XmlHelper.getXmlNamespace(annotation);
		}
		if (namespace == null) {
			log.warn("No namespace for " + primaryClass);
		}
		return namespace;
	}

	public String getXmlElementName() {
		return getXmlElementName(primaryClass);
	}

	public static String getXmlElementName(Class<?> clazz) {
		XmlElement annotation = clazz.getAnnotation(javax.xml.bind.annotation.XmlElement.class);
		String name = null;
		if (annotation != null) {
			name = annotation.name();
			if ("##default".equals(name)) {
				name = null;
			}
		}
		if (name == null) {
			XmlType xmlType = clazz.getAnnotation(XmlType.class);
			if (xmlType != null) {
				name = xmlType.name();
			}
		}
		if (name == null) {
			name = clazz.getSimpleName();
			name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		}
		return name;
	}

}
