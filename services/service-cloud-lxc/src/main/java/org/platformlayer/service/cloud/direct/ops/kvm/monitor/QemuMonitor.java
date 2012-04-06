package org.platformlayer.service.cloud.direct.ops.kvm.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.openstack.utils.Utf8;

public class QemuMonitor {
    static final Logger log = Logger.getLogger(QemuMonitor.class);

    private final InetAddress address;
    private final int port;

    final Socket socket;
    final BufferedReader reader;

    final OutputStreamWriter writer;

    Object cacheSupportedCommands;

    public QemuMonitor(InetAddress address, int port) throws IOException {
        this.address = address;
        this.port = port;
        this.socket = open();

        this.reader = new BufferedReader(Utf8.openReader(socket.getInputStream()));
        this.writer = Utf8.openWriter(socket.getOutputStream());
        negotiate();
    }

    Socket open() throws IOException {
        Socket socket = new Socket(address, port);
        return socket;
    }

    private void negotiate() throws IOException {
        JSONObject serverHello = readJson();
        execute("qmp_capabilities");
    }

    public void close() throws IOException {
        socket.close();
    }

    // class Response {
    // final JSONObject json;
    //
    // public Response(JSONObject json) {
    // this.json = json;
    // }
    //
    // public Object get(String key) throws JSONException {
    // return json.get(key);
    // }
    //
    // public boolean has(String key) throws JSONException {
    // return json.has(key);
    // }
    // }

    Object execute(String command) throws IOException {
        return execute(command, null);
    }

    Object execute(String command, Object arguments) throws IOException {
        send(command, arguments);
        JSONObject response = readJson();
        if (response.has("error")) {
            throw new IOException("Error connecting to KVM monitor: " + response);
        }

        try {
            return response.get("return");
        } catch (JSONException e) {
            throw new IOException("Error parsing return message: " + response);
        }
    }

    void send(String command, Object args) throws IOException {
        String msgJson;
        try {
            JSONObject msg = new JSONObject();
            msg.put("execute", command);
            if (args != null) {
                msg.put("arguments", args);
            }
            msgJson = msg.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("Error building JSON message", e);
        }
        // Apparently we may need a dummy byte with some KVM versions
        msgJson += ' ';

        log.debug("QMP sending: " + msgJson);

        writer.write(msgJson);
        writer.flush();

    }

    protected void onEvent(JSONObject event) {
        log.debug("Got KVM event: " + event);
    }

    JSONObject readJson() throws IOException {
        while (true) {
            String line = reader.readLine();
            log.debug("QMP read: " + line);
            JSONObject json = (JSONObject) JSONObject.stringToValue(line);

            if (json.has("event")) {
                onEvent(json);
            } else {
                return json;
            }
        }
    }

    Object doQueryVersion() throws IOException {
        return execute("query-version");
    }

    Object doQueryKvm() throws IOException {
        return execute("query-kvm");
    }

    Object doQueryVnc() throws IOException {
        return execute("query-vnc");
    }

    Object doQueryUuid() throws IOException {
        return execute("query-uuid");
    }

    Object doQueryBlock() throws IOException {
        return execute("query-block");
    }

    Object doQueryBlockStats() throws IOException {
        return execute("query-blockstats");
    }

    Object doSystemReset() throws IOException {
        return execute("system_reset");
    }

    Object doPause() throws IOException {
        return execute("pause");
    }

    Object unpause() throws IOException {
        return execute("cont");
    }

    Object stop() throws IOException {
        return execute("quit");
    }

    // + def human_monitor_command(self, command):
    // 119
    // + return self.execute('human-monitor-command',
    // 120
    // + {'command-line': command})
    // 121
    // +
    // 122
    // + def drive_add(self, file, interface, id, boot, format, media):
    // 123
    // + # TODO(justinsb): It sucks that the return value changes...
    // 124
    // +# if self.is_command_supported('drive_add'):
    // 125
    // +# return self.execute('drive_add', { 'file': file,
    // 126
    // +# 'if': interface,
    // 127
    // +# 'id': id,
    // 128
    // +# 'boot': boot,
    // 129
    // +# 'format': format,
    // 130
    // +# 'media': media })
    // 131
    // + drive_spec = ('file=%s,if=%s,id=%s,boot=%s,format=%s,media=%s' %
    // 132
    // + (file, interface, id, boot, format, media))
    // 133
    // + text = 'drive_add %s %s' % ('dummy', drive_spec)
    // 134
    // +
    // 135
    // + return self.human_monitor_command(text)
    // 136
    // +
    // 137
    // + def device_add(self, driver, bus, drive, id):
    // 138
    // +# return self.execute('device_add', { 'driver': driver,
    // 139
    // +# 'bus': bus,
    // 140
    // +# 'drive': drive,
    // 141
    // +# 'id': id })
    // 142
    // + device_spec = ('%s,bus=%s,drive=%s,id=%s' %
    // 143
    // + (driver, bus, drive, id))
    // 144
    // + text = 'device_add %s' % (device_spec)
    // 145
    // +
    // 146
    // + return self.human_monitor_command(text)
    // 147
    // +
    // 148
    // + def is_command_supported(self, command):
    // 149
    // + return command in self.supported_commands()
    // 150
    // +
    // 151

    Object getSupportedCommands() throws IOException {
        if (cacheSupportedCommands == null) {
            this.cacheSupportedCommands = execute("query-commands");
        }
        return cacheSupportedCommands;
    }
    // + commands = self.execute('query-commands')
    // 154
    // + supported_commands = []
    // 155
    // + for command in commands:
    // 156
    // + supported_commands.append(command['name'])
    // 157
    // + self._cache_supported_commands = supported_commands
    // 158
    // + return self._cache_supported_commands
}
