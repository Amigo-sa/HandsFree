package by.citech.websocketduplex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import by.citech.R;
import by.citech.websocketduplex.client.asynctask.DisconnectTask;
import by.citech.websocketduplex.client.asynctask.OpenWebSocketTask;
import by.citech.websocketduplex.client.asynctask.SendMessageToServerTask;
import by.citech.websocketduplex.client.asynctask.StreamTask;
import by.citech.websocketduplex.client.websocket.OkWebSocketClientCtrl;
import by.citech.websocketduplex.utils.DataSources;
import by.citech.websocketduplex.utils.Tags;

public class ClientActivity extends AppCompatActivity {
    private static final String DEFAULT_IP = "192.168.0.105";
    private static final String DEFAULT_PORT = "8080";

    public OkWebSocketClientCtrl clientCtrl;

    public TextView textViewCltStatus;
    public EditText editTextCltToSrvText;
    public EditText editTextCltRemSrvAddr;
    public EditText editTextCltRemSrvPort;
    public Button btnCltConnToSrv;
    public Button btnCltDiscFromSrv;
    public Button btnCltStreamOff;
    public Button btnCltStreamOn;
    public Button btnCltSendMsg;
    //Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Tags.ACT_CLT, "onCreate");
        setContentView(R.layout.activity_client);

        btnCltConnToSrv = (Button) findViewById(R.id.btnCltConnToSrv);
        btnCltDiscFromSrv = (Button) findViewById(R.id.btnCltDiscFromSrv);
        btnCltSendMsg = (Button) findViewById(R.id.btnCltSendMsg);
        btnCltStreamOn = (Button) findViewById(R.id.btnCltStreamOn);
        btnCltStreamOff = (Button) findViewById(R.id.btnCltStreamOff);
        editTextCltToSrvText = (EditText) findViewById(R.id.editTextCltToSrvText);
        editTextCltRemSrvAddr = (EditText) findViewById(R.id.editTextCltRemSrvAddr);
        editTextCltRemSrvPort = (EditText) findViewById(R.id.editTextCltRemSrvPort);
        textViewCltStatus = (TextView) findViewById(R.id.textViewCltStatus);

        editTextCltToSrvText.setVisibility(View.INVISIBLE);
        editTextCltRemSrvAddr.setText(DEFAULT_IP);
        editTextCltRemSrvPort.setText(DEFAULT_PORT);

        btnCltDiscFromSrv.setEnabled(false);
        btnCltSendMsg.setEnabled(false);
        btnCltStreamOn.setEnabled(false);
        btnCltStreamOff.setEnabled(false);
        textViewCltStatus.setText("Состояние: ожидание");
    }

    public void disconnect(View view) {
        Log.i(Tags.ACT_CLT, "disconnect");
        btnCltDiscFromSrv.setEnabled(false);
        new DisconnectTask(this).execute(clientCtrl);
    }

    public void streamOn(View view) {
        Log.i(Tags.ACT_CLT, "streamOn");
        btnCltStreamOn.setEnabled(false);
        btnCltDiscFromSrv.setEnabled(false);
        btnCltSendMsg.setEnabled(false);
        new StreamTask(this, clientCtrl).execute(DataSources.DEBUG);
    }

    public void streamOff(View view) {
        Log.i(Tags.ACT_CLT, "streamOff");
        btnCltStreamOff.setEnabled(false);

    }

    public void connect(View view) {
        Log.i(Tags.ACT_CLT, "connect");
        btnCltConnToSrv.setEnabled(false);
        new OpenWebSocketTask(this).execute(String.format("ws://%s:%s", editTextCltRemSrvAddr.getText().toString(), editTextCltRemSrvPort.getText().toString()));
    }

    public void sendMessage(View view) {
        Log.i(Tags.ACT_CLT, "sendMessage");
        btnCltSendMsg.setEnabled(false);
        new SendMessageToServerTask(this, clientCtrl).execute(editTextCltToSrvText.getText().toString());
    }
}
