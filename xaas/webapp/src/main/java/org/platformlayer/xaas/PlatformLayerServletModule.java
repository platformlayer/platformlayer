package org.platformlayer.xaas;

import java.util.List;
import java.util.Map;

import org.platformlayer.Scope;
import org.platformlayer.ScopeFilter;
import org.platformlayer.model.ProjectAuthorization;
import org.platformlayer.ops.extensions.Extensions;
import org.platformlayer.ops.extensions.HttpConfiguration;
import org.platformlayer.web.CORSFilter;
import org.platformlayer.xaas.web.jaxrs.MetricDataSourceWriter;
import org.platformlayer.xaas.web.jaxrs.ObjectMapperProvider;
import org.platformlayer.xaas.web.resources.RootResource;
import org.platformlayer.xml.JsonHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Scopes;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class PlatformLayerServletModule extends JerseyServletModule {
	private final Extensions extensions;

	public PlatformLayerServletModule(Extensions extensions) {
		super();
		this.extensions = extensions;
	}

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

		extensions.addFilters(new HttpConfiguration() {
			@Override
			public FilterKeyBindingBuilder filter(String urlPattern) {
				return PlatformLayerServletModule.this.filter(urlPattern);
			}
		});

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

		filter("/v0/*").through(OpsAuthenticationFilter.class);

		Map<String, String> params = Maps.newHashMap();
		List<String> packages = Lists.newArrayList();
		packages.add("org.platformlayer.xaas.web.jaxrs");
		packages.add("org.platformlayer.xaas.web.resources");
		// packages.add("org.codehaus.jackson.jaxrs");

		TypeFactory typeFactory = TypeFactory.defaultInstance();
		ObjectMapper objectMapper = JsonHelper.buildObjectMapper(typeFactory, false);

		// TypeResolverBuilder<?> typer = new PlatformLayerTypeResolverBuilder();
		// TypeIdResolver typeIdResolver = new PlatformLayerTypeIdResolver(null, typeFactory);
		// this.requestInjection(typeIdResolver);
		// typer = typer.init(JsonTypeInfo.Id.CLASS, typeIdResolver);
		// typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);
		// typer = typer.typeProperty("type");
		// objectMapper.setDefaultTyping(typer);

		bind(ObjectMapper.class).toInstance(objectMapper);

		bind(ObjectMapperProvider.class).in(Scopes.SINGLETON);

		bind(PlatformLayerJsonProvider.class).in(Scopes.SINGLETON);

		params.put(PackagesResourceConfig.PROPERTY_PACKAGES, Joiner.on(';').join(packages));
		serve("/v0/*").with(GuiceContainer.class, params);
		// ImmutableMap.of(JSONConfiguration.FEATURE_POJO_MAPPING, "true"));

	}
}
