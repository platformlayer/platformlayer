package org.platformlayer.service.git.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.core.model.ItemBase;
import org.platformlayer.service.git.ops.GitRepositoryController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GitRepositoryController.class)
public class GitRepository extends ItemBase {
    // We do allow public read (for now)
    // public boolean allowPublicRead = true;
    public String name;
}
