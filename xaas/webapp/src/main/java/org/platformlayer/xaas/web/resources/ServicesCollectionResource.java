package org.platformlayer.xaas.web.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.core.model.ServiceInfoCollection;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.ops.OpsException;

import com.google.common.collect.Lists;

public class ServicesCollectionResource extends XaasResourceBase {

	@Path("authorizations")
	public ServiceAuthorizationCollectionResource getServiceAuthorizationsResource() {
		ServiceAuthorizationCollectionResource child = objectInjector
				.getInstance(ServiceAuthorizationCollectionResource.class);
		return child;
	}

	@Path("jobs")
	public JobsResource getJobs() {
		JobsResource child = objectInjector.getInstance(JobsResource.class);
		return child;
	}

	@Inject
	ItemService itemService;

	@GET
	@Produces({ XML, JSON })
	@Path("roots")
	public ManagedItemCollection<ItemBase> listRoots() throws OpsException {
		List<ItemBase> roots = itemService.findRoots(getProjectAuthorization());
		ManagedItemCollection<ItemBase> collection = new ManagedItemCollection<ItemBase>();
		collection.items = roots;
		return collection;
	}

	// @Path("create")
	// @POST
	// @Produces({ APPLICATION_XML, APPLICATION_JSON })
	// public <T> TypedManagedItem<T> createService(String data) throws RepositoryException, OpsException {
	// // ServiceProvider serviceProvider = getServiceProvider();
	// // ItemType itemType = getItemType();
	//
	// Document document;
	// try {
	// document = XmlHelper.parseXmlDocument(data, false);
	// } catch (ParserConfigurationException e) {
	// throw new IllegalArgumentException("Cannot parse XML data", e);
	// } catch (SAXException e) {
	// throw new IllegalArgumentException("Cannot parse XML data", e);
	// } catch (IOException e) {
	// throw new IllegalArgumentException("Cannot parse XML data", e);
	// }
	// Element documentElement = document.getDocumentElement();
	// String namespaceURI = documentElement.getNamespaceURI();
	// ModelClass<?> modelClass = getModelClass(namespaceURI, documentElement.getNodeName());
	// if (modelClass.isSystemObject()) {
	// checkLoggedInAsAdmin();
	// }
	//
	// TypedManagedItem<T> service = TypedManagedItem.build(modelClass, data);
	//
	// // Managed service = new Managed();
	// // service.setModelClass(modelClass);
	// // service.setModelData(data);
	//
	// return serviceUtils.createItem(getAuthentication(), service);
	// }

	// @Path("list")
	// @GET
	// @Produces({ APPLICATION_XML, APPLICATION_JSON })
	// public ManagedItemCollection<?> listServices(@QueryParam("namespace") String namespace, @QueryParam("element")
	// String nodeName) throws RepositoryException {
	// ModelClass<?> modelClass = getModelClass(namespace, nodeName);
	// if (modelClass.isSystemObject()) {
	// checkLoggedInAsAdmin();
	// }
	//
	// return serviceUtils.listItems(getProject(), modelClass);
	// }

	@Path("{serviceType}")
	public ServiceResource getServiceResource(@PathParam("serviceType") String serviceType) {
		getScope().put(new ServiceType(serviceType));

		ServiceResource resource = objectInjector.getInstance(ServiceResource.class);
		return resource;
	}

	@GET
	@Produces({ XML, JSON })
	public ServiceInfoCollection getMetadata() {
		// boolean management = isInRole(RoleId.ADMIN);

		ServiceInfoCollection collection = new ServiceInfoCollection();
		collection.services = Lists.newArrayList();
		collection.services.addAll(serviceDictionary.getAllServices());
		return collection;
	}

}
