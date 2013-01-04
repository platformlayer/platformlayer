package org.platformlayer;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.platformlayer.model.ClassModel;
import org.platformlayer.model.FieldModel;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GwtCodegenFileVisitor extends FileVisitor {
	private final ClassInspection classInspection;
	private final TemplateEngine template;
	private List<String> gwtBasePathComponents;
	private final CodegenStyle style;

	public GwtCodegenFileVisitor(File srcDir, File outDir, Log log, ClassInspection classInspection,
			TemplateEngine template, CodegenStyle style) {
		super(srcDir, outDir, log);
		this.classInspection = classInspection;
		this.template = template;
		this.style = style;
	}

	@Override
	public void visitDirectory(File dir) throws MojoExecutionException {
		for (File gwtFile : dir.listFiles(new ExtensionFileFilter(".gwt.xml"))) {
			gwtBasePathComponents = getPathComponents();
		}

		super.visitDirectory(dir);
	}

	@Override
	public void visitFile(File file) throws MojoExecutionException {
		String fileName = file.getName();
		if (!fileName.endsWith(".class")) {
			return;
		}

		String simpleClassName = fileName.substring(0, fileName.length() - 6);
		String fullClassName = Joiner.on(".").join(getPathComponents()) + "." + simpleClassName;

		try {
			log.info("Processing " + fullClassName);
			Class<?> clazz = classInspection.loadClass(fullClassName);

			if (classInspection.findAnnotation(clazz, "org.platformlayer.codegen.GwtModel") == null) {
				log.info("No GWTModel annotation; skipping");
				return;
			}

			switch (style) {
			case Jso:
				processClassJso(clazz);
				break;
			case RequestFactory:
				processClassRequestFactory(clazz);
				break;
			default:
				throw new UnsupportedOperationException();
			}
			// getLog().info(" => " + converted);
		} catch (ClassNotFoundException e) {
			log.warn("Error loading class: " + fullClassName, e);
		} catch (NoClassDefFoundError e) {
			log.warn("Error loading class: " + fullClassName, e);
		}
	}

	private void runTemplate(String templateName, Map<String, Object> model, File outputFile)
			throws MojoExecutionException {
		Utils.mkdirs(outputFile.getParentFile());
		String result = template.runTemplateToString(getClass().getPackage().getName().replace(".", "/") + "/"
				+ templateName, model);

		String existing = Utils.readAll(outputFile);

		if (result.equals(existing)) {
			log.info("File unchanged; will not write: " + outputFile);
			return;
		}

		log.info("Writing file " + outputFile);
		Utils.writeAll(outputFile, result);
	}

	private void processClassRequestFactory(Class<?> clazz) throws MojoExecutionException {
		Map<String, Object> model = new HashMap<String, Object>();

		ClassModel classModel = new ClassModel();
		classModel.className = clazz.getSimpleName();
		classModel.proxyClassName = classModel.className + "Proxy";
		classModel.serviceClassName = classModel.className + "GwtService";
		classModel.editorClassName = classModel.className + "Editor";

		for (Field field : clazz.getFields()) {
			FieldModel fieldModel = new FieldModel();
			Class<?> type = field.getType();

			if (!isNativeType(type)) {
				continue;
			}

			Class<?> accessorType = type;
			if (accessorType.isPrimitive()) {
				accessorType = Utils.getBoxedType(accessorType);
			}
			String fieldName = field.getName();
			String beanName = Utils.capitalize(fieldName);

			fieldModel.type = type.getName();
			fieldModel.accessorType = accessorType.getName();
			fieldModel.beanName = beanName;
			fieldModel.name = fieldName;

			classModel.fields.add(fieldModel);
		}

		model.put("className", classModel.className);
		model.put("proxyClassName", classModel.proxyClassName);
		model.put("serviceClassName", classModel.serviceClassName);
		model.put("editorClassName", classModel.editorClassName);
		model.put("fields", classModel.fields);

		if (gwtBasePathComponents == null) {
			throw new MojoExecutionException("Did not find .gwt.xml file above " + clazz);
		}

		File gwtOutDir = new File(outDir, Joiner.on("/").join(gwtBasePathComponents));
		Utils.mkdirs(gwtOutDir);

		String gwtPackage = Joiner.on(".").join(gwtBasePathComponents);
		model.put("gwtPackage", gwtPackage);

		String modelPackage = Joiner.on(".").join(getPathComponents());
		model.put("modelPackage", modelPackage);

		String editorPackage = gwtPackage + ".client";
		model.put("editorPackage", editorPackage);

		runTemplate("requestfactory/GwtProxy.ftl", model, new File(new File(gwtOutDir, "shared"),
				classModel.proxyClassName + ".java"));
		runTemplate("requestfactory/GwtRequestFactory.ftl", model, new File(new File(gwtOutDir, "shared"),
				classModel.className + "RequestFactory.java"));
		runTemplate("requestfactory/GwtService.ftl", model, new File(new File(gwtOutDir, "server"),
				classModel.serviceClassName + ".java"));
	}

	private void processClassJso(Class<?> clazz) throws MojoExecutionException {
		Map<String, Object> model = new HashMap<String, Object>();

		// ClassModel classModel = new ClassModel();
		// classModel.className = clazz.getSimpleName();
		// classModel.proxyClassName = classModel.className + "Proxy";
		// classModel.serviceClassName = classModel.className + "GwtService";
		// classModel.editorClassName = classModel.className + "Editor";

		List<FieldModel> fields = Lists.newArrayList();
		List<String> warnings = Lists.newArrayList();

		String jsoClassName = clazz.getSimpleName();
		String jsoBaseClassName = "com.google.gwt.core.client.JavaScriptObject";

		Set<String> skipFields = Sets.newHashSet();

		{
			Class<?> c = clazz;
			while (c != null) {
				if (c.getName().equals("org.platformlayer.core.model.ItemBase")) {
					jsoBaseClassName = "org.platformlayer.core.model.ItemBaseJs";
					skipFields.add("key");
					skipFields.add("tags");
					break;
				}
				if (c.getName().equals("org.platformlayer.core.model.Action")) {
					jsoBaseClassName = "org.platformlayer.core.model.ActionJs";
					skipFields.add("type");
					break;
				}
				if (c == Object.class) {
					break;
				} else {
					c = c.getSuperclass();
				}
			}
		}

		for (Field field : clazz.getFields()) {
			FieldModel fieldModel = new FieldModel();
			Class<?> type = field.getType();

			String fieldName = field.getName();
			if (skipFields.contains(fieldName)) {
				// These come in from the base class
				continue;
			}

			String beanName = Utils.capitalize(fieldName);
			Class<?> accessorType = type;
			if (accessorType.isPrimitive()) {
				accessorType = Utils.getBoxedType(accessorType);
			}

			if (type == Long.class || type.equals(long.class)) {
				warnings.add("JSNI cannot map 'long " + fieldName + "'");
				continue;
			}

			String mapped = null;

			if (!isNativeType(type)) {
				mapped = mapSpecialType(type);

				if (mapped == null) {
					String get = null;
					String set = null;

					if (type.equals(List.class)) {
						Type genericType = field.getGenericType();
						if (genericType instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) genericType;
							Type[] actualTypeArguments = pt.getActualTypeArguments();
							if (actualTypeArguments != null && actualTypeArguments.length == 1) {
								Class<?> itemClass = (Class<?>) actualTypeArguments[0];

								mapped = "java.util.List<" + itemClass.getName() + ">";

								// public final
								// List<org.platformlayer.service.certificates.model.PurchaseCertificateExample>
								// getExamples() {
								// return
								// List<org.platformlayer.service.certificates.model.PurchaseCertificateExample>Js.get(this,
								// "examples");
								// }
								//
								// public final void
								// setExamples(List<org.platformlayer.service.certificates.model.PurchaseCertificateExample>
								// newValue) {
								// List<org.platformlayer.service.certificates.model.PurchaseCertificateExample>Js.set(this,
								// "examples", newValue);
								// }

								// Add imports??

								get = "";
								get += "public final java.util.List<{itemClass}> get{beanName}() {\n";

								if (itemClass.equals(String.class)) {
									get += "	com.google.gwt.core.client.JsArrayString array0 = org.platformlayer.core.model.JsHelpers.getObject0(this, \"{fieldName}\").cast();\n";
									get += "	return org.platformlayer.core.model.JsStringArrayToList.wrap(array0);\n";
								} else {
									get += "	com.google.gwt.core.client.JsArray<{itemClass}> array0 = org.platformlayer.core.model.JsHelpers.getObject0(this, \"{fieldName}\").cast();\n";
									get += "	return org.platformlayer.core.model.JsArrayToList.wrap(array0);\n";
								}

								get += "}\n";

								set = "";

								get = get.replace("{itemClass}", itemClass.getName());
								set = set.replace("{itemClass}", itemClass.getName());
							}
						}
					} else if (type.equals(String.class)) {
						get = "";
						get += "public final String get{beanName}() {\n";
						get += "	return org.platformlayer.core.model.JsHelpers.getString0(this, \"{fieldName}\");\n";
						get += "}\n";

						set = "";
						set += "public final void set{beanName}(String v) {\n";
						set += "	org.platformlayer.core.model.JsHelpers.set0(this, \"{fieldName}\", v);\n";
						set += "}\n";
					} else {
						boolean gwtSafe = false;
						for (Annotation annotation : type.getAnnotations()) {
							if (annotation.annotationType().getSimpleName().equals("GwtSafe")) {
								gwtSafe = true;
							}
						}

						if (gwtSafe) {
							get = "";
							get += "public final {field.type} get{beanName}() {\n";
							get += "	return org.platformlayer.core.model.JsHelpers.getObject0(this, \"{fieldName}\").cast();\n";
							get += "}\n";

							set = "";

							mapped = "HACK";
						}
					}

					if (get != null) {
						get = get.replace("{beanName}", beanName);
						get = get.replace("{fieldName}", fieldName);
						get = get.replace("{field.type}", type.getName());

						fieldModel.customGet = get;
					}

					if (set != null) {
						set = set.replace("{beanName}", beanName);
						set = set.replace("{fieldName}", fieldName);
						set = set.replace("{field.type}", type.getName());

						fieldModel.customSet = set;
					}
				}
			}

			if (mapped != null) {

			} else if (!isNativeType(type)) {
				warnings.add("JSNI cannot map '" + type.getSimpleName() + " " + fieldName + "'");
				continue;
			}

			fieldModel.type = mapped != null ? mapped : type.getName();
			fieldModel.accessorType = mapped != null ? mapped : accessorType.getName();
			fieldModel.beanName = beanName;
			fieldModel.methodNameGet = "get" + beanName;
			if (type.equals(boolean.class) || type.equals(Boolean.class)) {
				fieldModel.methodNameGet = "is" + beanName;
			}
			fieldModel.name = fieldName;

			fieldModel.custom = mapped != null;

			fields.add(fieldModel);
		}

		// model.put("className", classModel.className);
		model.put("jsoClassName", jsoClassName);
		model.put("jsoBaseClassName", jsoBaseClassName);
		// model.put("serviceClassName", classModel.serviceClassName);
		// model.put("editorClassName", classModel.editorClassName);
		model.put("fields", fields);

		if (gwtBasePathComponents == null) {
			throw new MojoExecutionException("Did not find .gwt.xml file above " + clazz);
		}

		// String gwtPackage = Joiner.on(".").join(gwtBasePathComponents);
		String outputPackage = clazz.getPackage().getName();
		model.put("package", outputPackage);

		// File outputDir = new File(outDir, Joiner.on("/").join(gwtBasePathComponents));
		// Utils.mkdirs(outputDir);

		String outputPath = Joiner.on(".").join(gwtBasePathComponents) + ".translatable." + outputPackage;
		File outputDir = new File(outDir, outputPath.replace('.', '/'));
		Utils.mkdirs(outputDir);

		model.put("warnings", warnings);

		// String modelPackage = Joiner.on(".").join(getPathComponents());
		// model.put("modelPackage", modelPackage);
		//
		// String editorPackage = gwtPackage + ".client";
		// model.put("editorPackage", editorPackage);

		runTemplate("jso/JsoObject.ftl", model, new File(outputDir, jsoClassName + ".java"));
	}

	private String mapSpecialType(Class<?> type) {
		String name = type.getName();

		Map<String, String> whitelist = Maps.newHashMap();
		whitelist.put("org.platformlayer.core.model.PlatformLayerKey", "org.platformlayer.core.model.PlatformLayerKey");
		whitelist.put("org.platformlayer.core.model.Tags", "org.platformlayer.core.model.Tags");
		String translated = whitelist.get(name);
		return translated;
	}

	private boolean isNativeType(Class<?> type) {
		if (type.isPrimitive()) {
			return true;
		}

		if (type == Boolean.class) {
			return true;
		}
		if (type == Byte.class) {
			return true;
		}
		if (type == Character.class) {
			return true;
		}
		if (type == Short.class) {
			return true;
		}
		if (type == Integer.class) {
			return true;
		}
		if (type == Long.class) {
			return true;
		}
		if (type == Float.class) {
			return true;
		}
		if (type == Double.class) {
			return true;
		}

		return false;
	}

	// private void convertClasses(String baseNamespace, String relativePath, File srcDir, File outputDir) throws
	// MojoExecutionException {
	// if (!srcDir.exists())
	// return;
	//
	// for (File gwtFile : srcDir.listFiles(new FileFilter() {
	// };() {
	//
	// public boolean accept(File arg0, String arg1) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	// })
	// for (File file : srcDir.listFiles()) {
	// String fileName = file.getName();
	// if (file.isDirectory()) {
	// String childPath = relativePath;
	// childPath += fileName + ".";
	// convertClasses(baseNamespace, childPath, file, new File(outputDir, fileName));
	// } else {
	// if (!fileName.endsWith(".class")) {
	// continue;
	// }
	// String className = relativePath;
	// String simpleClassName = fileName.substring(0, fileName.length() - 6);
	// className += simpleClassName;
	//
	// try {
	// getLog().info("Processing " + className);
	// Class<?> clazz = classLoader.loadClass(className);
	// if (clazz.isInterface()) {
	// getLog().info("Ignoring interface");
	// continue;
	// }
	//
	// if (findAnnotation(clazz, "org.platformlayer.codegen.GwtModel") == null) {
	// getLog().info("No GWTModel annotation; skipping");
	// continue;
	// }
	//
	// processClass(baseNamespace, clazz, outputDir);
	// // getLog().info(" => " + converted);
	// } catch (ClassNotFoundException e) {
	// throw new MojoExecutionException("Error loading class: " + className, e);
	// } catch (NoClassDefFoundError e) {
	// throw new MojoExecutionException("Error loading class: " + className, e);
	// }
	// }
	// }
	//
	// }
	//
	// }

}
