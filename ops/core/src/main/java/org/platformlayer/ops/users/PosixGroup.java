package org.platformlayer.ops.users;

import java.util.Map;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.users.PosixGroupManagement.Group;

public class PosixGroup {
	public String groupName;

	@Handler
	public void doOperation() throws OpsException {
		OpsTarget target = OpsContext.get().getInstance(OpsTarget.class);

		Map<String, Group> groups = PosixGroupManagement.getGroups(target);
		Group group = groups.get(groupName);
		if (group == null) {
			target.executeCommand("groupadd {0}", groupName);
		}
	}

	public static PosixGroup build(String groupName) {
		PosixGroup group = Injection.getInstance(PosixGroup.class);
		group.groupName = groupName;
		return group;
	}
}
