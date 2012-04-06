package org.platformlayer.auth.crypto;

public class SecretStoreVisitor {

    public void visitAsymetricSystemKey(int keyId, byte[] data) {
    }

    public void visitUserKey(int userId, byte[] data) {
    }

    public void visitProjectKey(int projectId, byte[] data) {
    }

    public void visitToken(int tokenId, byte[] data) {
    }

    public void visitPassword(byte[] salt, byte[] data) {
    }

    public void visitAsymetricUserKey(int userId, byte[] data) {

    }

};