package org.platformlayer.xaas.web.resources;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ops.OpsException;
import org.platformlayer.xaas.services.ModelClass;

public class ManagedItemCollectionResource extends XaasResourceBase {
	static final Logger log = Logger.getLogger(ManagedItemCollectionResource.class);

	@Inject
	ItemService itemService;

	@Path("{id}")
	@Produces({ XML, JSON })
	public ManagedItemResource retrieveSingleService(@PathParam("id") String id) {
		ManagedItemId itemId = new ManagedItemId(id);
		getScope().put(itemId);

		ManagedItemResource resource = objectInjector.getInstance(ManagedItemResource.class);
		return resource;
	}

	@GET
	@Produces({ XML, JSON })
	public <T extends ItemBase> ManagedItemCollection<T> listItems() throws OpsException {
		ModelClass<T> modelClass = (ModelClass<T>) getModelClass();

		List<T> listItems = itemService.findAll(getProjectAuthorization(), modelClass.getJavaClass());
		ManagedItemCollection<T> collection = new ManagedItemCollection<T>();
		collection.items = listItems;

		return collection;
	}

	@POST
	@Consumes({ XML })
	@Produces({ XML })
	public <T extends ItemBase> T createItem(final T item) throws RepositoryException, OpsException {
		// ModelClass<T> modelClass = (ModelClass<T>) getModelClass();

		// TODO: Does it matter that we're not checking the item type??
		boolean generateUniqueName = true;
		return itemService.createItem(getProjectAuthorization(), item, generateUniqueName);
	}

	@POST
	@Consumes({ JSON })
	@Produces({ JSON })
	public <T extends ItemBase> T createItemJson(String json) throws RepositoryException, OpsException, JAXBException,
			XMLStreamException {
		T typedItem = readItem(json);

		boolean generateUniqueName = true;
		return itemService.createItem(getProjectAuthorization(), typedItem, generateUniqueName);
	}

	// private Tags deserializeTags(HttpHeaders hh) {
	// Tags tags = new Tags();
	//
	// MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
	// for (Entry<String, List<String>> header : headerParams.entrySet()) {
	// String key = header.getKey();
	// if (!key.startsWith("X-Tag-")) {
	// continue;
	// }
	// key = key.substring(6);
	//
	// for (String value : header.getValue()) {
	// tags.add(key, value);
	// }
	// }
	//
	// return tags;
	// }

}
