package org.platformlayer.ssh.mina.bugfix;

import java.io.IOException;
import java.io.InputStream;

import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.util.Buffer;

public class BugFixChannelExec extends ChannelExec {
    private static final boolean USE_BUG_FIX = true;

    public BugFixChannelExec(String command) {
        super(command);
    }

    @Override
    protected void sendEof() throws IOException {
        if (closeFuture.isClosed()) {
            log.warn("Blocking attempt to send SSH_MSG_CHANNEL_EOF on closed channel");
            return;
        }
        if (recipient == 0) {
            log.warn("Blocking attempt to send SSH_MSG_CHANNEL_EOF with recipient == 0");
            return;
        }
        super.sendEof();
    }

    // I hit this bug (100% CPU); it's fixed in the latest SVN.
    // TODO: Remove BugFixChannelExec when the if (len < 0) sendEof() logic is in mina-sshd.
    @Override
    protected void pumpInputStream() {
        try {
            while (!closeFuture.isClosed()) {
                Buffer buffer = session.createBuffer(SshConstants.Message.SSH_MSG_CHANNEL_DATA, 0);
                buffer.putInt(recipient);
                int wpos1 = buffer.wpos(); // keep buffer position to write data length later
                buffer.putInt(0);
                int wpos2 = buffer.wpos(); // keep buffer position for data write
                buffer.wpos(wpos2 + remoteWindow.getPacketSize()); // Make room
                int len = securedRead(in, buffer.array(), wpos2, remoteWindow.getPacketSize()); // read data into buffer
                if (len > 0) {
                    buffer.wpos(wpos1);
                    buffer.putInt(len);
                    buffer.wpos(wpos2 + len);
                    remoteWindow.waitAndConsume(len);
                    log.debug("Send SSH_MSG_CHANNEL_DATA on channel {}", id);
                    session.writePacket(buffer);
                } else {
                    // This is the bug fix we need!!
                    sendEof();
                    break;
                }
            }
        } catch (Exception e) {
            if (!closing) {
                log.info("Caught exception", e);
                close(false);
            }
        }
    }

    //
    // On some platforms, a call to System.in.read(new byte[65536], 0,32768) always throws an IOException.
    // So we need to protect against that and chunk the call into smaller calls.
    // This problem was found on Windows, JDK 1.6.0_03-b05.
    //
    @Override
    protected int securedRead(InputStream in, byte[] buf, int off, int len) throws IOException {
        int n = 0;
        for (;;) {
            int nread = in.read(buf, off + n, Math.min(1024, len - n));
            if (nread <= 0) {
                return (n == 0) ? nread : n;
            }
            n += nread;
            if (n >= len) {
                return n;
            }
            // if not closed but no bytes available, return
            if (in != null && in.available() <= 0) {
                return n;
            }
        }
    }

    public static ClientChannel createExecChannel(ClientSession clientSession, String command) throws Exception {
        if (!USE_BUG_FIX) {
            return clientSession.createExecChannel(command);
        } else {
            BugFixChannelExec channel = new BugFixChannelExec(command);
            ((org.apache.sshd.client.session.ClientSessionImpl) clientSession).registerChannel(channel);
            return channel;
        }
    }

}
