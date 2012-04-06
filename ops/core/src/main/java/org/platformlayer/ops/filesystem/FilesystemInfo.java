package org.platformlayer.ops.filesystem;

import com.google.common.base.Objects;

public class FilesystemInfo {
    private static final int STICKY_BIT = 1000;

    // 4000 Set user ID on execution.
    private static final int SET_USER_ID = 4000;

    // 20#0 Set group ID on execution if # is 7, 5, 3,
    // or 1.
    //
    // Enable mandatory locking if # is 6, 4, 2,
    // or 0.
    private static final int SET_GROUP_ID = 2000;

    public String mode;
    public String links;
    public String owner;
    public String group;
    public long size;
    public String date;
    public String name;
    public String type;
    public String symlinkTarget;
    public int depth;

    // target removed, use readlink instead
    // public String target;

    // public int parseNumericMode() {
    // // e.g. drwxrwxrwt
    // if (mode == null)
    // throw new IllegalArgumentException("Mode not set");
    // try {
    // if (mode.length() != 10)
    // throw new IllegalArgumentException();
    //
    // int decimal = 0;
    // decimal += parseModeFragment(mode.substring(1, 4), 100);
    // decimal += parseModeFragment(mode.substring(4, 7), 10);
    // decimal += parseModeFragment(mode.substring(7, 10), 1);
    // return decimal;
    // } catch (Exception e) {
    // throw new IllegalArgumentException("Cannot parse mode: " + mode, e);
    // }
    // }

    // private int parseModeFragment(String substring, int multiplier) {
    // if (substring.length() != 3)
    // throw new IllegalArgumentException();
    // int decimal = 0;
    //
    // switch (substring.charAt(0)) {
    // case '-':
    // // - The indicated permission is not granted.
    // break;
    //
    // case 'r':
    // // r The file is readable.
    // decimal += 4 * multiplier;
    // break;
    //
    // default:
    // throw new IllegalArgumentException();
    // }
    //
    // switch (substring.charAt(1)) {
    // case '-':
    // // - The indicated permission is not granted.
    // break;
    //
    // case 'w':
    // // w The file is writable.
    // decimal += 2 * multiplier;
    // break;
    //
    // default:
    // throw new IllegalArgumentException();
    // }
    //
    // switch (substring.charAt(2)) {
    // case '-':
    // // - The indicated permission is not granted.
    // break;
    //
    // case 'x':
    // // x The file is executable.
    // decimal += 1 * multiplier;
    // break;
    //
    // case 'T':
    // // T The 1000 bit is turned on, and execution is off
    // // (undefined bit-state).
    // decimal += STICKY_BIT;
    // break;
    //
    // case 't':
    // // t The 1000 (octal) bit, or sticky bit, is on (see
    // // chmod(1)), and execution is on.
    // decimal += STICKY_BIT;
    // decimal += 1 * multiplier;
    // break;
    //
    // case 's':
    // case 'S':
    // // s The set-user-ID or set-group-ID bit is on, and
    // // the corresponding user or group execution bit
    // // is also on.
    //
    // // S Undefined bit-state (the set-user-ID or set-
    // // group-id bit is on and the user or group execu-
    // // tion bit is off). For group permissions, this
    // // applies only to non-regular files.
    //
    // if (substring.charAt(2) == 's') {
    // decimal += 1 * multiplier;
    // }
    //
    // switch (multiplier) {
    // case 100:
    // decimal += SET_USER_ID;
    // break;
    // case 10:
    // decimal += SET_GROUP_ID;
    // break;
    // default:
    // throw new IllegalArgumentException();
    // }
    // break;
    // default:
    // throw new IllegalArgumentException();
    // }
    //
    // return decimal;
    // }

    public boolean matchesMode(String matchMode) {
        if (Objects.equal(matchMode, getFileMode())) {
            return true;
        }

        if (Integer.parseInt(matchMode) == Integer.parseInt(getFileMode())) {
            return true;
        }

        return false;
    }

    public String getFileMode() {
        return this.mode;
    }

    public boolean matchesOwner(String matchOwner) {
        return (owner.equals(matchOwner));
    }

    public boolean matchesGroup(String matchGroup) {
        return (group.equals(matchGroup));
    }

    public boolean isSymlink() {
        return "l".equals(type);
    }

    @Override
    public String toString() {
        return "FilesystemInfo [name=" + name + ", size=" + size + ", date=" + date + ", owner=" + owner + ", group=" + group + ", links=" + links + ", mode=" + mode + "]";
    }

}