package org.platformlayer.guice;

import java.sql.Connection;

public class JdbcGuice {
    public static Connection getConnection() {
        return JdbcTransactionInterceptor.getConnection();
    }
}
