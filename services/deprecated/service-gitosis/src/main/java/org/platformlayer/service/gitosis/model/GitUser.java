package org.platformlayer.service.gitosis.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.service.gitosis.ops.GitUserController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GitUserController.class)
public class GitUser {
    public String username;
    public String sshPublicKey;
}
