package org.platformlayer.xaas.web.resources;

import java.util.Iterator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBException;

import org.json.JSONException;
import org.json.JSONObject;
import org.platformlayer.RepositoryException;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xaas.model.ServiceAuthorization;
import org.platformlayer.xaas.model.Setting;
import org.platformlayer.xaas.model.SettingCollection;
import org.platformlayer.xaas.services.ServiceProvider;
import org.platformlayer.xml.JaxbHelper;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class ServiceAuthorizationResource extends XaasResourceBase {
	@GET
	@Produces({ XML, JSON })
	public ServiceAuthorization retrieveItem() throws RepositoryException {
		ServiceType serviceType = getServiceType();

		ServiceAuthorization auth = authorizationRepository.findServiceAuthorization(serviceType, getProject());
		if (auth == null) {
			throw new WebApplicationException(404);
		}

		// For security, never return the data
		auth.data = null;

		return auth;
	}

	// We deliberately don't support this at the moment... it's quite restrictive on our data store
	// @GET
	// @Produces({ APPLICATION_XML, APPLICATION_JSON })
	// public <T> ServiceAuthorizationCollection getAll() {
	// List<ServiceAuthorization> items = authorizationRepository.getByAccountId(getAccountId());
	// ServiceAuthorizationCollection collection = new ServiceAuthorizationCollection();
	// collection.items = items;
	// return collection;
	// }

	@POST
	@Consumes({ XML, JSON })
	@Produces({ XML, JSON })
	public <T> ServiceAuthorization createService(final ServiceAuthorization authorization) throws OpsException,
			RepositoryException {
		ServiceType serviceType = getServiceType();
		authorization.serviceType = serviceType.getKey();

		final ServiceProvider serviceProvider = opsSystem.getServiceProvider(serviceType);
		if (serviceProvider == null) {
			log.warn("Unknown serviceProvider: " + serviceType);
			throw new WebApplicationException(404);
		}

		String data = authorization.data;
		if (Strings.isNullOrEmpty(data)) {
			throw new IllegalArgumentException("Data is required");
		}

		data = data.trim();
		if (data.startsWith("{")) {
			// Convert to XML
			SettingCollection settings = new SettingCollection();
			settings.items = Lists.newArrayList();

			// We presume it's a simple map of keys and values
			try {
				JSONObject json = new JSONObject(data);
				@SuppressWarnings("unchecked")
				Iterator<String> keys = json.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					String value = json.getString(key);
					Setting setting = new Setting();
					setting.key = key;
					setting.value = value;
					settings.items.add(setting);
				}
			} catch (JSONException e) {
				throw new IllegalArgumentException("Error parsing data", e);
			}

			JaxbHelper jaxbHelper = JaxbHelper.get(SettingCollection.class);
			String xml;
			try {
				xml = jaxbHelper.marshal(settings, false);
			} catch (JAXBException e) {
				throw new IllegalArgumentException("Error converting JSON to XML", e);
			}
			authorization.data = xml;
		}

		// Authentication authentication = getAuthentication();
		//
		// OpsContextBuilder opsContextBuilder = opsSystem.getInjector().getInstance(OpsContextBuilder.class);
		// final OpsContext opsContext = opsContextBuilder.buildOpsContext(serviceType, authentication, false);
		//
		// OpsContext.runInContext(opsContext, new CheckedCallable<Object, Exception>() {
		// @Override
		// public Object call() throws Exception {
		// serviceProvider.validateAuthorization(authorization);
		// return null;
		// }
		// });

		// serviceProvider.validateAuthorization(authorization);

		ServiceAuthorization created = authorizationRepository.createAuthorization(getProject(), authorization);

		// For security, never return the data
		created.data = null;

		return created;
	}
}
