package vn.iotech.firebasechat.testssh;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

  private EditText mUsernameEt;
  private EditText mPasswordEt;
  private Button mConnectBt;
  private SSHManager mSshManager;
  private final String mTestCommand = "iwinfo wlan0 assoclist";
  private final String TAG = "SSH";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mUsernameEt = findViewById(R.id.user_name_et);
    mPasswordEt = findViewById(R.id.password_et);
    mConnectBt = findViewById(R.id.connect_bt);
    mConnectBt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new testSSHTask().execute();
      }
    });
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

  private class testSSHTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
      mSshManager = new SSHManager(mUsernameEt.getText().toString().trim(),
              mPasswordEt.getText().toString(), "192.168.1.1", "", 22);
      String error = mSshManager.connect();
      Log.d(TAG, error);
      if (error == null) {
        String response = mSshManager.sendCommand(mTestCommand);
        if (response != null && !response.isEmpty())
          Log.d(TAG, response);
      }
      return null;
    }
  }
}
