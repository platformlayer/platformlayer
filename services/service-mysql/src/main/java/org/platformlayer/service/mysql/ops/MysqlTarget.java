package org.platformlayer.service.mysql.ops;

import org.platformlayer.core.model.Secret;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.Command.Argument;
import org.platformlayer.ops.OpsContext;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.process.ProcessExecution;
import org.platformlayer.ops.process.ProcessExecutionException;

public class MysqlTarget {
    final String hostname;
    final String username;
    Secret password;

    public MysqlTarget(String hostname, String username, Secret password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    public void changePassword(Secret newPassword) throws OpsException {
        OpsTarget target = getOpsTarget();

        Command command = Command.build("mysqladmin");
        command.addQuoted("--user=", username);
        command.addQuoted("--host=", hostname);
        command.addQuoted("--password=", password);
        command.addLiteral("password");
        command.addQuoted(newPassword);

        target.executeCommand(command);

        password = newPassword;
    }

    private OpsTarget getOpsTarget() {
        return OpsContext.get().getInstance(OpsTarget.class);
    }

    public ProcessExecution execute(String sql, String maskedSql) throws OpsException {
        OpsTarget target = getOpsTarget();

        Command command = Command.build("mysql");
        command.addQuoted("--user=", username);
        command.addQuoted("--host=", hostname);
        command.addQuoted("--password=", password);

        command.addArgument(Argument.buildQuoted("--execute=", sql).setMasked("--execute=" + Command.MASKED));

        return target.executeCommand(command);
    }

    public ProcessExecution execute(String sql) throws OpsException {
        return execute(sql, sql);
    }

    // public static MysqlTarget resolve(String mysqlHost) throws OpsException {
    // MysqlServer mysqlServer;
    // try {
    // mysqlServer = findMysqlServer(mysqlHost);
    // } catch (OpenstackClientException e) {
    // throw new OpsException("Error listing mysql servers", e);
    // }
    //
    // if (mysqlServer == null) {
    // throw new OpsException("Cannot resolve mysql server: " + mysqlHost);
    // }
    //
    // return new MysqlTarget(mysqlServer.dnsName, "root", mysqlServer.rootPassword);
    // }
    //
    // private static MysqlServer findMysqlServer(String mysqlHost) throws OpenstackClientException {
    // PlatformLayerClient platformLayer = Injection.getInstance(PlatformLayerClient.class);
    //
    // for (MysqlServer mysqlServer : platformLayer.listItems(MysqlServer.class)) {
    // if (Objects.equal(mysqlServer.dnsName, mysqlHost)) {
    // return mysqlServer;
    // }
    // }
    //
    // return null;
    // }

    public boolean canLogin() throws OpsException {
        try {
            String testSql = "SHOW STATUS LIKE 'uptime'";

            execute(testSql);
            return true;
        } catch (ProcessExecutionException e) {
            ProcessExecution execution = e.getExecution();
            if (execution.getExitCode() == 1 && execution.getStdErr().contains("Access denied")) {
                return false;
            }
            throw new OpsException("Unexpected error connecting to MySQL", e);
        }
    }
}
