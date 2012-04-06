package org.platformlayer.service.gitosis.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.platformlayer.service.gitosis.ops.GitServerController;
import org.platformlayer.xaas.Controller;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Controller(GitServerController.class)
public class GitServer {
    public String dnsName;
}
