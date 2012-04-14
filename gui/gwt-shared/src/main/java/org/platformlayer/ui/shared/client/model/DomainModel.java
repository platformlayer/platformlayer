package org.platformlayer.ui.shared.client.model;

import java.util.List;

import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import com.google.gwt.editor.client.Editor;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestFactory;

public abstract class DomainModel<P, Context extends BaseEntityRequest<P>> {
	final Class<P> proxyClass;

	public DomainModel(Class<P> proxyClass) {
		super();
		this.proxyClass = proxyClass;
	}

	// static <P> DomainHelper<P> get(Class<P> proxyClass) {
	// throw new UnsupportedOperationException();
	// }

	public abstract Editor<P> buildEditor();

	public Request<List<P>> findAll(RequestFactory requestFactory) {
		return context(requestFactory).findAll();
	}

	public Class<P> getProxyClass() {
		return proxyClass;
	}

	public abstract Context context(RequestFactory requestFactory);

	public abstract void persist(Context context, P item);

	public abstract RequestFactoryEditorDriver<P, Editor<P>> buildEditorDriver();
}
