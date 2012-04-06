package org.platformlayer.service.gitosis.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.service.gitosis.ops.GitRepositoryController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GitRepositoryController.class)
public class GitRepository {
    // public boolean allowPublicRead;
    public String name;
    public List<String> user;
}
