package org.platformlayer.service.imagefactory.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.core.model.PlatformLayerKey;
import org.platformlayer.service.imagefactory.ops.DiskImageController;
import org.platformlayer.xaas.Controller;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Controller(DiskImageController.class)
public class DiskImage extends ItemBase {
    public PlatformLayerKey cloud;
    public PlatformLayerKey recipeId;
    public String format;
}
