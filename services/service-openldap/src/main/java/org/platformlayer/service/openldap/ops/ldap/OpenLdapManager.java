package org.platformlayer.service.openldap.ops.ldap;

import java.io.File;
import java.util.List;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.ldap.LdapDN;
import org.platformlayer.ops.process.ProcessExecution;

import com.google.common.base.Strings;

public class OpenLdapManager {
    // static final String CMD_SLAPADD = "/usr/sbin/slapadd";
    private static final String CMD_LDAP_MODIFY = "/usr/bin/ldapmodify";
    private static final String CMD_LDAP_SEARCH = "/usr/bin/ldapsearch";

    // private static final String CMD_SLAPCAT = "/usr/sbin/slapcat";
    //
    // public static void offlineDatabaseInsert(OpsServer server, String ldifData, FilePath dataDir, String databaseSuffix, FilePath configDir) throws OpsException {
    // Agent agent = server.getAgent();
    // FilePath tempFile = agent.uploadTempTextFile(ldifData, FileMetadata.ROOT_ONLY);
    //
    // try {
    // // slapadd -l <inputfile> -f <slapdconfigfile>
    // // [-d <debuglevel>] [-n <integer>|-b <suffix>]
    // SimpleBashCommand command = BashCommand.build(CMD_SLAPADD);
    // command.addLiteral("-b");
    // command.addQuoted(databaseSuffix);
    // command.addLiteral("-l");
    // command.addFileArg(tempFile);
    // command.addLiteral("-F");
    // command.addFileArg(configDir);
    //
    // server.simpleRun(command);
    // } finally {
    // agent.rm(tempFile);
    // }
    //
    // agent.chownRecursive(dataDir, "openldap", "openldap");
    // }
    //
    // public static void doLdapBackup(MultitenantOpenLdapInstance openLdapInstance, String databaseSuffix, FilePath outputDirectory) throws OpsException {
    // // /usr/sbin/slapcat [-a filter] [-b suffix] [-c] [-d level] [-f slapd.conf] [-F confdir] [-g] [-l ldif-file] [-n dbnum] [-o name[=value]] [-s subtree-dn] [-v]
    // OpsServer server = openLdapInstance.smartGetServer(true);
    //
    // SimpleBashCommand command = BashCommand.build(CMD_SLAPCAT);
    //
    // command.addLiteral("-b"); // which database to backup?
    // command.addQuoted(databaseSuffix);
    //
    // command.addLiteral("-l"); // where do I want the output file?
    // command.addFileArg(outputDirectory);
    //
    // command.addLiteral("-F"); // what config directory
    // command.addFileArg(openLdapInstance.getConfigTreeDirectory());
    //
    // ProcessExecution execution = server.simpleRun(command);
    //
    // execution.checkExitCode();
    // }
    //
    // public static void addAttribute(OpenLdapInstance openLdapInstance, LdapDN bindDN, String password, LdapDN targetDn, String attribute, String attributeValue) throws OpsException {
    // StringBuilder ldifCommands = new StringBuilder();
    // ldifCommands.append("dn: " + targetDn.toLdifEncoded() + "\n");
    // ldifCommands.append("add: " + attribute + "\n");
    // ldifCommands.append(attribute + ": " + attributeValue + "\n");
    //
    // doLdapModify(openLdapInstance, bindDN, password, false, ldifCommands.toString());
    // }

    private static void doLdapModify(OpsTarget target, LdapDN bindDN, String password, boolean add, String ldifCommands) throws OpsException {
        File ldifTempDir = target.createTempDir();
        File ldifTempFile = new File(ldifTempDir, "ldapmodify.ldif");
        target.setFileContents(ldifTempFile, ldifCommands);
        try {
            Command command = Command.build(CMD_LDAP_MODIFY);

            if (add)
                command.addLiteral("-a"); // Add

            command.addLiteral("-x"); // Simple auth

            command.addLiteral("-D"); // Bind DN
            command.addQuoted(bindDN.toLdifEncoded());

            command.addLiteral("-w"); // Simple auth password
            command.addQuoted(password);

            command.addLiteral("-f"); // Command file
            command.addFile(ldifTempFile);

            target.executeCommand(command);
        } finally {
            target.rmdir(ldifTempDir);
        }
    }

    public static void doLdapModify(OpsTarget target, LdapDN bindDN, String password, boolean add, LdifRecord ldifRecord) throws OpsException {
        doLdapModify(target, bindDN, password, add, ldifRecord.toLdifText());
    }

    public enum SearchScope {
        Base, One, Sub, Children
    };

    public static List<LdifRecord> doLdapQuery(OpsTarget target, LdapDN bindDN, String ldapPassword, LdapDN searchBaseDN, String filter, SearchScope searchScope) throws OpsException {
        Command command = Command.build(CMD_LDAP_SEARCH);

        command.addLiteral("-LLL"); // Pure LDIF, no extra junk

        command.addLiteral("-x"); // Simple auth

        command.addLiteral("-D"); // Bind DN
        command.addQuoted(bindDN.toLdifEncoded());

        command.addLiteral("-w"); // Simple auth password
        command.addQuoted(ldapPassword);

        command.addLiteral("-b"); // Search base
        command.addQuoted(searchBaseDN.toLdifEncoded());

        command.addLiteral("-s"); // Scope
        command.addLiteral(searchScope.toString().toLowerCase());

        if (!Strings.isNullOrEmpty(filter)) {
            command.addQuoted(filter);
        }

        ProcessExecution processExecution = target.executeCommand(command);

        return LdifRecord.parse(processExecution.getStdOut());
    }

    public static LdifRecord doLdapQuerySingle(OpsTarget openLdapInstance, LdapDN bindDN, String ldapPassword, LdapDN searchBaseDN, String filter) throws OpsException {
        List<LdifRecord> results = doLdapQuery(openLdapInstance, bindDN, ldapPassword, searchBaseDN, filter, SearchScope.Base);
        if (results.size() == 0)
            return null;
        if (results.size() != 1)
            throw new OpsException("Got multiple LDAP results, expecting one for " + searchBaseDN);
        return results.get(0);
    }

    // public static List<LdifRecord> doLdapQueryChildren(MultitenantOpenLdapInstance openLdapInstance, LdapDN bindDN, String ldapPassword, LdapDN searchBaseDN, String filter) throws OpsException {
    // List<LdifRecord> results = doLdapQuery(openLdapInstance, bindDN, ldapPassword, searchBaseDN, filter, SearchScope.One);
    // return results;
    // }
}
