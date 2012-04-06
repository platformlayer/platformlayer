package org.openstack.service.imagestore.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.openstack.service.imagestore.ops.ImageStoreController;
import org.platformlayer.core.model.ItemBase;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(ImageStoreController.class)
public class ImageStore extends ItemBase {
    public String dnsName;
}
