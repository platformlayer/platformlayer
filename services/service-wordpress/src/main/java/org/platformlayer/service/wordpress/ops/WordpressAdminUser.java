package org.platformlayer.service.wordpress.ops;

import org.platformlayer.ops.Handler;
import org.platformlayer.ops.Injection;

public class WordpressAdminUser {

    @Handler
    public void handler() {
        // MysqlTarget mysql;
        //
        // mysql.createDatabase(databaseName);
        //
        // String grant = String.format("GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,ALTER ON {0}.* TO {1}@'%' IDENTIFIED BY '{2}';", databaseName, databaseUser, databasePassword);
        // mysql.execute(grant);
        //
        // mysql.execute("FLUSH PRIVILEGES;");
    }

    public static WordpressAdminUser build() {
        return Injection.getInstance(WordpressAdminUser.class);
    }

}
