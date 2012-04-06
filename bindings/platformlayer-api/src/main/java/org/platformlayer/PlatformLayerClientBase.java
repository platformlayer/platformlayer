package org.platformlayer;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public abstract class PlatformLayerClientBase implements PlatformLayerClient {
    static final Logger log = LoggerFactory.getLogger(PlatformLayerClientBase.class);

    static Class<?>[] objectFactories = { org.platformlayer.service.dns.v1.ObjectFactory.class, org.platformlayer.service.imagefactory.v1.ObjectFactory.class,
            org.platformlayer.service.instancesupervisor.v1.ObjectFactory.class, };

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

    public static <T> T deserializeItem(Class<? extends T> itemClass, String retval) throws PlatformLayerClientException {
        T created;

        try {
            created = (T) JaxbHelper.deserializeXmlObject(retval, itemClass);
        } catch (UnmarshalException e) {
            throw new PlatformLayerClientException("Error parsing returned data", e);
        }

        return created;
    }

    protected <T> PlatformLayerKey toKey(JaxbHelper jaxbHelper, T item) throws PlatformLayerClientException {
        return toKey(jaxbHelper, item, listServices(true));
    }

    public static <T> PlatformLayerKey toKey(JaxbHelper jaxbHelper, T item, Collection<ServiceInfo> services) throws PlatformLayerClientException {
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

        ManagedItemId id = item != null ? findId(item) : null;

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

    public static ServiceInfo getServiceInfo(Collection<ServiceInfo> services, String namespace) throws PlatformLayerClientException {
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
}
