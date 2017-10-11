package by.citech.websocketduplex;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import by.citech.websocketduplex.server.asynctask.ServerOffTask;
import by.citech.websocketduplex.server.asynctask.ServerOnTask;
import by.citech.websocketduplex.server.network.NanoWebSocketServerCtrl;
import by.citech.websocketduplex.util.OpMode;
import by.citech.websocketduplex.util.StatusMessages;
import by.citech.websocketduplex.util.Tags;
import static by.citech.websocketduplex.util.NetworkInfo.getIPAddress;

public class ServerActivity extends Activity implements OnCheckedChangeListener {
    private static final String DEFAULT_PORT = "8080";
    private static final boolean IPV4 = true;

    public NanoWebSocketServerCtrl serverCtrl;

    public ToggleButton btnSrvRedirectData;

    public Button btnSrvOn;
    public Button btnSrvOff;
    public Button btnSrvSendMsg;

    public EditText editTextSrvPort;
    public EditText editTextSrvIp;
    public EditText editTextSrvToCltText;

    public TextView textViewSrvStatus;
    public TextView textViewSrvFromCltByte;
    public TextView textViewSrvFromCltText;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Tags.ACT_SRV, "onCreate");
        setContentView(R.layout.activity_server);
        final ServerActivity activity = this;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case StatusMessages.SRV_ONMESSAGE:
                        textViewSrvFromCltText.setText((String) msg.obj);
                        break;
                    case StatusMessages.SRV_ONCLOSE:
                        textViewSrvStatus.setText("Состояние: сокет закрыт.");
                        Toast.makeText(activity, "CLOSED", Toast.LENGTH_SHORT).show();
                        editTextSrvToCltText.setVisibility(View.INVISIBLE);
                        btnSrvSendMsg.setEnabled(false);
                        break;
                    case StatusMessages.SRV_ONOPEN:
                        textViewSrvStatus.setText("Состояние: сокет открыт.");
                        editTextSrvToCltText.setVisibility(View.VISIBLE);
                        btnSrvSendMsg.setEnabled(true);
                        Toast.makeText(activity, "OPENED", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusMessages.SRV_ONDEBUGFRAMERX:
                        Toast.makeText(activity, "RECEIVED", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusMessages.SRV_ONDEBUGFRAMETX:
                        Toast.makeText(activity, "SENDED", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusMessages.SRV_ONEXCEPTION:
                        Toast.makeText(activity, "EXCEPTION", Toast.LENGTH_SHORT).show();
                        editTextSrvToCltText.setVisibility(View.INVISIBLE);
                        break;
                    case StatusMessages.SRV_ONPONG:
                        Toast.makeText(activity, "PONGED", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(activity, "UNKNOWN", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btnSrvOn = (Button) findViewById(R.id.btnSrvOn);
        btnSrvOff = (Button) findViewById(R.id.btnSrvOff);
        btnSrvRedirectData = (ToggleButton) findViewById(R.id.btnSrvRedirectData);
        btnSrvSendMsg = (Button) findViewById(R.id.btnSrvSendMsg);

        editTextSrvPort = (EditText) findViewById(R.id.editTextSrvPort);
        editTextSrvIp = (EditText) findViewById(R.id.editTextSrvIp);
        editTextSrvToCltText = (EditText) findViewById(R.id.editTextSrvToCltText);

        textViewSrvStatus = (TextView) findViewById(R.id.textViewSrvStatus);
        textViewSrvFromCltByte = (TextView) findViewById(R.id.textViewSrvFromCltByte);
        textViewSrvFromCltText = (TextView) findViewById(R.id.textViewSrvFromCltText);

        editTextSrvToCltText.setVisibility(View.INVISIBLE);
        editTextSrvPort.setText(DEFAULT_PORT);
        editTextSrvIp.setText(getIPAddress(IPV4));
        editTextSrvIp.setFocusable(false);

        btnSrvOff.setEnabled(false);
        btnSrvRedirectData.setOnCheckedChangeListener(this);
        btnSrvSendMsg.setEnabled(false);

        textViewSrvStatus.setText("Состояние: ожидание.");
    }

    public void offServer(View view) {
        Log.i(Tags.ACT_SRV, "offServer");
        new ServerOffTask(this, serverCtrl).execute();
    }

    public void onServer(View view) {
        Log.i(Tags.ACT_SRV, "onServer");
        btnSrvOn.setEnabled(false);
        new ServerOnTask(this, handler).execute(editTextSrvPort.getText().toString(), OpMode.SRV_DEBUG);
    }

    public void sendMessage(View view) {
        Log.i(Tags.ACT_SRV, "sendMessage");
        serverCtrl.sendMessage(editTextSrvToCltText.getText().toString());
        editTextSrvToCltText.setText("");
    }

    @Override
    protected void onStop() {
        super.onStop();
        new ServerOffTask(this, serverCtrl).execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new ServerOffTask(this, serverCtrl).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        new ServerOffTask(this, serverCtrl).execute();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(Tags.ACT_SRV, "redirectData");
    }
}
