package ${gwtPackage}.client.model;

import com.google.gwt.core.client.JavaScriptObject;

public class ${jsoClassName} extends JavaScriptObject {
	protected ${jsoClassName}() {
	}

    <#list warnings as warning>
	// TODO: ${warning}
	</#list>  
    
    <#list fields as field>
	public final native ${field.type} get${field.beanName}()
	/*-{ return this.${field.name}; }-*/;

	public final native void set${field.beanName}(${field.type} newValue)
	/*-{ this.${field.name} = newValue; }-*/;
	</#list>  
}
