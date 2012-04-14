package org.platformlayer.web;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.platformlayer.Scope;
import org.platformlayer.inject.ObjectInjector;

public class ResourceBase {
	@Inject
	protected ObjectInjector objectInjector;

	public static final String JSON = javax.ws.rs.core.MediaType.APPLICATION_JSON;
	public static final String XML = javax.ws.rs.core.MediaType.APPLICATION_XML;
	public static final String TEXT_PLAIN = javax.ws.rs.core.MediaType.TEXT_PLAIN;

	// This horrendous hack is because only the root JAX-RS handler is managed by the JAX-RS runtime,
	// and therefore only it has @Context resources injected (an outrageous design flaw IMHO)
	// However, Spring doesn't seem to be able to inject the JAX-RS resource
	// (outrageous design flaw #2 - JAX-RS rolls-its-own injection system)
	// It's probably OK because it's all hidden in this (evil) base class. Evil code is less bad if we hide it!
	// TODO: Can we make this better ???
	static ResourceBase root = null;

	protected void notifyRootResource() {
		if (ResourceBase.root != null) {
			throw new IllegalStateException("Multiple root resources - make sure the root is in singleton scope");
		}

		ResourceBase.root = this;
	}

	private ResourceBase requireRootResource() {
		if (root == null) {
			throw new IllegalStateException("Root resource must be set / must override required getters");
		}
		return root;
	}

	/**
	 * Returns the URI info... root resource must override
	 */
	protected UriInfo getUriInfo() {
		return requireRootResource().getUriInfo();
	}

	/**
	 * Returns the HttpServletRequest... root resource must override
	 */
	protected HttpServletRequest getRequest() {
		return requireRootResource().getRequest();
	}

	@Context
	void setRequestHeaders(HttpHeaders requestHeaders) {
		getScope().put(requestHeaders);
	}

	protected HttpHeaders getRequestHeaders() {
		return getScopeParameter(HttpHeaders.class, true);
	}

	// protected <T> void setScopeParameter(T t) {
	// getScope().put(t);
	// }

	protected <T> T getScopeParameter(Class<T> clazz, boolean required) {
		Scope contextMap = Scope.get();
		T t = contextMap.get(clazz);
		if (required && t == null) {
			throw new IllegalArgumentException(clazz.getSimpleName() + " is required");
		}
		return t;
	}

	protected Scope getScope() {
		// TODO: Do we want to ensure we have a per-request scope?
		// (neither too broad nor too narrow)
		return Scope.get();
	}

	// private TypedMap getRequestContextMap() {
	// HttpServletRequest request = getRequest();
	// return TypedMap.get(request);
	// }
}
