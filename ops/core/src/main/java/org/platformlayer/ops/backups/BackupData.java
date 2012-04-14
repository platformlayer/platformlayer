package org.platformlayer.ops.backups;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BackupData {
	public String id;
	public Date timestamp;

	public List<BackupItem> items = Lists.newArrayList();
}
