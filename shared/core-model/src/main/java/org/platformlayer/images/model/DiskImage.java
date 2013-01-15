package org.platformlayer.images.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
// @Controller(DiskImageController.class)
public class DiskImage extends ItemBase {
	public PlatformLayerKey cloud;
	public PlatformLayerKey recipeId;
	public String format;

	public PlatformLayerKey getCloud() {
		return cloud;
	}

	public void setCloud(PlatformLayerKey cloud) {
		this.cloud = cloud;
	}

	public PlatformLayerKey getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(PlatformLayerKey recipeId) {
		this.recipeId = recipeId;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

}
