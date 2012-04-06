package org.platformlayer.core.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class InstanceBase extends ItemBase {
    public PlatformLayerKey cloud;
    public PlatformLayerKey recipeId;
    public String sshPublicKey;
    public HostPolicy hostPolicy;

    public List<Integer> publicPorts;
}
