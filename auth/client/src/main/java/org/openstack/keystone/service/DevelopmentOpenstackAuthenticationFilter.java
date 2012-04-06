//package org.openstack.keystone.service;
//
//import org.platformlayer.ApplicationMode;
//import org.platformlayer.model.Authentication;
//
//public class DevelopmentOpenstackAuthenticationFilter extends OpenstackAuthenticationFilterBase {
//    public DevelopmentOpenstackAuthenticationFilter() {
//        super(new DevelopmentAuthTokenValidator());
//
//        ApplicationMode.onlyForDevelopment();
//    }
//
//    public static final String PREFIX = "DEV-TOKEN-";
//
//    static class DevelopmentAuthTokenValidator implements AuthenticationTokenValidator {
//        public DevelopmentAuthTokenValidator() {
//        }
//
//        @Override
//        public Authentication validate(String authToken) {
//            AccountId accountId = null;
//
//            authToken = authToken.trim();
//
//            if (authToken.startsWith(PREFIX)) {
//                String accountIdString = authToken.substring(PREFIX.length());
//                accountId = new AccountId(accountIdString);
//            }
//
//            return accountId;
//        }
//    }
// }
