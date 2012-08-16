package org.platformlayer.xaas;

import java.util.Map;

import org.platformlayer.Scope;
import org.platformlayer.ScopeFilter;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.web.CORSFilter;
import org.platformlayer.xaas.web.jaxrs.MetricDataSourceWriter;
import org.platformlayer.xaas.web.resources.RootResource;

import com.google.common.collect.Maps;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class PlatformLayerServletModule extends JerseyServletModule {
	@Override
	protected void configureServlets() {
		// switch (ApplicationMode.getMode()) {
		// case DEVELOPMENT:
		// bind(OpenstackAuthenticationFilterBase.class).to(DevelopmentOpenstackAuthenticationFilter.class).in(Scopes.SINGLETON);
		// break;
		//
		// default:
		// throw new IllegalStateException("Unhandled application mode: " + ApplicationMode.getMode());
		// }

		bind(MetricDataSourceWriter.class);

		bind(CORSFilter.class).asEagerSingleton();
		filter("/*").through(CORSFilter.class);

		bind(ScopeFilter.class).asEagerSingleton();
		filter("/*").through(ScopeFilter.class);
		bind(ProjectAuthorization.class).toProvider(ScopeProjectAuthorizationProvider.class);
		bind(Scope.class).toProvider(ScopeProvider.class);

		// if (ApplicationMode.isDevelopment()) {
		// bind(AuthenticationTokenValidator.class).to(DevelopmentTokenValidator.class);
		// } else {
		// bind(AuthenticationTokenValidator.class).to(PlatformLayerTokenValidator.class);
		// }

		bind(OpsAuthenticationFilter.class).asEagerSingleton();

		// /* bind the REST resources */
		// bind(ManagedItemCollectionResource.class);
		// bind(ManagedItemResource.class);
		bind(RootResource.class);
		// bind(ServiceAuthorizationCollectionResource.class);
		// bind(ServiceAuthorizationResource.class);
		// bind(ServiceResource.class);
		// bind(ServicesCollectionResource.class);

		// /* bind jackson converters for JAXB/JSON serialization */
		// bind(MessageBodyReader.class).to(JacksonJsonProvider.class);
		// bind(MessageBodyWriter.class).to(JacksonJsonProvider.class);

		filter("/v0/*").through(OpsAuthenticationFilter.class);

		Map<String, String> params = Maps.newHashMap();
		params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
				"org.platformlayer.xaas.web.jaxrs;org.platformlayer.xaas.web.resources;org.codehaus.jackson.jaxrs");
		serve("/v0/*").with(GuiceContainer.class, params);
		// ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));

	}
}
