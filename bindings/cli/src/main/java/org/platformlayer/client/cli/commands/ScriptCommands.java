package org.platformlayer.client.cli.commands;

import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.client.cli.PlatformLayerCliContext;

import com.fathomdb.cli.commands.CommandRunner;
import com.fathomdb.cli.output.FormattedList;
import com.google.common.collect.Lists;

public class ScriptCommands {
	public static Object listServices() throws Exception {
		ListServices listServices = new ListServices();
		return runCommand(listServices);
	}

	private static Object runCommand(CommandRunner commandRunner) throws Exception {
		// PlatformLayerCliContext context = PlatformLayerCliContext.get();
		Object ret = commandRunner.runCommand();
		return toPython(ret);
	}

	public static Object activateService(String serviceType, Object properties) throws PlatformLayerClientException,
			JSONException {
		PlatformLayerCliContext context = PlatformLayerCliContext.get();
		PlatformLayerClient client = context.getPlatformLayerClient();
		String json = properties.toString();
		String wrapper = "{ \"data\": \"" + json + "\" }";
		String retvalJsonString = client.activateService(serviceType, wrapper, Format.JSON);
		JSONObject retvalJsonObject = new JSONObject(retvalJsonString);
		return toPython(retvalJsonObject);
	}

	public static Object getActivation(String serviceType) throws PlatformLayerClientException, JSONException {
		PlatformLayerCliContext context = PlatformLayerCliContext.get();
		PlatformLayerClient client = context.getPlatformLayerClient();
		String retvalJsonString = client.getActivation(serviceType, Format.JSON);
		JSONObject retvalJsonObject = new JSONObject(retvalJsonString);
		return toPython(retvalJsonObject);
	}

	public static Object getSshPublicKey(String serviceType) throws PlatformLayerClientException, JSONException {
		PlatformLayerCliContext context = PlatformLayerCliContext.get();
		PlatformLayerClient client = context.getPlatformLayerClient();
		String key = client.getSshPublicKey(serviceType);
		return toPython(key);
	}

	public static Object getSchema(String serviceType) throws PlatformLayerClientException, JSONException {
		PlatformLayerCliContext context = PlatformLayerCliContext.get();
		PlatformLayerClient client = context.getPlatformLayerClient();
		String retval = client.getSchema(serviceType, Format.XML);
		return retval;
	}

	// public static Object create(String serviceType, String objectType, Object item) throws
	// PlatformLayerClientException, JSONException {
	// return create(serviceType, objectType, item, null);
	// }

	// public static Object create(String serviceType, String itemType, Object item, PyDictionary tags) throws
	// PlatformLayerClientException, JSONException {
	// PlatformLayerCliContext context = PlatformLayerCliContext.get();
	// PlatformLayerClient client = context.getPlatformLayerClient();
	//
	// if (tags != null) {
	// PyDictionary dictionary = (PyDictionary) item;
	// PyDictionary itemTags = (PyDictionary) dictionary.get("core.tags");
	// if (itemTags == null) {
	// itemTags = new PyDictionary();
	// dictionary.put("core.tags", itemTags);
	// }
	// PyList itemTagsList = (PyList) itemTags.get("core.tags");
	// if (itemTagsList == null) {
	// itemTagsList = new PyList();
	// itemTags.put("core.tags", itemTagsList);
	// }
	// for (Object entryObj : tags.entrySet()) {
	// Map.Entry entry = (Entry) entryObj;
	// Object key = entry.getKey();
	// Object value = entry.getValue();
	// PyDictionary tag = new PyDictionary();
	// tag.put("core.key", key);
	// tag.put("core.value", value);
	// itemTagsList.add(tag);
	// }
	// }
	//
	// JythonToJson formatter = new JythonToJson();
	// formatter.append(item);
	//
	// String json = formatter.getJson();
	// String wrapper = "{ \"" + itemType + "\": " + json + " }";
	// String retvalJsonString = client.createItem(new ServiceType(serviceType), new ItemType(itemType), wrapper,
	// Format.JSON);
	// JSONObject retvalJsonObject = new JSONObject(retvalJsonString);
	// return toPython(retvalJsonObject);
	// }

	public static Object listItems(String serviceType, String itemType) throws Exception {
		ListItems command = new ListItems();
		command.path = serviceType + "/" + itemType;
		return runCommand(command);
	}

	// public static Object listMetrics(String serviceType, String itemType, String itemKey) throws Exception {
	// return runCommand(new ListMetrics(serviceType, itemType, itemKey));
	// }

	// public static Object doAction(String serviceType, String itemType, String itemKey, String action) throws
	// Exception {
	// DoAction cmd = new DoAction();
	// cmd.path = serviceType + "/" + itemType + "/" + itemKey;
	// cmd.action = action;
	// return runCommand(cmd);
	// }

	// public static Object getMetric(String serviceType, String itemType, String itemKey, String metricKey) throws
	// Exception {
	// return runCommand(new GetMetric(serviceType, itemType, itemKey, metricKey));
	// }

	public static Object toPython(Object in) {
		if (in instanceof Iterable) {
			Iterable<?> list = (Iterable<?>) in;
			PlatformLayerCliContext context = PlatformLayerCliContext.get();
			List<Object> pythonObjects = Lists.newArrayList();
			for (Object item : list) {
				pythonObjects.add(toPython(item));
			}
			return FormattedList.build(context, pythonObjects, true);
		}

		// if (in instanceof UntypedItemCollection) {
		// UntypedItemCollection list = (UntypedItemCollection) in;
		// List<Object> python = new FormattedList<Object>();
		// for (UntypedItem item : list) {
		// python.add(toPython(item));
		// }
		// return python;
		// }

		if (in == null) {
			return null;
		}

		return in;
	}

}
