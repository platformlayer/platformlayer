package org.platformlayer.ops.java;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.packages.HasDiskImageRecipe;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.OperatingSystemRecipe;
import org.platformlayer.service.imagefactory.v1.Repository;

public class JavaVirtualMachine extends OpsTreeBase implements HasDiskImageRecipe {
	public String version;
	public boolean addJdk = true;

	@Handler
	public void handler() {

	}

	@Override
	public void addTo(DiskImageRecipe recipe) {
		if (version.equals("6")) {
			Repository repository = new Repository();
			repository.setKey("debian-non-free");
			repository.getSource().add("deb http://ftp.us.debian.org/debian squeeze non-free");
			recipe.getRepository().add(repository);

			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/accepted-sun-dlj-v1-1", "boolean",
					"true");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/accepted-sun-dlj-v1-1", "boolean",
					"true");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jdk", "shared/accepted-sun-dlj-v1-1", "boolean",
					"true");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "shared/accepted-sun-dlj-v1-1", "boolean",
					"true");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "sun-java6-jre/stopthread", "boolean",
					"true");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "sun-java6-jre/jcepolicy", "note", "");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/error-sun-dlj-v1-1", "error", "");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jdk", "shared/error-sun-dlj-v1-1", "error", "");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "shared/error-sun-dlj-v1-1", "error", "");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/present-sun-dlj-v1-1", "note", "");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jdk", "shared/present-sun-dlj-v1-1", "note", "");
			DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "shared/present-sun-dlj-v1-1", "note", "");

			recipe.getAddPackage().add("sun-java6-jre");
			if (addJdk) {
				recipe.getAddPackage().add("sun-java6-jdk");
			}
		} else if (version.equals("7")) {
			OperatingSystemRecipe operatingSystem = recipe.getOperatingSystem();
			if (operatingSystem == null) {
				operatingSystem = new OperatingSystemRecipe();
			}

			if (operatingSystem.getDistribution() == null) {
				operatingSystem.setDistribution("debian");
			}

			if (operatingSystem.getVersion() == null) {
				if (operatingSystem.getDistribution().equalsIgnoreCase("debian")) {
					operatingSystem.setVersion("wheezy");
				}
			}
			recipe.setOperatingSystem(operatingSystem);
		} else {
			throw new IllegalArgumentException("Unknown java version: " + version);
		}
	}

	@Override
	protected void addChildren() throws OpsException {
		if (version.equals("6")) {
			{
				PackageDependency jre = PackageDependency.build("sun-java6-jre");
				jre.addConfiguration("sun-java6-bin", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
				jre.addConfiguration("sun-java6-bin", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
				jre.addConfiguration("sun-java6-jdk", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
				jre.addConfiguration("sun-java6-jre", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
				jre.addConfiguration("sun-java6-jre", "sun-java6-jre/stopthread", "boolean", "true");
				jre.addConfiguration("sun-java6-jre", "sun-java6-jre/jcepolicy", "note", "");
				jre.addConfiguration("sun-java6-bin", "shared/error-sun-dlj-v1-1", "error", "");
				jre.addConfiguration("sun-java6-jdk", "shared/error-sun-dlj-v1-1", "error", "");
				jre.addConfiguration("sun-java6-jre", "shared/error-sun-dlj-v1-1", "error", "");
				jre.addConfiguration("sun-java6-bin", "shared/present-sun-dlj-v1-1", "note", "");
				jre.addConfiguration("sun-java6-jdk", "shared/present-sun-dlj-v1-1", "note", "");
				jre.addConfiguration("sun-java6-jre", "shared/present-sun-dlj-v1-1", "note", "");

				addChild(jre);
			}

			if (addJdk) {
				addChild(PackageDependency.build("sun-java6-jdk"));
			}
		} else if (version.equals("7")) {
			addChild(PackageDependency.build("openjdk-7-jre"));

			if (addJdk) {
				addChild(PackageDependency.build("openjdk-7-jdk"));
			}
		} else {
			throw new IllegalArgumentException("Unknown java version: " + version);
		}

	}

	public static JavaVirtualMachine build(String version, boolean addJdk) {
		JavaVirtualMachine jvm = Injection.getInstance(JavaVirtualMachine.class);
		jvm.version = version;
		jvm.addJdk = addJdk;
		return jvm;
	}

	public static JavaVirtualMachine buildJava6() {
		return build("6", true);
	}

	public static JavaVirtualMachine buildJava7() {
		return build("7", true);
	}

	public static JavaVirtualMachine buildJre7() {
		return build("7", false);
	}

	public static JavaVirtualMachine buildJdk7() {
		return build("7", true);
	}

}
