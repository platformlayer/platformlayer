package org.platformlayer.ops.java;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.instances.DiskImageRecipeBuilder;
import org.platformlayer.ops.metrics.collectd.OpsTreeBase;
import org.platformlayer.ops.packages.HasDiskImageRecipe;
import org.platformlayer.ops.packages.PackageDependency;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.OperatingSystemRecipe;
import org.platformlayer.service.imagefactory.v1.Repository;

public class JavaVirtualMachine extends OpsTreeBase implements HasDiskImageRecipe {

    public String version;

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

            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jdk", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "shared/accepted-sun-dlj-v1-1", "boolean", "true");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "sun-java6-jre/stopthread", "boolean", "true");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "sun-java6-jre/jcepolicy", "note", "");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/error-sun-dlj-v1-1", "error", "");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jdk", "shared/error-sun-dlj-v1-1", "error", "");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "shared/error-sun-dlj-v1-1", "error", "");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-bin", "shared/present-sun-dlj-v1-1", "note", "");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jdk", "shared/present-sun-dlj-v1-1", "note", "");
            DiskImageRecipeBuilder.addPreconfigure(recipe, "sun-java6-jre", "shared/present-sun-dlj-v1-1", "note", "");

            recipe.getAddPackage().add("sun-java6-jdk");
            recipe.getAddPackage().add("sun-java6-jre");
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
            addChild(PackageDependency.build("sun-java6-jdk"));
            addChild(PackageDependency.build("sun-java6-jre"));
        } else if (version.equals("7")) {
            addChild(PackageDependency.build("openjdk-7-jre"));
        } else {
            throw new IllegalArgumentException("Unknown java version: " + version);
        }

    }

    public static JavaVirtualMachine build(String version) {
        JavaVirtualMachine jvm = Injection.getInstance(JavaVirtualMachine.class);
        jvm.version = version;
        return jvm;
    }

    public static JavaVirtualMachine buildJava6() {
        return build("6");
    }

    public static JavaVirtualMachine buildJava7() {
        return build("7");
    }

}
