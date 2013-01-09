package org.platformlayer.xaas.web.jaxrs;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import org.slf4j.*;

public class JAXBContextAdapter extends JAXBContext {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(JAXBContextAdapter.class);

	@Override
	public Unmarshaller createUnmarshaller() throws JAXBException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Marshaller createMarshaller() throws JAXBException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Validator createValidator() throws JAXBException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
