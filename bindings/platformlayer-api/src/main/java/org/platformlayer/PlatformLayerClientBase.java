package org.platformlayer;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.platformlayer.common.UntypedItem;
import org.platformlayer.common.UntypedItemCollection;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xml.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class PlatformLayerClientBase implements PlatformLayerClient {
	static final Logger log = LoggerFactory.getLogger(PlatformLayerClientBase.class);

	static Class<?>[] objectFactories = {};

	private final TypedItemMapper mapper;

	public PlatformLayerClientBase(TypedItemMapper mapper) {
		this.mapper = mapper;
	}

	public static <T> JaxbHelper toJaxbHelper(Class<?> clazz, Class<?>... extraClasses) {
		List<Class<?>> extraClassesLists = Arrays.asList(extraClasses);

		// Package clazzPackage = clazz.getPackage();
		// for (Class<?> objectFactoryClass : objectFactories) {
		// if (clazzPackage.equals(objectFactoryClass.getPackage())) {
		// extraClassesLists.add(objectFactoryClass);
		// return JaxbHelper.get(objectFactoryClass, extraClassesLists);
		// }
		// }

		return JaxbHelper.get(clazz, extraClassesLists);
	}

	public static <T> JaxbHelper toJaxbHelper(T jaxbObject) {
		Class<?> clazz = jaxbObject.getClass();
		return toJaxbHelper(clazz, new Class[] {});
	}

	public static <T> String serialize(JaxbHelper jaxbHelper, T item) {
		String xml;
		try {
			xml = jaxbHelper.marshal(item, false);
		} catch (JAXBException e) {
			throw new IllegalArgumentException("Error marshalling object to XML", e);
		}
		return xml;
	}

	public static <T> T deserializeItem(Class<? extends T> itemClass, String retval)
			throws PlatformLayerClientException {
		T created;

		try {
			created = JaxbHelper.deserializeXmlObject(retval, itemClass);
		} catch (UnmarshalException e) {
			throw new PlatformLayerClientException("Error parsing returned data", e);
		}

		return created;
	}

	protected <T> PlatformLayerKey toKey(JaxbHelper jaxbHelper, T item) throws PlatformLayerClientException {
		return toKey(jaxbHelper, item, listServices(true));
	}

	public static <T> PlatformLayerKey toKey(JaxbHelper jaxbHelper, T item, Collection<ServiceInfo> services)
			throws PlatformLayerClientException {
		ManagedItemId id = item != null ? findId(item) : null;

		return toKey(jaxbHelper, id, item != null ? item.getClass() : null, services);
	}

	protected <T> PlatformLayerKey toKey(JaxbHelper jaxbHelper, ManagedItemId id, Class<T> itemClass)
			throws PlatformLayerClientException {
		return toKey(jaxbHelper, id, itemClass, listServices(true));
	}

	public static <T> PlatformLayerKey toKey(JaxbHelper jaxbHelper, ManagedItemId id, Class<T> itemClass,
			Collection<ServiceInfo> services) throws PlatformLayerClientException {
		String namespaceURI = jaxbHelper.getPrimaryNamespace();
		String nodeName = jaxbHelper.getXmlElementName();

		if (namespaceURI == null) {
			throw new IllegalArgumentException("Namespace could not be determined");
		}

		ServiceInfo service = getServiceInfo(services, namespaceURI);
		if (service == null) {
			throw new PlatformLayerClientException("Cannot find service for " + namespaceURI);
		}

		ServiceType serviceType = new ServiceType(service.getServiceType());
		ItemType itemType = new ItemType(nodeName);

		FederationKey host = null;
		ProjectId project = null;
		return new PlatformLayerKey(host, project, serviceType, itemType, id);
	}

	public static <T> ManagedItemId findId(T item) throws PlatformLayerClientException {
		String v;

		try {
			Method method = item.getClass().getMethod("getId");
			v = (String) method.invoke(item, null);
		} catch (SecurityException e) {
			throw new IllegalArgumentException("Cannot get id", e);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Cannot get id", e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Cannot get id", e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Cannot get id", e);
		} catch (InvocationTargetException e) {
			throw new IllegalArgumentException("Cannot get id", e);
		}

		if (v == null || v.isEmpty()) {
			return null;

			// We could ask the server to figure it out for us (PUT on collection?)
			// throw new PlatformLayerClientException("Cannot determine item id");
		}

		return new ManagedItemId(v);
	}

	protected <T> PlatformLayerKey toKey(JaxbHelper jaxbHelper) throws PlatformLayerClientException {
		return toKey(jaxbHelper, null);
	}

	public ServiceInfo getServiceInfo(String namespace) throws PlatformLayerClientException {
		List<ServiceInfo> services = Lists.newArrayList(listServices(true));
		return getServiceInfo(services, namespace);
	}

	public static ServiceInfo getServiceInfo(Collection<ServiceInfo> services, String namespace)
			throws PlatformLayerClientException {
		return ServiceUtils.findByNamespace(services, namespace);
	}

	public static String buildRelativePath(PlatformLayerKey key) {
		return buildRelativePath(key.getServiceType(), key.getItemType(), key.getItemId());
	}

	public static String buildRelativePath(ServiceType serviceType, ItemType itemType, ManagedItemId id) {
		String s = urlEncode(serviceType.getKey()) + "/" + urlEncode(itemType.getKey());
		if (id != null) {
			s += "/" + urlEncode(id.getKey());
		}
		return s;
	}

	public static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 Encoding not found", e);
		}
	}

	@Override
	public <T> List<T> listItems(Class<T> clazz) throws OpsException {
		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(clazz, ManagedItemCollection.class);
		PlatformLayerKey path = PlatformLayerClientBase.toKey(jaxbHelper, null, listServices(true));

		UntypedItemCollection untypedItems = listItemsUntyped(path);

		List<T> items = Lists.newArrayList();

		for (UntypedItem untypedItem : untypedItems.getItems()) {
			T item = promoteToTyped(untypedItem, clazz);
			items.add(item);
		}

		return items;
	}

	public <T> T promoteToTyped(UntypedItem untypedItem) throws PlatformLayerClientException {
		if (mapper == null) {
			throw new UnsupportedOperationException();
		}
		try {
			return mapper.promoteToTyped(untypedItem);
		} catch (OpsException e) {
			throw new PlatformLayerClientException("Error parsing item", e);
		}
	}

	public <T> T promoteToTyped(UntypedItem untypedItem, Class<T> itemClass) throws PlatformLayerClientException {
		if (mapper == null) {
			throw new UnsupportedOperationException();
		}
		try {
			return mapper.promoteToTyped(untypedItem, itemClass);
		} catch (OpsException e) {
			throw new PlatformLayerClientException("Error parsing item", e);
		}
	}

	@Override
	public <T extends ItemBase> T putItemByTag(T item, Tag uniqueTag) throws OpsException {
		JaxbHelper jaxbHelper = PlatformLayerClientBase.toJaxbHelper(item);

		String xml = PlatformLayerClientBase.serialize(jaxbHelper, item);
		PlatformLayerKey key = PlatformLayerClientBase.toKey(jaxbHelper, item, listServices(true));

		UntypedItem ret = putItemByTag(key, uniqueTag, xml, Format.XML);
		Class<T> itemClass = (Class<T>) item.getClass();
		return promoteToTyped(ret, itemClass);
	}

	@Override
	public List<ItemBase> listChildrenTyped(PlatformLayerKey parent) throws OpsException {
		List<ItemBase> ret = Lists.newArrayList();
		for (UntypedItem item : listChildren(parent).getItems()) {
			ItemBase typedItem = promoteToTyped(item);
			ret.add(typedItem);
		}
		return ret;
	}

	@Override
	public <T> T findItem(PlatformLayerKey key, Class<T> itemClass) throws OpsException {
		UntypedItem itemUntyped = getItemUntyped(key, Format.XML);
		if (itemUntyped == null) {
			return null;
		}

		return promoteToTyped(itemUntyped, itemClass);
	}

	@Override
	public <T> T findItem(PlatformLayerKey key) throws OpsException {
		UntypedItem itemUntyped = getItemUntyped(key, Format.XML);
		if (itemUntyped == null) {
			return null;
		}

		return promoteToTyped(itemUntyped);
	}

	public Tags changeTags(PlatformLayerKey key, TagChanges tagChanges) throws PlatformLayerClientException {
		return changeTags(key, tagChanges, null);
	}

	@Override
	public Tags getItemTags(PlatformLayerKey key) throws PlatformLayerClientException {
		UntypedItem itemUntyped = getItemUntyped(key, Format.XML);
		if (itemUntyped == null) {
			return null;
		}
		return itemUntyped.getTags();
	}

}
