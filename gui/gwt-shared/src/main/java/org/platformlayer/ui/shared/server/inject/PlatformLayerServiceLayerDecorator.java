package org.platformlayer.ui.shared.server.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.auth.OpsAuthentication;
import org.platformlayer.xaas.web.resources.ItemService;

import com.google.web.bindery.requestfactory.server.ServiceLayerDecorator;
import com.google.web.bindery.requestfactory.shared.BaseProxy;
import com.google.web.bindery.requestfactory.shared.Locator;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;

public class PlatformLayerServiceLayerDecorator extends ServiceLayerDecorator {

	@Inject
	Provider<OpsAuthentication> authenticationProvider;

	@Inject
	ItemService itemService;

	@Override
	public <T> T createDomainObject(Class<T> clazz) {
		// TODO Auto-generated method stub
		return super.createDomainObject(clazz);
	}

	@Override
	public <T extends Locator<?, ?>> T createLocator(Class<T> clazz) {
		// TODO Auto-generated method stub
		return super.createLocator(clazz);
	}

	@Override
	public Object createServiceInstance(Class<? extends RequestContext> requestContext) {
		// TODO Auto-generated method stub
		return super.createServiceInstance(requestContext);
	}

	@Override
	public <T extends ServiceLocator> T createServiceLocator(Class<T> clazz) {
		// TODO Auto-generated method stub
		return super.createServiceLocator(clazz);
	}

	@Override
	public ClassLoader getDomainClassLoader() {
		// TODO Auto-generated method stub
		return super.getDomainClassLoader();
	}

	@Override
	public Method getGetter(Class<?> domainType, String property) {
		// TODO Auto-generated method stub
		return super.getGetter(domainType, property);
	}

	@Override
	public Object getId(Object domainObject) {
		// TODO Auto-generated method stub
		return super.getId(domainObject);
	}

	@Override
	public Class<?> getIdType(Class<?> domainType) {
		// TODO Auto-generated method stub
		return super.getIdType(domainType);
	}

	@Override
	public Object getProperty(Object domainObject, String property) {
		Class<? extends Object> domainClass = domainObject.getClass();

		XmlAccessorType xmlAccessorType = domainClass.getAnnotation(XmlAccessorType.class);
		if (xmlAccessorType != null && xmlAccessorType.value() == XmlAccessType.FIELD) {
			Field field = null;
			try {
				field = domainClass.getField(property);
			} catch (SecurityException e) {
				throw new IllegalStateException("Unexpected error while getting field", e);
			} catch (NoSuchFieldException e) {
				field = null;
			}
			if (field != null) {
				try {
					return field.get(domainObject);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Unexpected error while getting field", e);
				}
			}
		}

		// if (domainObject instanceof ManagedItem) {
		// boolean passThrough = property.equals("id") || property.equals("version");
		// if (!passThrough) {
		// ManagedItem<?> managedItem = (ManagedItem) domainObject;
		//
		// Document doc = null;
		//
		// String xml = managedItem.getModelData();
		//
		// if (xml != null && !xml.isEmpty()) {
		// try {
		// doc = XmlHelper.parseXmlDocument(xml, true);
		// } catch (ParserConfigurationException e) {
		// throw new IllegalArgumentException("Error parsing data", e);
		// } catch (SAXException e) {
		// throw new IllegalArgumentException("Error parsing data", e);
		// } catch (IOException e) {
		// throw new IllegalArgumentException("Error parsing data", e);
		// }
		// }
		//
		// if (doc != null) {
		// Element element = doc.getElementById(property);
		// if (element != null) {
		// String value = element.getTextContent();
		// return value;
		// }
		// }
		//
		// }
		// }
		// // TODO Auto-generated method stub

		return super.getProperty(domainObject, property);
	}

	@Override
	public Type getRequestReturnType(Method contextMethod) {
		// TODO Auto-generated method stub
		return super.getRequestReturnType(contextMethod);
	}

	@Override
	public Method getSetter(Class<?> domainType, String property) {
		// TODO Auto-generated method stub
		return super.getSetter(domainType, property);
	}

	@Override
	public Object getVersion(Object domainObject) {
		// TODO Auto-generated method stub
		return super.getVersion(domainObject);
	}

	@Override
	public Object invoke(Method domainMethod, Object... args) {
		// TODO Auto-generated method stub
		return super.invoke(domainMethod, args);
	}

	@Override
	public boolean isLive(Object domainObject) {
		if (domainObject instanceof ItemBase) {
			Class<? extends ItemBase> clazz = (Class<? extends ItemBase>) domainObject.getClass();
			try {
				return findDomainObject(clazz, ((ItemBase) domainObject).getId()) != null;
			} catch (OpsException e) {
				throw new IllegalStateException("Error checking for object liveness", e);
			}
		}
		// TODO Auto-generated method stub
		return super.isLive(domainObject);
	}

	private <T extends ItemBase> T findDomainObject(Class<T> clazz, String id) throws OpsException {
		OpsAuthentication auth = authenticationProvider.get();

		PlatformLayerLiveObjects liveObjects = PlatformLayerLiveObjects.get();
		T t = liveObjects.findInContext(clazz, id);
		if (t == null) {
			// Not in context; may well be in database
			t = itemService.findItem(auth, clazz, id);
			if (t != null) {
				liveObjects.notifyLoaded(t, id);
			}
		}
		return t;
	}

	@Override
	public <T> T loadDomainObject(Class<T> clazz, Object domainId) {
		// TODO Auto-generated method stub
		return super.loadDomainObject(clazz, domainId);
	}

	@Override
	public List<Object> loadDomainObjects(List<Class<?>> classes, List<Object> domainIds) {
		// TODO Auto-generated method stub
		return super.loadDomainObjects(classes, domainIds);
	}

	@Override
	public boolean requiresServiceLocator(Method contextMethod, Method domainMethod) {
		// TODO Auto-generated method stub
		return super.requiresServiceLocator(contextMethod, domainMethod);
	}

	@Override
	public Class<? extends BaseProxy> resolveClass(String typeToken) {
		// TODO Auto-generated method stub
		return super.resolveClass(typeToken);
	}

	@Override
	public <T> Class<? extends T> resolveClientType(Class<?> domainClass, Class<T> clientType, boolean required) {
		// TODO Auto-generated method stub
		return super.resolveClientType(domainClass, clientType, required);
	}

	@Override
	public Class<?> resolveDomainClass(Class<?> clazz) {
		// TODO Auto-generated method stub
		return super.resolveDomainClass(clazz);
	}

	@Override
	public Method resolveDomainMethod(String operation) {
		// TODO Auto-generated method stub
		return super.resolveDomainMethod(operation);
	}

	@Override
	public Class<? extends Locator<?, ?>> resolveLocator(Class<?> domainType) {
		// TODO Auto-generated method stub
		return super.resolveLocator(domainType);
	}

	@Override
	public Class<? extends RequestContext> resolveRequestContext(String operation) {
		// TODO Auto-generated method stub
		return super.resolveRequestContext(operation);
	}

	@Override
	public Method resolveRequestContextMethod(String operation) {
		// TODO Auto-generated method stub
		return super.resolveRequestContextMethod(operation);
	}

	@Override
	public Class<? extends RequestFactory> resolveRequestFactory(String binaryName) {
		// TODO Auto-generated method stub
		return super.resolveRequestFactory(binaryName);
	}

	@Override
	public Class<?> resolveServiceClass(Class<? extends RequestContext> requestContextClass) {
		// TODO Auto-generated method stub
		return super.resolveServiceClass(requestContextClass);
	}

	@Override
	public Class<? extends ServiceLocator> resolveServiceLocator(Class<? extends RequestContext> requestContext) {
		// TODO Auto-generated method stub
		return super.resolveServiceLocator(requestContext);
	}

	@Override
	public String resolveTypeToken(Class<? extends BaseProxy> proxyType) {
		// TODO Auto-generated method stub
		return super.resolveTypeToken(proxyType);
	}

	@Override
	public void setProperty(Object domainObject, String property, Class<?> expectedType, Object value) {
		Class<? extends Object> domainClass = domainObject.getClass();

		XmlAccessorType xmlAccessorType = domainClass.getAnnotation(XmlAccessorType.class);
		if (xmlAccessorType != null && xmlAccessorType.value() == XmlAccessType.FIELD) {
			Field field = null;
			try {
				field = domainClass.getField(property);
			} catch (SecurityException e) {
				throw new IllegalStateException("Unexpected error while setting field", e);
			} catch (NoSuchFieldException e) {
				field = null;
			}
			if (field != null) {
				try {
					field.set(domainObject, value);
					return;
				} catch (IllegalAccessException e) {
					throw new IllegalStateException("Unexpected error while setting field", e);
				}
			}
		}

		super.setProperty(domainObject, property, expectedType, value);
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validate(T domainObject) {
		// TODO Auto-generated method stub
		return super.validate(domainObject);
	}

}
