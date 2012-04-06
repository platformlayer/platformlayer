package org.platformlayer.ops.users;

import java.util.List;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PosixUser {
    public String userName;
    public String primaryGroup;

    public List<String> secondaryGroups = Lists.newArrayList();

    @Handler
    public void doOperation() throws OpsException {
        OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

        // TODO: Only if user not found
        {
            Command command = Command.build("adduser");
            command.addLiteral("--system");
            command.addLiteral("--no-create-home");

            if (!Strings.isNullOrEmpty(primaryGroup)) {
                command.addLiteral("--ingroup");
                command.addQuoted(primaryGroup);
            }

            command.addQuoted(userName);

            target.executeCommand(command);
        }

        for (String secondaryGroup : secondaryGroups) {
            Command command = Command.build("adduser");

            command.addQuoted(userName);
            command.addQuoted(secondaryGroup);

            target.executeCommand(command);
        }
    }

    public static PosixUser build(String userName) {
        PosixUser user = Injection.getInstance(PosixUser.class);
        user.userName = userName;
        return user;
    }

}
