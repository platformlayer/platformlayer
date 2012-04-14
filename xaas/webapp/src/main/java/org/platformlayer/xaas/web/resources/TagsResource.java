package org.platformlayer.xaas.web.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.TagChanges;
import org.platformlayer.core.model.Tags;
import org.platformlayer.xaas.services.ModelClass;

public class TagsResource extends XaasResourceBase {
	@GET
	@Produces({ XML, JSON })
	public Tags listTags() throws RepositoryException {
		boolean fetchTags = true;
		ItemBase managedItem = getManagedItem(fetchTags);
		return managedItem.getTags();
	}

	@POST
	@Consumes({ XML, JSON })
	@Produces({ XML, JSON })
	public Tags changeTags(TagChanges changeTags) throws RepositoryException {
		ModelClass<?> modelClass = getModelClass();

		return repository.changeTags(modelClass, getProject(), getItemId(), changeTags);
	}
}
