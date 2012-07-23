package org.platformlayer.xaas.web.resources;

import java.util.HashMap;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.codehaus.jettison.mapped.Configuration;
import org.eclipse.jetty.server.Response;
import org.platformlayer.CastUtils;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ModelKey;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.model.AuthenticationCredentials;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.OpsSystem;
import org.platformlayer.web.ResourceBase;
import org.platformlayer.xaas.repository.ManagedItemRepository;
import org.platformlayer.xaas.repository.ServiceAuthorizationRepository;
import org.platformlayer.xaas.services.ChangeQueue;
import org.platformlayer.xaas.services.ModelClass;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xaas.services.ServiceProviderDictionary;
import org.platformlayer.xaas.web.jaxrs.JaxbContextHelper;
import org.platformlayer.xml.JsonHelper;
import org.platformlayer.xml.XmlHelper;
import org.platformlayer.xml.XmlHelper.ElementInfo;

import com.google.common.collect.Maps;

public class XaasResourceBase extends ResourceBase {
	static final Logger log = Logger.getLogger(XaasResourceBase.class);

	@Inject
	protected OpsSystem opsSystem;

	@Inject
	protected ManagedItemRepository repository;

	@Inject
	protected ServiceAuthorizationRepository authorizationRepository;

	@Inject
	protected ServiceProviderDictionary serviceDictionary;

	@Inject
	protected ChangeQueue changeQueue;

	@Inject
	protected JaxbContextHelper jaxbContextHelper;

	protected ItemBase getManagedItem() throws RepositoryException {
		return getManagedItem(true);
	}

	protected void raiseNotFound() {
		throw new WebApplicationException(Response.SC_NOT_FOUND);
	}

	protected ItemBase getManagedItem(boolean fetchTags) throws RepositoryException {
		PlatformLayerKey modelKey = getPlatformLayerKey();

		ItemBase managedItem = repository.getManagedItem(modelKey, fetchTags, getSecretProvider());
		if (managedItem == null) {
			raiseNotFound();
		}

		return managedItem;
	}

	protected SecretProvider getSecretProvider() {
		return SecretProvider.from(getProjectAuthorization());
	}

	// protected OpsAuthentication getOpsAuthentication() {
	// OpsAuthentication opsAuth = getScopeParameter(OpsAuthentication.class, true);
	// return opsAuth;
	// }

	protected AuthenticationCredentials getAuthenticationCredentials() {
		AuthenticationCredentials auth = getScopeParameter(AuthenticationCredentials.class, false);
		if (auth == null) {
			throw new WebApplicationException(HttpServletResponse.SC_FORBIDDEN);
		}
		return auth;
	}

	protected ProjectAuthorization getProjectAuthorization() {
		ProjectAuthorization project = getScopeParameter(ProjectAuthorization.class, true);
		return project;
	}

	protected ProjectId getProject() {
		ProjectId project = getScopeParameter(ProjectId.class, true);
		return project;
	}

	// protected void checkLoggedInAsAdmin() {
	// checkIsInRole(RoleId.ADMIN);
	// }

	// protected void checkIsInRole(RoleId role) {
	// if (isInRole(role)) {
	// return;
	// }
	//
	// // TODO: Proper role hierarchy
	// if (isInRole(RoleId.ADMIN)) {
	// return;
	// }
	//
	// throw new WebApplicationException(401);
	// }

	// protected boolean isInRole(RoleId role) {
	// OpsAuthentication auth = getAuthentication();
	//
	// ProjectId project = getProject();
	// return auth.isInRole(project, role);
	// }

	// protected void verifyLoggedInAs(AccountId accountId) {
	// AccountId currentUser = getAccountId();
	// if (currentUser == null || !currentUser.equals(accountId))
	// throw new WebApplicationException(401);
	// }
	// if (!isLoggedInAsAdmin())
	// throw new WebApplicationException(401);
	// }
	// }
	// protected boolean isLoggedInAsAdmin() {
	// AccountId currentUser = getAccountId();
	// return AccountRights.isAdmin(currentUser);
	// }

	protected ServiceType getServiceType() {
		return getScopeParameter(ServiceType.class, true);
	}

	protected ItemType getItemType() {
		return getScopeParameter(ItemType.class, true);
	}

	protected ServiceProvider getServiceProvider() {
		return serviceDictionary.getServiceProvider(getServiceType());
	}

	protected ModelClass<?> getModelClass() {
		ServiceProvider serviceProvider = getServiceProvider();
		if (serviceProvider == null) {
			log.warn("Unknown service");
			raiseNotFound();
		}
		ModelClass<?> modelClass = serviceProvider.getModels().find(getItemType());
		if (modelClass == null) {
			log.warn("Unknown itemtype: " + getItemType());
			raiseNotFound();
		}

		return modelClass;
	}

	protected ManagedItemId getItemId() {
		return getScopeParameter(ManagedItemId.class, true);
	}

	// protected OpsContext buildOpsContext(PlatformLayerKey jobKey) throws OpsException {
	// OpsContextBuilder opsContextBuilder = objectInjector.getInstance(OpsContextBuilder.class);
	// return opsContextBuilder.buildOpsContext(getServiceType(), getAuthentication(), jobKey);
	// }

	protected Class<?> getJavaClass(ModelKey modelKey) {
		return opsSystem.getJavaClass(modelKey);
	}

	protected PlatformLayerKey getPlatformLayerKey() {
		return new PlatformLayerKey(null, getProject(), getServiceType(), getItemType(), getItemId());
	}

	protected <T extends ItemBase> T readItem(String json) throws XMLStreamException, JAXBException {
		Class<T> itemClass = (Class<T>) getModelClass().getJavaClass();

		JAXBContext jaxbContext = jaxbContextHelper.getJaxbContext(itemClass);

		ElementInfo elementInfo = XmlHelper.getXmlElementInfo(itemClass);
		if (elementInfo == null) {
			throw new IllegalStateException("Cannot determine XML info for: " + itemClass);
		}

		final HashMap<String, String> xmlNamespaceToJsonPrefix = Maps.newHashMap();
		xmlNamespaceToJsonPrefix.put(elementInfo.namespace, "");
		xmlNamespaceToJsonPrefix.put("http://platformlayer.org/core/v1.0", "core");

		Configuration configuration = JsonHelper.buildConfiguration(xmlNamespaceToJsonPrefix);

		XMLStreamReader xmlStreamReader = JsonHelper.buildStreamReader(json, configuration);

		// if (log.isDebugEnabled()) {
		// String xml = XmlHelper.toXml(xmlStreamReader);
		// log.debug("XML = " + xml);
		// xmlStreamReader = XmlHelper.buildXmlStreamReader(xml);
		// }

		Object item = XmlHelper.unmarshal(jaxbContext, xmlStreamReader);

		T typedItem = CastUtils.checkedCast(item, itemClass);
		return typedItem;
	}
}
