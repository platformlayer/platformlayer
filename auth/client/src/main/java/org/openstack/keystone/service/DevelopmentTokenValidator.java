package org.openstack.keystone.service;

import javax.inject.Inject;

import org.platformlayer.ApplicationMode;
import org.platformlayer.model.Authentication;
import org.platformlayer.model.RoleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevelopmentTokenValidator implements AuthenticationTokenValidator {
    static final Logger log = LoggerFactory.getLogger(DevelopmentTokenValidator.class);

    @Inject
    KeystoneTokenValidator keystone;

    public DevelopmentTokenValidator() {
        if (!ApplicationMode.isDevelopment())
            throw new IllegalStateException();
    }

    public static final String PREFIX = "DEV-TOKEN-";

    class DevelopmentAuthentication implements Authentication {

        final String project;
        final String userKey;

        public DevelopmentAuthentication(String userKey, String project) {
            super();
            this.userKey = userKey;
            this.project = project;
        }

        @Override
        public String getProject() {
            return project;
        }

        @Override
        public boolean isInRole(String project, RoleId role) {
            return true;
        }

        @Override
        public byte[] getUserSecret() {
            return null;
        }

        @Override
        public String getUserKey() {
            return userKey;
        }

    }

    @Override
    public Authentication validate(String authToken) {
        authToken = authToken.trim();

        if (authToken.startsWith(PREFIX)) {
            String project = authToken.substring(PREFIX.length());
            String userKey = project;

            return new DevelopmentAuthentication(userKey, project);
        }

        return keystone.validate(authToken);
    }
}
