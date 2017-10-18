package by.citech.websocketduplex;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import by.citech.websocketduplex.data.StorageData;

public class DuplexActivity extends Activity {
    private EditText editTextSrvRemPort;
    private EditText editTextSrvLocPort;
    private EditText editTextSrvRemAddr;
    private Button btnCall;
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplex);
    }
}
