package org.platformlayer.client.cli.commands;

import java.io.PrintWriter;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.platformlayer.Format;
import org.platformlayer.PlatformLayerClient;
import org.platformlayer.PlatformLayerClientException;
import org.platformlayer.UntypedItemJson;
import org.platformlayer.UntypedItemXml;
import org.platformlayer.client.cli.PlatformLayerCliContext;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.core.model.ServiceInfo;
import org.platformlayer.ids.FederationKey;
import org.platformlayer.ids.ItemType;
import org.platformlayer.ids.ProjectId;
import org.platformlayer.ids.ServiceType;
import org.platformlayer.jobs.model.JobDataList;
import org.platformlayer.xml.DomUtils;

import com.fathomdb.cli.commands.Ansi;
import com.fathomdb.cli.commands.CommandRunnerBase;
import com.fathomdb.cli.commands.CommandSpecifier;

public abstract class PlatformLayerCommandRunnerBase extends CommandRunnerBase {
	public static final String NAMESPACE_URI_CORE = "http://platformlayer.org/core/v1.0";

	protected PlatformLayerCommandRunnerBase(String verb, String noun) {
		super(verb, noun);
	}

	protected PlatformLayerCommandRunnerBase(CommandSpecifier commandSpecifier) {
		super(commandSpecifier);
	}

	protected PlatformLayerClient getPlatformLayerClient() {
		return getContext().getPlatformLayerClient();
	}

	@Override
	protected PlatformLayerCliContext getContext() {
		return (PlatformLayerCliContext) super.getContext();
	}

	@Override
	public Object convertToOutputFormat(Object results) {
		if (results instanceof JobDataList) {
			return ((JobDataList) results).getJobs();
		}

		return results;
	}

	protected ProjectId getProject() {
		return getContext().getProject();
	}

	protected static String getServiceTypeFromItemType(PlatformLayerClient client, String itemType)
			throws PlatformLayerClientException {
		Iterable<ServiceInfo> serviceInfo = client.listServices(true);
		for (ServiceInfo service : serviceInfo) {
			for (String type : service.itemTypes) {
				if (type.equals(itemType)) {
					return service.serviceType;
				}
			}
		}
		throw new PlatformLayerClientException("Cannot find service for item: " + itemType);
	}

	public static PlatformLayerKey pathToKey(PlatformLayerClient client, String path)
			throws PlatformLayerClientException {
		String serviceType;
		String itemType;
		if (path.contains("/")) {
			String[] components = path.split("/");
			if (components.length != 2) {
				throw new IllegalArgumentException("Cannot parse path: " + path);
			}
			serviceType = components[0];
			itemType = components[1];
		} else {
			itemType = path;
			serviceType = getServiceTypeFromItemType(client, itemType);
		}

		FederationKey host = null;
		ProjectId project = client.getProject();
		return new PlatformLayerKey(host, project, new ServiceType(serviceType), new ItemType(itemType), null);
	}

	protected Format getFormat() {
		switch (getOutputFormat()) {
		case Json:
			return Format.JSON;

		default:
			return Format.XML;
		}
	}

	@Override
	public void formatRaw(Object o, PrintWriter writer) {

		String data;
		if (o instanceof UntypedItemXml) {
			UntypedItemXml item = (UntypedItemXml) o;

			Source src = new DOMSource(item.getRoot());
			String xml = DomUtils.toXml(src, 4);
			data = xml;
		} else if (o instanceof UntypedItemJson) {
			UntypedItemJson item = (UntypedItemJson) o;

			JSONObject root = item.getRoot();
			try {
				data = root.toString(2);
			} catch (JSONException e) {
				throw new IllegalStateException("Error formatting JSON", e);
			}
		} else {
			super.formatRaw(o, writer);
			return;
		}

		Ansi ansi = new Ansi(writer);

		ansi.print(data);
		ansi.println();

		ansi.reset();
	}

}
