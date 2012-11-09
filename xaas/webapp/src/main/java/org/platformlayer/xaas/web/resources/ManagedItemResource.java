package org.platformlayer.xaas.web.resources;

import static com.google.common.base.Objects.equal;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;

import org.platformlayer.Filter;
import org.platformlayer.RepositoryException;
import org.platformlayer.TagFilter;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.ManagedItemCollection;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.Tag;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ManagedItemId;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobExecutionData;
import org.platformlayer.ops.OpsException;

public class ManagedItemResource extends XaasResourceBase {
	@Inject
	ItemService itemService;

	@Inject
	JsonMapper jsonMapper;

	@GET
	@Produces({ XML, JSON })
	public ItemBase retrieveItem() throws RepositoryException {
		boolean fetchTags = true;
		ItemBase managedItem = getManagedItem(fetchTags);
		return managedItem;
	}

	@PUT
	@Consumes({ XML })
	@Produces({ XML, JSON })
	public <T extends ItemBase> T createItem(final T item, @QueryParam("unique") String uniqueTag)
			throws RepositoryException, OpsException {
		// ModelClass<T> modelClass = (ModelClass<T>) getModelClass();

		checkItemKey(item);

		// TODO: Does it matter that we're not checking the item type??
		return itemService.putItem(getProjectAuthorization(), item, uniqueTag);
	}

	@PUT
	@Consumes({ JSON })
	@Produces({ XML, JSON })
	public <T extends ItemBase> T createItemJson(String json, @QueryParam("unique") String uniqueTag)
			throws RepositoryException, OpsException, JAXBException, IOException {
		// String json2 = json.replace("{\"value\":\"pl", "{\"value\":\"pl");

		Class<T> javaClass = (Class<T>) getModelClass().getJavaClass();
		T item = jsonMapper.readItem(javaClass, json);

		checkItemKey(item);

		return itemService.putItem(getProjectAuthorization(), item, uniqueTag);
	}

	private void checkItemKey(ItemBase item) throws OpsException {
		PlatformLayerKey key = item.getKey();

		ManagedItemId itemId = getItemId();
		ServiceType serviceType = getServiceType();
		ItemType itemType = getItemType();
		ProjectId project = getProject();

		if (key != null) {
			if (key.getItemId() != null && !equal(key.getItemId(), itemId)) {
				throw new OpsException("Item id mismatch");
			}
			if (key.getServiceType() != null && !equal(key.getServiceType(), serviceType)) {
				throw new OpsException("Service type mismatch");
			}
			if (key.getItemType() != null && !key.getItemType().isEmpty() && !equal(key.getItemType(), itemType)) {
				throw new OpsException("Item type mismatch");
			}
			if (key.getProject() != null && !equal(key.getProject(), project)) {
				throw new OpsException("Project mismatch");
			}
		}

		key = new PlatformLayerKey(null, project, serviceType, itemType, itemId);
		item.setKey(key);
	}

	@DELETE
	@Produces({ XML, JSON })
	public JobExecutionData deleteItem() throws RepositoryException, OpsException {
		PlatformLayerKey key = getPlatformLayerKey();

		return itemService.deleteItem(getProjectAuthorization(), key);
		// PlatformLayerKey jobKey = itemService.deleteItem(getProjectAuthorization(), key);
		//
		// JobData jobData = new JobData();
		// jobData.key = jobKey;
		// jobData.targetId = key;
		// return jobData;
	}

	@GET
	@Produces({ XML, JSON })
	@Path("children")
	public ManagedItemCollection<ItemBase> listChildren() throws OpsException, RepositoryException {
		boolean fetchTags = true;
		ItemBase item = getManagedItem(fetchTags);

		Tag parentTag = Tag.buildParentTag(item.getKey());
		Filter filter = TagFilter.byTag(parentTag);
		List<ItemBase> roots = itemService.listAll(getProjectAuthorization(), filter);
		ManagedItemCollection<ItemBase> collection = new ManagedItemCollection<ItemBase>();
		collection.items = roots;
		return collection;
	}

	@Path("tags")
	@Consumes({ XML, JSON })
	@Produces({ XML, JSON })
	public TagsResource getTags() {
		TagsResource resource = objectInjector.getInstance(TagsResource.class);
		return resource;
	}

	@Path("metrics")
	@Consumes({ XML, JSON })
	@Produces({ XML, JSON })
	public MetricsResource getMetrics() {
		MetricsResource resource = objectInjector.getInstance(MetricsResource.class);
		return resource;
	}

	@Path("actions")
	@Consumes({ XML, JSON })
	@Produces({ XML, JSON })
	public ActionsResource getActions() {
		ActionsResource resource = objectInjector.getInstance(ActionsResource.class);
		return resource;
	}

	// @DELETE
	// public Response deleteDatabase() {
	// try {
	// repository.getByIdAndAccountId(getDatabaseServiceId(), getAccountId());
	// // Throw up an exception if this doesn't exist.
	//
	// DatabaseService db = new DatabaseService();
	// db.setId(getDatabaseServiceId().getId());
	// db.setAccountId(getAccountId().getId());
	//
	// OperationResponse response =
	// esbService.callDatabaseServiceOperation(Operation.DELETE_DATABASESERVICE,
	// db);
	// if (response.isExecutedOkay()) {
	// return Response.status(Response.Status.ACCEPTED).build();
	// } else {
	// return ResponseFactory.getErrorResponse(response);
	// }
	// } catch (Exception e) {
	// return ResponseFactory.getErrorResponse(e, null, null);
	// }
	// }

	// @PUT
	// @Consumes({ APPLICATION_XML, APPLICATION_JSON })
	// // TODO: Allow subtree update, by allowing post to a sub-path, which then
	// aligns to a portion of the XML
	// public Response
	// updateDatabaseService(com.rackspace.cloud.docs.dbaas.api.v1.DatabaseService
	// apiDb) {
	// ValidatorResult result = ValidatorRepository.validate(apiDb,
	// HttpRequestType.PUT);
	// if (!result.passedValidation()) {
	// return
	// Response.status(400).entity(HttpResponseBuilder.buildBadRequestResponse("Validation fault",
	// result.getValidationErrorMessages())).build();
	// }
	//
	// try {
	// DatabaseService db = mapper.fromApi(apiDb);
	// db.setId(getDatabaseServiceId().getId());
	// db.setAccountId(getAccountId().getId());
	// OperationResponse response =
	// esbService.callDatabaseServiceOperation(Operation.UPDATE_DATABASESERVICE,
	// db);
	// if (response.isExecutedOkay()) {
	// return Response.status(Response.Status.ACCEPTED).build();
	// } else {
	// return ResponseFactory.getErrorResponse(response);
	// }
	// } catch (Exception e) {
	// return ResponseFactory.getErrorResponse(e, null, null);
	// }
	// }

}
