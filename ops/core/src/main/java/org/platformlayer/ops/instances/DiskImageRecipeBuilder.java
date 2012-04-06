package org.platformlayer.ops.instances;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import org.platformlayer.ResourceUtils;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.packages.HasDiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.ConfigurePackage;
import org.platformlayer.service.imagefactory.v1.DiskImageRecipe;
import org.platformlayer.service.imagefactory.v1.Repository;
import org.platformlayer.service.imagefactory.v1.RepositoryKey;
import org.platformlayer.xml.JaxbHelper;
import org.platformlayer.xml.UnmarshalException;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class DiskImageRecipeBuilder extends OpsTreeVisitor {
    final DiskImageRecipe recipe = new DiskImageRecipe();

    @Override
    public void visit(Object controller) throws OpsException {
        if (controller instanceof HasDiskImageRecipe) {
            ((HasDiskImageRecipe) controller).addTo(recipe);
        }

        super.visit(controller);
    }

    public DiskImageRecipe getRecipe() {
        return recipe;
    }

    protected void removeDetails() {
        DiskImageRecipe recipe = getRecipe();

        final Set<String> whitelistPackages = Sets.newHashSet();
        final Set<String> whitelistRepos = Sets.newHashSet();
        final Set<String> whitelistRepositoryKeys = Sets.newHashSet();

        whitelistPackages.add("openjdk-7-jre");

        Iterables.removeIf(recipe.getAddPackage(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !whitelistPackages.contains(input);
            }
        });

        Iterables.removeIf(recipe.getRepositoryKey(), new Predicate<RepositoryKey>() {
            @Override
            public boolean apply(RepositoryKey input) {
                return !whitelistRepositoryKeys.contains(input.getUrl());
            }
        });

        Iterables.removeIf(recipe.getRepository(), new Predicate<Repository>() {
            @Override
            public boolean apply(Repository input) {
                return !whitelistRepos.contains(input.getKey());
            }
        });

        Iterables.removeIf(recipe.getConfigurePackage(), new Predicate<ConfigurePackage>() {
            @Override
            public boolean apply(ConfigurePackage input) {
                return !whitelistPackages.contains(input.getPackageName());
            }
        });
    }

    protected void normalize() {
        DiskImageRecipe recipe = getRecipe();

        List<String> packages = recipe.getAddPackage();
        Collections.sort(packages);
        String previous = null;
        Iterator<String> it = packages.iterator();
        while (it.hasNext()) {
            String packageName = it.next();
            if (Objects.equal(previous, packageName)) {
                it.remove();
            }
            previous = packageName;
        }

        // TODO: Other collections!
    }

    public static Provider<DiskImageRecipe> buildDiskImageRecipe(final Object controller) throws OpsException {
        return new ThrowingProvider<DiskImageRecipe>() {
            @Override
            public DiskImageRecipe build() throws OpsException {
                DiskImageRecipeBuilder builder = Injection.getInstance(DiskImageRecipeBuilder.class);
                builder.visit(controller);

                builder.removeDetails();

                builder.normalize();

                return builder.getRecipe();
            }
        };
    }

    public static Provider<DiskImageRecipe> loadDiskImageResource(final Class<?> context, final String resourceName) {
        return new ThrowingProvider<DiskImageRecipe>() {
            @Override
            public DiskImageRecipe build() throws OpsException {
                DiskImageRecipe recipe;
                try {
                    String recipeXml = ResourceUtils.get(context, resourceName);
                    recipe = JaxbHelper.deserializeXmlObject(recipeXml, DiskImageRecipe.class);
                } catch (IOException e) {
                    throw new OpsException("Error loading recipe", e);
                } catch (UnmarshalException e) {
                    throw new OpsException("Error loading recipe", e);
                }
                return recipe;
            }
        };
    }

    public static void addPreconfigure(DiskImageRecipe recipe, String packageName, String key, String type, String value) {
        ConfigurePackage configurePackage = new ConfigurePackage();
        configurePackage.setPackageName(packageName);
        configurePackage.setKey(key);
        configurePackage.setType(type);
        configurePackage.setValue(value);

        recipe.getConfigurePackage().add(configurePackage);
    }
}
