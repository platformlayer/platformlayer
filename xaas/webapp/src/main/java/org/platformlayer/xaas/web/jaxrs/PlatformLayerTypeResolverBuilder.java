package org.platformlayer.xaas.web.jaxrs;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.platformlayer.core.model.Action;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

public class PlatformLayerTypeResolverBuilder extends StdTypeResolverBuilder {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PlatformLayerTypeResolverBuilder.class);

	@Override
	public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType,
			Collection<NamedType> subtypes) {
		return useForType(baseType) ? super.buildTypeDeserializer(config, baseType, subtypes) : null;
	}

	@Override
	public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType,
			Collection<NamedType> subtypes) {
		return useForType(baseType) ? super.buildTypeSerializer(config, baseType, subtypes) : null;
	}

	/**
	 * Method called to check if the default type handler should be used for given type. Note: "natural types" (String,
	 * Boolean, Integer, Double) will never use typing; that is both due to them being concrete and final, and since
	 * actual serializers and deserializers will also ignore any attempts to enforce typing.
	 */
	public boolean useForType(JavaType t) {
		// switch (_appliesFor) {
		// case NON_CONCRETE_AND_ARRAYS:
		// while (t.isArrayType()) {
		// t = t.getContentType();
		// }
		// // fall through
		// case OBJECT_AND_NON_CONCRETE:
		// return (t.getRawClass() == Object.class) || !t.isConcrete();
		// case NON_FINAL:
		// while (t.isArrayType()) {
		// t = t.getContentType();
		// }
		// return !t.isFinal(); // includes Object.class
		// default:
		// // case JAVA_LANG_OBJECT:
		// return (t.getRawClass() == Object.class);
		// }
		Class<?> rawClass = t.getRawClass();
		// if (rawClass == Object.class) {
		// return true;
		// }
		// if (rawClass == ItemBase.class) {
		// return true;
		// }
		if (rawClass == Action.class) {
			return true;
		}

		return false;
	}
}
