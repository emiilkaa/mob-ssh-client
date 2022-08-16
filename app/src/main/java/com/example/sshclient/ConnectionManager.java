package com.example.sshclient;

import com.jcraft.jsch.*;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class ConnectionManager {
    private static Session session;

    private static String hostname;
    private static int port = 22;
    private static String username;
    private static String password;

    private static int flag = -1;

    public ConnectionManager() {
    }

    public ConnectionManager(String hostname, String username, String password) {
        ConnectionManager.hostname = hostname;
        ConnectionManager.username = username;
        ConnectionManager.password = password;
    }

    public ConnectionManager(String hostname, String username, String password, int port) {
        ConnectionManager.hostname = hostname;
        ConnectionManager.username = username;
        ConnectionManager.password = password;
        ConnectionManager.port = port;
    }

    public static void setHostname(String hostname) {
        ConnectionManager.hostname = hostname;
    }

    public static void setPort(int port) {
        ConnectionManager.port = port;
    }

    public static void setUsername(String username) {
        ConnectionManager.username = username;
    }

    public static void setPassword(String password) {
        ConnectionManager.password = password;
    }

    public static void setFlag(int flag) {
        ConnectionManager.flag = flag;
    }

    public static int getFlag() {
        return ConnectionManager.flag;
    }

    public static void open() throws JSchException {
        open(hostname, username, password, port);
    }

    public static void open(String hostname, String username, String password, int port) throws JSchException {
        try {
            if (session != null) {
                close();
            }
            JSch jSch = new JSch();

            session = jSch.getSession(username, hostname, port);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);

            session.connect(30000);
            flag = 0;
        } catch (JSchException e) {
            flag = 1;
            throw e;
        }
    }

    public static String runCommand(String command) throws JSchException, IOException {
        String result;

        if (!session.isConnected())
            throw new RuntimeException("Not connected to an open session");

        ChannelExec channel = (ChannelExec)session.openChannel("exec");

        channel.setCommand(command);

        InputStream in = channel.getInputStream();

        channel.connect();

        result = getChannelOutput(channel, in);

        channel.disconnect();

        return result;
    }

    private static String getChannelOutput(Channel channel, InputStream in) {
        byte[] buffer = new byte[1024];
        StringBuilder res = new StringBuilder();

        try {
            String line = "";
            int k = 0;
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(buffer, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    line = new String(buffer, 0, i);
                    res.append(line);
                }

                if (line.contains("logout")) {
                    break;
                }
                if (channel.isClosed()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
                if (++k >= 50) {
                    res.append("\n...");
                    break;
                }
            }
        } catch (Exception e){
            System.out.println("Error while reading channel output: " + e);
        }
        return res.toString();
    }

    public static void close() {
        session.disconnect();
    }
}
