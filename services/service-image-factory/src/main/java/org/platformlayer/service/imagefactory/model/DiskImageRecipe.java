package org.platformlayer.service.imagefactory.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.imagefactory.ops.DiskImageRecipeController;
import org.platformlayer.xaas.Controller;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Controller(DiskImageRecipeController.class)
public class DiskImageRecipe extends ItemBase {
	public List<String> addPackage;
	public OperatingSystemRecipe operatingSystem;

	public List<Repository> repository;

	public List<RepositoryKey> repositoryKey;

	public List<ConfigurePackage> configurePackage;
}
