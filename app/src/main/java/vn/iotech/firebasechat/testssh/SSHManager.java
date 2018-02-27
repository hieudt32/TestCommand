package vn.iotech.firebasechat.testssh;

import android.content.Context;
import android.content.res.AssetManager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
  private Context mContext;
  private int intConnectionPort;
  private String strPassword;
  private Session sesConnection;
  private int intTimeOut;
  private String privateKeyPath = "//android_asset/id_rsa";

  private void doCommonConstructorActions(String userName,
                                          String password, String connectionIP,
                                          String knownHostsFileName, Context context) {
    jschSSHChannel = new JSch();
    try {
      String path = copyAsset(mContext.getAssets());
      if (path != null) {
        jschSSHChannel.addIdentity(path);
      }
      jschSSHChannel.setKnownHosts(knownHostsFileName);
    } catch (JSchException jschX) {
      logError(jschX.getMessage());
    }

    strUserName = userName;
    strPassword = password;
    strConnectionIP = connectionIP;

  }

  private String copyAsset(AssetManager assetManager) {
    File key = new File(mContext.getFilesDir() + "/id_rsa");
    if (!key.exists()) {
      InputStream in;
      OutputStream out;
      try {
        in = assetManager.open("id_rsa");
        out = new FileOutputStream(key);
        copyFile(in, out);
        in.close();
        out.flush();
        out.close();
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    return key.getAbsolutePath();
  }

  private static void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }


  public SSHManager(String userName, String password,
                    String connectionIP, String knownHostsFileName, Context context) {
    mContext = context;
    doCommonConstructorActions(userName, password,
            connectionIP, knownHostsFileName, context);
    intConnectionPort = 22;
    intTimeOut = 60000;
  }

  public SSHManager(String userName, String password, String connectionIP,
                    String knownHostsFileName, int connectionPort, Context context) {
    mContext = context;
    doCommonConstructorActions(userName, password, connectionIP,
            knownHostsFileName, context);
    intConnectionPort = connectionPort;
    intTimeOut = 60000;
  }

  public SSHManager(String userName, String password, String connectionIP,
                    String knownHostsFileName, int connectionPort, int timeOutMilliseconds,
                    Context context) {
    mContext = context;
    doCommonConstructorActions(userName, password, connectionIP,
            knownHostsFileName, context);
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
