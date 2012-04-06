package org.platformlayer.core.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class HostPolicy {
    public boolean allowRunInContainer;
    //
    // private static final String KEY_ALLOW_RUN_IN_CONTAINER = "container";
    // List<String> keys = Lists.newArrayList();
    //
    // public boolean isCanRunInContainer() {
    // return keys.contains(KEY_ALLOW_RUN_IN_CONTAINER);
    // }
    //
    // public void allowRunInContainer() {
    // if (!keys.contains(KEY_ALLOW_RUN_IN_CONTAINER)) {
    // keys.add(KEY_ALLOW_RUN_IN_CONTAINER);
    // }
    // }
    //
    // public Tag buildTag() {
    // String serialized = Joiner.on(";").join(keys);
    // return new Tag(Tag.HOST_POLICY, serialized);
    // }
}
