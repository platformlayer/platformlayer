package org.platformlayer.model;

public interface Authentication {
    String getProject();

    String getUserKey();

    boolean isInRole(String project, RoleId role);

    byte[] getUserSecret();
}
