package org.platformlayer.xaas;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class PlatformLayerTypeIdResolver extends TypeIdResolverBase {
	protected PlatformLayerTypeIdResolver(JavaType baseType, TypeFactory typeFactory) {
		super(baseType, typeFactory);
	}

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PlatformLayerTypeIdResolver.class);

	@Override
	public String idFromValue(Object value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public JavaType typeFromId(String id) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Id getMechanism() {
		return Id.CUSTOM;
	}

}
