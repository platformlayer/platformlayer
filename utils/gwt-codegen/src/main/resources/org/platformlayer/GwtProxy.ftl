package ${gwtPackage}.shared;

import org.platformlayer.ui.shared.client.commons.Accessor;
import org.platformlayer.ui.shared.client.model.DomainModel;
import ${editorPackage}.${editorClassName};
import org.platformlayer.ui.shared.server.inject.InjectingServiceLocator;
import org.platformlayer.ui.shared.shared.BaseEntityRequest;

import ${modelPackage}.${className};

import ${gwtPackage}.server.${serviceClassName};

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.ProxyFor;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.shared.Service;
import com.google.web.bindery.requestfactory.shared.SkipInterfaceValidation;

@ProxyFor(value = ${className}.class)
@SkipInterfaceValidation
// @SkipInterfaceValidation as we rely on PlatformLayer ServiceLayerDecorator
public interface ${proxyClassName} extends EntityProxy {
    EntityProxyId<${proxyClassName}> stableId();

    <#list fields as field>
    ${field.type} get${field.beanName}();
    void set${field.beanName}(${field.type} value);
    
    public static final Accessor<${proxyClassName}, ${field.accessorType}> ${field.beanName} = new Accessor<${proxyClassName}, ${field.accessorType}>() {
        @Override
        public ${field.accessorType} get(${proxyClassName} o) {
            return o.get${field.beanName}();
        }

        @Override
        public void set(${proxyClassName} o, ${field.accessorType} value) {
            o.set${field.beanName}(value);
        }
    };

	</#list>  

    @Service(value = ${serviceClassName}.class, locator = InjectingServiceLocator.class)
    @SkipInterfaceValidation
    public static interface ${className}Request extends RequestContext, BaseEntityRequest<${proxyClassName}> {
        Request<${proxyClassName}> persist(${proxyClassName} proxy);
    }

    public static interface EditorDriver extends RequestFactoryEditorDriver<${proxyClassName}, ${editorClassName}> {
    }

	public static interface ${className}RequestFactory {
    	${proxyClassName}.${className}Request get${className}Request();
	}

    public static final DomainModel<${proxyClassName}, ${className}Request> Model = new DomainModel<${proxyClassName}, ${className}Request>(${proxyClassName}.class) {
        @Override
        public Editor<${proxyClassName}> buildEditor() {
            return new ${editorClassName}();
        }

        @Override
        public ${className}Request context(RequestFactory requestFactory) {
            return ((${className}RequestFactory) requestFactory).get${className}Request();
        }

        @Override
        public void persist(${className}Request context, ${proxyClassName} item) {
            context.persist(item);
        }

        @Override
        public RequestFactoryEditorDriver<${proxyClassName}, Editor<${proxyClassName}>> buildEditorDriver() {
            return GWT.create(EditorDriver.class);
        }
    };
}
