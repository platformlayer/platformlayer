package org.platformlayer.guice.xaas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.platformlayer.xml.JaxbHelper;

import com.fathomdb.Casts;
import com.fathomdb.io.IoUtils;

public class CloneHelpers {
	public static <T> T cloneViaSerialization(T o) {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);

			oos.writeObject(o);
			oos.flush();

			ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

			T t = (T) Casts.checkedCast(ois.readObject(), o.getClass());
			return t;
		} catch (IOException e) {
			throw new IllegalStateException("Error while cloning object", e);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Error while cloning object", e);
		} finally {
			IoUtils.safeClose(oos);
			IoUtils.safeClose(ois);

		}

	}

	public static <T> T cloneViaJaxb(T o) {
		try {
			Class<T> objectClass = (Class<T>) o.getClass();
			JaxbHelper jaxbHelper = JaxbHelper.get(objectClass);

			String xml = JaxbHelper.toXml(o, false);
			return jaxbHelper.deserialize(new StringReader(xml), objectClass);
		} catch (UnmarshalException e) {
			throw new IllegalStateException("Error while cloning object", e);
		} catch (JAXBException e) {
			throw new IllegalStateException("Error while cloning object", e);
		}
	}
}
