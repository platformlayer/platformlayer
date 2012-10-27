package org.platformlayer.service.cloud.openstack.ops;

import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.openstack.client.OpenstackAuthenticationException;
import org.openstack.client.OpenstackException;
import org.openstack.client.OpenstackForbiddenException;
import org.openstack.client.OpenstackNotFoundException;
import org.openstack.client.common.HeadResponse;
import org.openstack.client.common.OpenstackSession;
import org.openstack.client.common.RequestBuilder;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.helpers.CurlRequest;
import org.platformlayer.ops.helpers.CurlResult;
import org.platformlayer.xml.JaxbHelper;

import com.google.common.base.Joiner;

public class RemoteCurlOpenstackSession extends OpenstackSession {
	final OpsTarget target;

	public RemoteCurlOpenstackSession(OpsTarget target) {
		super();
		this.target = target;
	}

	@Override
	protected RequestBuilder createRequestBuilder(String resourceUrl) {
		return new RemoteCurlOpenstackRequest(resourceUrl);
	}

	public class RemoteCurlOpenstackRequest extends RequestBuilder {

		public RemoteCurlOpenstackRequest(String resourceUrl) {
			super(RemoteCurlOpenstackSession.this, resourceUrl);
		}

		@Override
		public <T> T doRequest0(Class<T> c) {
			CurlRequest request = toCurlRequest();
			CurlResult result;
			try {
				result = request.executeRequest(target);
			} catch (OpsException e) {
				throw new OpenstackException("Error issuing request", e);
			}

			int httpStatus = result.getHttpResult();

			switch (httpStatus) {
			case 200:
				break;
			case 201:
				// Created
				break;
			case 202:
				// Accepted
				break;

			case 401:
				throw new OpenstackAuthenticationException("Not authorized");

			case 403:
				throw new OpenstackForbiddenException("Forbidden");

			case 404:
				throw new OpenstackNotFoundException("Item not found");

			default:
				throw new OpenstackException("Error processing request. Status code=" + httpStatus);
			}

			if (c == Void.class) {
				return null;
			} else {
				String xml = result.getBody();

				JaxbHelper jaxb = JaxbHelper.get(c);
				try {
					return (T) jaxb.unmarshal(xml);
				} catch (JAXBException e) {
					throw new OpenstackException("Error deserializing response", e);
				}
			}
		}

		public CurlRequest toCurlRequest() {
			CurlRequest request = new CurlRequest(resourceUrl);
			if (body != null) {
				JaxbHelper jaxb = JaxbHelper.get(body.getClass());
				String xml;
				try {
					xml = jaxb.marshal(body, false);
				} catch (JAXBException e) {
					throw new OpenstackException("Error serializing request body", e);
				}
				request.body = xml;
			}
			request.method = this.method;
			if (this.contentType != null) {
				request.getHeaders().put("Content-Type", this.contentType.toString());
			}
			if (this.acceptTypes != null) {
				String accept = Joiner.on(",").join(this.acceptTypes);
				request.getHeaders().put("Accept", accept);
			}
			for (Entry<String, String> entry : this.headers.entrySet()) {
				request.getHeaders().put(entry.getKey(), entry.getValue());
			}
			return request;
		}

		@Override
		public HeadResponse head() {
			throw new UnsupportedOperationException();
		}
	}

	public static RemoteCurlOpenstackSession build(OpsTarget target, OpenstackSession openstackSession) {
		RemoteCurlOpenstackSession session = new RemoteCurlOpenstackSession(target);
		session.access = openstackSession.getAccess();
		return session;
	}

	public CurlRequest toCurlRequest(RequestBuilder request) {
		return ((RemoteCurlOpenstackRequest) request).toCurlRequest();
	}

}
