package vn.iotech.firebasechat.testssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by akai on 2/23/2018.
 */

public class SSHManager {
  private static final Logger LOGGER = Logger.getLogger(SSHManager.class.getName());
  private JSch jschSSHChannel;
  private String strUserName;
  private String strConnectionIP;
  private int intConnectionPort;
  private String strPassword;
  private Session sesConnection;
  private int intTimeOut;
  private String privateKeyPath = "//android_asset/id_rsa";

  private void doCommonConstructorActions(String userName,
                                          String password, String connectionIP, String knownHostsFileName) {
    jschSSHChannel = new JSch();
    try {
      jschSSHChannel.addIdentity(getPath());

      jschSSHChannel.setKnownHosts(knownHostsFileName);
    } catch (JSchException jschX) {
      logError(jschX.getMessage());
    }

    strUserName = userName;
    strPassword = password;
    strConnectionIP = connectionIP;
  }

  private String getPath() {
    File key = new File(privateKeyPath);
    return key.getAbsolutePath();
  }

  public SSHManager(String userName, String password,
                    String connectionIP, String knownHostsFileName) {
    doCommonConstructorActions(userName, password,
            connectionIP, knownHostsFileName);
    intConnectionPort = 22;
    intTimeOut = 60000;
  }

  public SSHManager(String userName, String password, String connectionIP,
                    String knownHostsFileName, int connectionPort) {
    doCommonConstructorActions(userName, password, connectionIP,
            knownHostsFileName);
    intConnectionPort = connectionPort;
    intTimeOut = 60000;
  }

  public SSHManager(String userName, String password, String connectionIP,
                    String knownHostsFileName, int connectionPort, int timeOutMilliseconds) {
    doCommonConstructorActions(userName, password, connectionIP,
            knownHostsFileName);
    intConnectionPort = connectionPort;
    intTimeOut = timeOutMilliseconds;
  }

  public String connect() {
    String errorMessage = null;

    try {
      sesConnection = jschSSHChannel.getSession(strUserName,
              strConnectionIP, intConnectionPort);
      sesConnection.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
//      sesConnection.setPassword(strPassword);
      // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION
      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");
      sesConnection.setConfig(config);
      sesConnection.connect(intTimeOut);
    } catch (JSchException jschX) {
      errorMessage = jschX.getMessage();
    }

    return errorMessage;
  }

  private String logError(String errorMessage) {
    if (errorMessage != null) {
      LOGGER.log(Level.SEVERE, "{0}:{1} - {2}",
              new Object[]{strConnectionIP, intConnectionPort, errorMessage});
    }

    return errorMessage;
  }

  private String logWarning(String warnMessage) {
    if (warnMessage != null) {
      LOGGER.log(Level.WARNING, "{0}:{1} - {2}",
              new Object[]{strConnectionIP, intConnectionPort, warnMessage});
    }

    return warnMessage;
  }

  public String sendCommand(String command) {
    StringBuilder outputBuffer = new StringBuilder();

    try {
      Channel channel = sesConnection.openChannel("exec");
      ((ChannelExec) channel).setCommand(command);
      InputStream commandOutput = channel.getInputStream();
      channel.connect();
      int readByte = commandOutput.read();

      while (readByte != 0xffffffff) {
        outputBuffer.append((char) readByte);
        readByte = commandOutput.read();
      }

      channel.disconnect();
    } catch (IOException ioX) {
      logWarning(ioX.getMessage());
      return null;
    } catch (JSchException jschX) {
      logWarning(jschX.getMessage());
      return null;
    }

    return outputBuffer.toString();
  }

  public void close() {
    sesConnection.disconnect();
  }

}
