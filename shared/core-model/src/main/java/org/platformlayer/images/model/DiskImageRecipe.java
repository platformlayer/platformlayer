package org.platformlayer.images.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
// @Controller(DiskImageRecipeController.class)
public class DiskImageRecipe extends ItemBase {
	public List<String> addPackage;
	public OperatingSystemRecipe operatingSystem;

	public List<Repository> repository;

	public List<RepositoryKey> repositoryKey;

	public List<ConfigurePackage> configurePackage;

	public List<String> getAddPackage() {
		if (addPackage == null) {
			addPackage = Lists.newArrayList();
		}
		return addPackage;
	}

	public OperatingSystemRecipe getOperatingSystem() {
		return operatingSystem;
	}

	public List<Repository> getRepository() {
		if (repository == null) {
			repository = Lists.newArrayList();
		}
		return repository;
	}

	public List<RepositoryKey> getRepositoryKey() {
		if (repositoryKey == null) {
			repositoryKey = Lists.newArrayList();
		}
		return repositoryKey;
	}

	public List<ConfigurePackage> getConfigurePackage() {
		if (configurePackage == null) {
			configurePackage = Lists.newArrayList();
		}
		return configurePackage;
	}

	public void setAddPackage(List<String> addPackage) {
		this.addPackage = addPackage;
	}

	public void setOperatingSystem(OperatingSystemRecipe operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public void setRepository(List<Repository> repository) {
		this.repository = repository;
	}

	public void setRepositoryKey(List<RepositoryKey> repositoryKey) {
		this.repositoryKey = repositoryKey;
	}

	public void setConfigurePackage(List<ConfigurePackage> configurePackage) {
		this.configurePackage = configurePackage;
	}

}
