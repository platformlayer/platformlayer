package org.platformlayer.ops.images.direct;

import java.io.File;
import java.net.InetAddress;

import org.platformlayer.TimeSpan;
import org.platformlayer.ops.Command;
import org.platformlayer.ops.OpsException;
import org.platformlayer.ops.OpsTarget;
import org.platformlayer.ops.SshOpsTarget;
import org.platformlayer.ops.machines.InetAddressUtils;
import org.platformlayer.ops.tree.OpsTreeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fathomdb.hash.Md5Hash;
import com.google.common.base.Objects;
import com.google.common.net.InetAddresses;

public class SshAgentPeerToPeerCopy implements PeerToPeerCopy {

	static final Logger log = LoggerFactory.getLogger(SshAgentPeerToPeerCopy.class);

	@Override
	public void copy(final OpsTarget src, final File srcFile, final OpsTarget dest, final File finalDestFile)
			throws OpsException {
		File tempDir = dest.createTempDir();

		try {
			final File tempDest = new File(tempDir, finalDestFile.getName());

			{
				SshOpsTarget srcSshOpsTarget = (SshOpsTarget) src;

				Command pullCommand = Command.build("scp -o StrictHostKeyChecking=no {0}@{1}:{2} {3}",
						srcSshOpsTarget.getUsername(), InetAddresses.toAddrString(srcSshOpsTarget.getHost()), srcFile,
						tempDest);

				pullCommand.setKeyPair(srcSshOpsTarget.getKeyPair());

				dest.executeCommand(pullCommand.setTimeout(TimeSpan.TEN_MINUTES));

				Md5Hash targetHash = dest.getFileHash(tempDest);
				Md5Hash srcHash = src.getFileHash(srcFile);

				if (!Objects.equal(srcHash, targetHash)) {
					dest.rm(tempDest);
					throw new OpsException("Files did not match after transfer");
				}
			}

			dest.mv(tempDest, finalDestFile);
		} finally {
			dest.rmdir(tempDir);
		}
	}

	@Override
	public void addChildren(OpsTreeBase parent) {

	}

}
