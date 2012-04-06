package org.platformlayer.ops.backups;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.platformlayer.core.model.PlatformLayerKey;

@XmlAccessorType(XmlAccessType.FIELD)
public class BackupItem {
    public PlatformLayerKey item;
    public String format;
    public String location;

    public BackupItem(PlatformLayerKey item, String format, String location) {
        this.item = item;
        this.format = format;
        this.location = location;
    }

    public BackupItem() {

    }
}
