package ${package};

import com.google.gwt.core.client.JavaScriptObject;

public class ${jsoClassName} extends ${jsoBaseClassName} {
	protected ${jsoClassName}() {
	}

    <#list warnings as warning>
	// TODO: ${warning}
	</#list>  

    <#list fields as field>
    
    <#if field.customGet??>
    ${field.customGet}
    <#elseif field.custom>
    public final ${field.type} ${field.methodNameGet}() {
		return ${field.type}Js.get(this, "${field.name}");
	}
    <#else>
	public final native ${field.type} ${field.methodNameGet}()
	/*-{ return this.${field.name}; }-*/;
	</#if>
	
    <#if field.customSet??>
    ${field.customSet}
    <#elseif field.custom>
	public final void set${field.beanName}(${field.type} newValue) {
		${field.type}Js.set(this, "${field.name}", newValue);
	}
    <#else>
	public final native void set${field.beanName}(${field.type} newValue)
	/*-{ this.${field.name} = newValue; }-*/;
	</#if>

	</#list>

	public static final ${jsoClassName} create() {
		return ${jsoClassName}.createObject().cast();
	}
}
