package org.platformlayer.ops.filesystem;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import com.fathomdb.Utf8;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.templates.TemplateDataSource;
import org.platformlayer.ops.templates.TemplateEngine;

import com.google.common.collect.Maps;

public class TemplatedFile extends SyntheticFile {
	static final Logger log = Logger.getLogger(TemplatedFile.class);

	@Inject
	TemplateEngine template;

	String templateName;

	TemplateDataSource templateDataSource;

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		Map<String, Object> model = buildModel();
		String templateString = template.runTemplateToString(getTemplateName(), model);
		return Utf8.getBytes(templateString);
	}

	protected Map<String, Object> buildModel() throws OpsException {
		Map<String, Object> model = Maps.newHashMap();
		TemplateDataSource templateDataSource = getTemplateDataSource();
		if (templateDataSource != null) {
			templateDataSource.buildTemplateModel(model);
		}
		return model;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public static TemplatedFile build(TemplateDataSource context, File filePath, String templateName) {
		String templatePath = templateName;
		if (!templatePath.contains("/")) {
			templatePath = getDefaultResourceName(context.getClass(), templatePath);
		}

		TemplatedFile templatedFile = Injection.getInstance(TemplatedFile.class);
		templatedFile.setTemplateName(templatePath);
		templatedFile.filePath = filePath;
		templatedFile.setTemplateDataSource(context);
		return templatedFile;
	}

	public static TemplatedFile build(TemplateDataSource context, File filePath) {
		String templateName = getDefaultResourceName(context.getClass(), filePath);
		return build(context, filePath, templateName);
	}

	public TemplateDataSource getTemplateDataSource() {
		return templateDataSource;
	}

	public void setTemplateDataSource(TemplateDataSource templateDataSource) {
		this.templateDataSource = templateDataSource;
	}

	// public static void buildDefaultContext(Map<String, Object> context, OpsItem opsItem) {
	// context.put("me", opsItem);
	// context.put("ops", opsItem.getOpsSystem());
	//
	// // Put the entire chain in, keyed by their type name
	// OpsNode current = opsItem;
	// while (current != null) {
	// Class<?> currentClass = current.getClass();
	// do {
	// String key = currentClass.getSimpleName();
	// if (key.length() != 0) {
	// key = Character.toLowerCase(key.charAt(0)) + key.substring(1);
	// if (!context.containsKey(key))
	// context.put(key, current);
	// }
	// currentClass = currentClass.getSuperclass();
	// } while (!currentClass.equals(OpsItem.class));
	//
	// current = current.getParent();
	// }
	//
	// OpsServer server = opsItem.smartGetServer(false);
	// if (server == null) {
	// log.info("Warning - opsServer is null for template");
	// }
	// context.put("server", server);
	// context.put("opsServer", server);
	// }
	//
	// protected void buildContext(Map<String, Object> context) {
	// buildDefaultContext(context, this);
	// }
	//
	// public static String executeTemplate(OpsSystem opsSystem, String templateFile, final OpsItem item) throws
	// TemplateException {
	// return executeTemplate(opsSystem, templateFile, new Functor<Map<String, Object>>() {
	// public void invoke(Map<String, Object> context) {
	// buildDefaultContext(context, item);
	// }
	// });
	// }
	//
	// public static String executeTemplate(OpsSystem opsSystem, String templateFile, Functor<Map<String, Object>>
	// contextBuilder) throws TemplateException {
	// // Accept (but do not require) res: prefixed files
	// if (templateFile.startsWith("res:"))
	// templateFile = templateFile.substring(4);
	//
	// TemplateApplication template = opsSystem.getTemplateApplication(templateFile, contextBuilder);
	// return template.runTemplateToString();
	// }
	//
	// protected String executeTemplate() throws TemplateException {
	// // Accept (but do not require) res: prefixed files
	// String templateFile = getSourceFile();
	// return executeTemplate(getOpsSystem(), templateFile, this);
	// }
	//
	// @Override
	// // @DisplayAnnotation(shouldDump = false)
	// public String getContents() throws OpsException {
	// try {
	// return executeTemplate();
	// } catch (TemplateException e) {
	// throw new OpsException("Could not build templated file", e);
	// }
	// }
	//
	// @Override
	// public void setContents(String contents) {
	// throw new IllegalStateException();
	// }

}
