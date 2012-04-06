package org.platformlayer.service.wordpress.ops;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Handler;
import org.platformlayer.ops.OpsException;
import org.platformlayer.service.mysql.ops.MysqlTarget;

public class MysqlUser {
    public String databaseName;
    public String databaseUser;
    public Secret databasePassword;

    @Handler
    public void handler(MysqlTarget mysql) throws OpsException {
        String grant = String.format("GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,ALTER ON %s.* TO %s@'%s';", databaseName, databaseUser, "%");
        mysql.execute(grant);

        // TODO: We create a Command.Argument.format function
        String setPassword = String.format("SET PASSWORD FOR '%s'@'%s' = PASSWORD('%s');", databaseUser, "%", databasePassword.plaintext());
        String setPasswordMasked = String.format("SET PASSWORD FOR '%s'@'%s' = PASSWORD('%s');", databaseUser, "%", Command.MASKED);
        mysql.execute(setPassword, setPasswordMasked);

        mysql.execute("FLUSH PRIVILEGES;");
    }
}
