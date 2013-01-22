package org.platformlayer.xaas.web.resources;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;

import org.eclipse.jetty.server.Response;
import org.platformlayer.RepositoryException;
import org.platformlayer.auth.crypto.SecretProvider;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.core.model.Tags;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

public class XaasResourceBase extends ResourceBase {
	static final Logger log = LoggerFactory.getLogger(XaasResourceBase.class);

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

	@Override
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

	@Override
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

	protected <T extends ItemBase> T cleanup(T item) {
		if (item != null) {
			cleanup(item.getTags());
		}
		return item;
	}

	protected <T extends ItemBase> ManagedItemCollection<T> cleanup(ManagedItemCollection<T> collection) {
		if (collection != null) {
			if (collection.items != null) {
				for (T item : collection.items) {
					cleanup(item);
				}

				Collections.sort(collection.items, Ordering.natural().onResultOf(new Function<T, String>() {
					@Override
					public String apply(T input) {
						PlatformLayerKey key = input.getKey();
						if (key == null)
							return "";
						return key.getUrl();
					}
				}));
			}
		}
		return collection;
	}

	protected void cleanup(Tags tags) {
		if (tags == null)
			return;
		List<Tag> tagList = tags.getTags();
		if (tagList == null)
			return;
		Collections.sort(
				tagList,
				Ordering.natural().onResultOf(Tag.GET_TAG_KEY)
						.compound(Ordering.natural().onResultOf(Tag.GET_TAG_VALUE)));
	}
}
