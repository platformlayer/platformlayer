package org.platformlayer.ops;

public enum OperationType {
    /**
     * A configure operation makes the changes we want
     */
    Configure,

    /**
     * A Validate operation doesn't make changes, but raises warnings if the state isn't what we want
     */
    Validate,

    /**
     * Delete cleans up
     */
    Delete,

    /**
     * Performs a backup
     */
    Backup;

    public boolean isConfigure() {
        return this == Configure;
    }

    public boolean isValidate() {
        return this == Validate;
    }

    public boolean isForce() {
        // TODO: Support force?
        return false;
    }

    public boolean isDelete() {
        return this == Delete;
    }
}
