package org.platformlayer.service.solr;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.ServiceProviderBase;
import org.platformlayer.service.solr.model.SolrSchemaField;
import org.platformlayer.xaas.Service;

@Service("solr")
public class SolrServiceProvider extends ServiceProviderBase {

	@Override
	public void beforeCreateItem(ItemBase item) throws OpsException {
		if (item instanceof SolrSchemaField) {
			SolrSchemaField field = (SolrSchemaField) item;
			if (field.name == null) {
				field.name = field.getId();
			}

			if (field.name == null) {
				throw new OpsException("name is required");
			}

			if (field.type == null) {
				throw new OpsException("type is required");
			}
		}

		super.beforeCreateItem(item);
	}

}
