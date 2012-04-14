package org.platformlayer.ops.templates;

import org.platformlayer.core.model.Secret;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class PlatformLayerObjectWrapper extends DefaultObjectWrapper {
	public PlatformLayerObjectWrapper() {
		setExposeFields(true);
	}

	@Override
	public TemplateModel wrap(Object obj) throws TemplateModelException {
		if (obj instanceof javax.inject.Provider<?>) {
			Object provided = ((javax.inject.Provider<?>) obj).get();
			return wrap(provided);
		}

		if (obj instanceof Secret) {
			Secret secret = (Secret) obj;
			String plaintext = secret.plaintext();
			return wrap(plaintext);
		}

		return super.wrap(obj);
	}

}
