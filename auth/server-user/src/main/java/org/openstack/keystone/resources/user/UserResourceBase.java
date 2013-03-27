package org.openstack.keystone.resources.user;

import java.util.Date;

import javax.inject.Inject;

import org.platformlayer.RepositoryException;
import org.platformlayer.auth.ProjectEntity;
import org.platformlayer.auth.UserEntity;
import org.platformlayer.auth.keystone.AuthenticationSecrets;
import org.platformlayer.auth.model.Access;
import org.platformlayer.auth.model.Token;
import org.platformlayer.auth.resources.PlatformlayerAuthResourceBase;
import org.platformlayer.auth.services.TokenInfo;
import org.platformlayer.auth.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class UserResourceBase extends PlatformlayerAuthResourceBase {
	private static final Logger log = LoggerFactory.getLogger(UserResourceBase.class);

	

}
