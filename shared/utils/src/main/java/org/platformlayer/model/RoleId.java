package org.platformlayer.model;

public class RoleId extends StringWrapper {
    public static final RoleId ADMIN = new RoleId("admin");
    public static final RoleId READ = new RoleId("read");

    public RoleId(String key) {
        super(key);
    }
}
