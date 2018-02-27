package vn.iotech.firebasechat.testssh;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

  private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
  private String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
  private EditText mUsernameEt;
  private EditText mPasswordEt;
  private Button mConnectBt;
  private Button mGetSystem;
  private SSHManager mSshManager;
  private final String mTestCommand = "iwinfo wlan0 assoclist";
  private final String getSystemifor = "cat /proc/cpuinfo";
  private final String TAG = "SSH";
  private String error = null;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mUsernameEt = findViewById(R.id.user_name_et);
    mPasswordEt = findViewById(R.id.password_et);
    mConnectBt = findViewById(R.id.connect_bt);
    mGetSystem = findViewById(R.id.get_system_bt);
    mConnectBt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new testSSHTask().execute(mTestCommand);
      }
    });
    mGetSystem.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new testSSHTask().execute(getSystemifor);
      }
    });
    requestPermission();
  }

  private void requestPermission() {
    int check = 0;
    for (int i = 0; i < PERMISSIONS.length; i++) {
      if (ContextCompat.checkSelfPermission(MainActivity.this, PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
        check++;
      }
    }
    if (check > 0) {
      ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_WRITE_EXTERNAL_STORAGE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      Log.d("App", "granted");
    } else {
      Log.d("App", "not granted");
      finish();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mSshManager.close();
  }

  private class testSSHTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... string) {
      mSshManager = new SSHManager(mUsernameEt.getText().toString().trim(),
              mPasswordEt.getText().toString(), "192.168.1.1", "", 22, getApplicationContext());
      error = mSshManager.connect();
      if (error == null) {
        String response = mSshManager.sendCommand(string[0]);
        if (response != null && !response.isEmpty())
          Log.d(TAG, response);
      } else {
        Log.d(TAG, error);
      }
      return null;
    }
  }
}
