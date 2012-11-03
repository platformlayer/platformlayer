package org.platformlayer.xaas.web.resources;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import org.json.JSONException;
import org.json.JSONObject;
import org.platformlayer.RepositoryException;
import org.platformlayer.core.model.Action;
import org.platformlayer.core.model.BackupAction;
import org.platformlayer.core.model.ConfigureAction;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ValidateAction;
import org.platformlayer.jobs.model.JobData;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.tasks.JobRegistry;
import org.platformlayer.xaas.services.ServiceProviderDictionary;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class ActionsResource extends XaasResourceBase {
	@Inject
	JobRegistry jobRegistry;

	@Inject
	JsonMapper jsonMapper;

	@POST
	@Consumes({ XML })
	@Produces({ XML, JSON })
	public JobData doActionXml(Action action) throws RepositoryException, OpsException {
		return doAction(action);
	}

	@POST
	@Consumes({ JSON })
	@Produces({ XML, JSON })
	public JobData doActionJson(String json) throws IOException, RepositoryException, OpsException {
		JSONObject jsonObject;
		String actionType = null;
		try {
			jsonObject = new JSONObject(json);
			Object typeObject = jsonObject.remove("type");
			if (typeObject != null) {
				actionType = typeObject.toString();
			}

			// Remove type attribute
			json = jsonObject.toString();
		} catch (JSONException e) {
			throw new IllegalArgumentException("Malformed JSON", e);
		}

		if (Strings.isNullOrEmpty(actionType)) {
			throw new IllegalArgumentException("Must pass type attribute");
		}

		Map<String, Class<? extends Action>> actionMap = Maps.newHashMap();
		actionMap.put("configureaction", ConfigureAction.class);
		actionMap.put("validateaction", ValidateAction.class);
		// actionMap.put("deleteaction", DeleteAction.class);
		actionMap.put("backupaction", BackupAction.class);

		for (Class<? extends Action> action : getServiceProvider().getActions()) {
			String key = action.getSimpleName().toLowerCase();
			actionMap.put(key, action);
		}

		Class<? extends Action> actionClass = actionMap.get(actionType.toLowerCase());
		if (actionClass == null) {
			throw new IllegalArgumentException("Unknown action: " + actionType);
		}

		Action action = jsonMapper.readItem(actionClass, json);
		return doAction(action);
	}

	@Inject
	ServiceProviderDictionary serviceProviderDictionary;

	private JobData doAction(Action action) throws RepositoryException, OpsException {
		boolean fetchTags = true;
		// Check we can get the item
		ItemBase managedItem = getManagedItem(fetchTags);

		// String actionName = action.getName();
		// if (Strings.isNullOrEmpty(actionName)) {
		// actionName = action.getClass().getSimpleName();
		// // throw new IllegalArgumentException("Action is required");
		// action.name = actionName;
		// }
		// OperationType operationType = EnumUtils.valueOfCaseInsensitive(OperationType.class, actionName);
		PlatformLayerKey itemKey = getPlatformLayerKey();
		PlatformLayerKey jobKey = jobRegistry.enqueueOperation(action, getProjectAuthorization(), itemKey);

		JobData jobData = new JobData();
		jobData.key = jobKey;
		return jobData;
	}
}
