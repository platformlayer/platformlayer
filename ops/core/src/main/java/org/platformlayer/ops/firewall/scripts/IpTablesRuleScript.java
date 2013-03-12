package org.platformlayer.ops.firewall.scripts;

import java.io.File;

import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.filesystem.FileAccess;
import org.platformlayer.ops.filesystem.SyntheticFile;
import org.platformlayer.ops.firewall.Sanitizer;
import org.platformlayer.ops.firewall.Transport;
import org.platformlayer.ops.networks.ScriptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IpTablesRuleScript extends SyntheticFile {
	private static final Logger log = LoggerFactory.getLogger(IpTablesRuleScript.class);

	public String ruleKey;
	public String interfaceName = "eth0";

	public IpTablesRuleScript() {
		this.fileMode = "0755";
		this.mkdirs = FileAccess.ROOT_OWNED_READABLE_DIRECTORY;
	}

	@Override
	protected File getFilePath() throws OpsException {
		File scriptDirectory = new File(PersistIptablesScripts.BASE_DIR, interfaceName);

		File transportDirectory;
		switch (getRuleTransport()) {
		case Ipv4:
			transportDirectory = new File(scriptDirectory, "inet");
			break;
		case Ipv6:
			transportDirectory = new File(scriptDirectory, "inet6");
			break;
		default:
			throw new IllegalStateException();
		}

		if (ruleKey == null) {
			throw new OpsException("ruleKey is required");
		}

		String fileName = Sanitizer.forFileName().clean(ruleKey);

		File scriptFile = new File(transportDirectory, fileName);

		return scriptFile;
	}

	protected abstract IptablesRule getRule() throws OpsException;

	protected abstract Transport getRuleTransport() throws OpsException;

	@Override
	protected void doUpdateAction(OpsTarget target) throws OpsException {
		super.doUpdateAction(target);

		Command executeScript = Command.build("{0}", getFilePath());
		target.executeCommand(executeScript);
	}

	@Override
	protected void doDeleteAction(OpsTarget target) throws OpsException {
		super.doDeleteAction(target);

		Command removeRule = buildIptablesDeleteCommand();
		target.executeCommand(removeRule);
	}

	@Override
	protected byte[] getContentsBytes() throws OpsException {
		Command command = buildIptablesAddCommand();

		ScriptBuilder sb = new ScriptBuilder();
		sb.addMetadata("key", ruleKey);
		sb.add(command);

		return sb.getBytes();
	}

	private Command buildIptablesAddCommand() throws OpsException {
		Command command = getRule().buildIptablesAddCommand();
		addKey(command);
		return command;
	}

	private Command buildIptablesDeleteCommand() throws OpsException {
		Command command = getRule().buildIptablesDeleteCommand();
		addKey(command);
		return command;
	}

	private void addKey(Command command) {

		command.addLiteral("-m").addLiteral("comment");
		command.addLiteral("--comment").addQuoted(ruleKey);
	}
}
