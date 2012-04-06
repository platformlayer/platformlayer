package org.platformlayer.guice;

import java.sql.Connection;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class JdbcTransactionInterceptor implements MethodInterceptor {

    private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<Connection>();

    @Inject
    DataSource dataSource;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Connection connection = threadLocalConnection.get();

        if (connection != null) {
            // Ignore recursive calls
            return methodInvocation.proceed();
        }

        // Yes, this is fairly paranoid...
        try {
            connection = dataSource.getConnection();
            threadLocalConnection.set(connection);

            try {
                boolean committed = false;
                connection.setAutoCommit(false);

                try {
                    Object returnValue = methodInvocation.proceed();
                    connection.commit();
                    committed = true;
                    return returnValue;
                } finally {
                    if (!committed) {
                        connection.rollback();
                    }
                }
            } finally {
                connection.close();
            }
        } finally {
            threadLocalConnection.set(null);
        }
    }

    static Connection getConnection() {
        Connection connection = threadLocalConnection.get();
        if (connection == null) {
            throw new IllegalArgumentException("Must decorate transactional methods with @JdbcTransaction attribute");
        }
        return connection;
    }

}
