package by.citech;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import by.citech.client.asynctask.OpenWebSocketTask;
import by.citech.client.asynctask.SendMessageToServerTask;
import by.citech.client.asynctask.StreamTask;
import by.citech.client.network.IClientCtrl;
import by.citech.client.network.IClientOn;
import by.citech.client.network.IMessage;
import by.citech.client.network.IStream;
import by.citech.client.network.IStreamOn;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;
import by.citech.server.asynctask.RedirectDataTask;
import by.citech.server.asynctask.ServerOnTask;
import by.citech.server.network.IRedirectCtrl;
import by.citech.server.network.IRedirectOn;
import by.citech.server.network.IServerOn;
import by.citech.server.network.IServerCtrl;
import by.citech.server.network.websockets.WebSocketFrame;
import static by.citech.util.NetworkInfo.getIPAddress;

public class DuplexActivity extends Activity implements IServerOn, IRedirectOn, IStreamOn, IClientOn, IMessage {
    private EditText editTextSrvLocPort;
    private EditText editTextSrvLocAddr;
    private EditText editTextSrvRemPort;
    private EditText editTextSrvRemAddr;
    private Button btnCall;
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;
    private Handler handler;
    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private IRedirectCtrl iRedirectCtrl;
    private IStream iStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplex);
        final DuplexActivity activity = this;
        storageBtToNet = new StorageData(Tags.DPL_STORE_BT2NET);
        storageNetToBt = new StorageData(Tags.DPL_STORE_NET2BT);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case StatusMessages.SRV_ONMESSAGE:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, String.format("handleMessage SRV_ONMESSAGE %s", ((WebSocketFrame) msg.obj).getTextPayload()));
                        break;
                    case StatusMessages.SRV_ONCLOSE:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONCLOSE");
                        break;
                    case StatusMessages.SRV_ONOPEN:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONOPEN");
                        if (Settings.testSendOneOnCall) {
                            new SendMessageToServerTask(DuplexActivity.this, iClientCtrl).execute("FUCK YOU ASSHOLE");
                        } else {
                            callIn();
                        }
                        break;
                    case StatusMessages.SRV_ONEXCEPTION:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONOPEN");
                        break;
                    case StatusMessages.CLT_ONFAILURE:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage SRV_ONFAILURE");
                        Toast.makeText(activity, "SUBSCRIBER NOT ONLINE", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        if (Settings.debug) Log.i(Tags.ACT_DPL, "handleMessage DEFAULT");
                        break;
                }
            }
        };

        btnCall = findViewById(R.id.btnCallOut);
        editTextSrvLocPort = findViewById(R.id.editTextSrvLocPort);
        editTextSrvLocAddr = findViewById(R.id.editTextSrvLocAddr);
        editTextSrvRemPort = findViewById(R.id.editTextSrvRemPort);
        editTextSrvRemAddr = findViewById(R.id.editTextSrvRemAddr);

        btnCall.setEnabled(false);
        editTextSrvLocPort.setText(String.format("%d", Settings.serverLocalPortNumber));
        editTextSrvLocAddr.setText(getIPAddress(Settings.ipv4));
        editTextSrvLocAddr.setFocusable(false);
        editTextSrvRemPort.setText(String.format("%d", Settings.serverRemotePortNumber));
        editTextSrvRemAddr.setText(Settings.serverRemoteIpAddress);

        new ServerOnTask(this, handler).execute(editTextSrvLocPort.getText().toString());

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableTransmitData();
                callOut();
            }
        });
    }

    private void callIn() {
    }

    private void enableTransmitData() {
    }

    private void callOut() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "call");
        new OpenWebSocketTask(DuplexActivity.this, handler).execute(String.format("ws://%s:%s",
                editTextSrvRemAddr.getText().toString(),
                editTextSrvRemPort.getText().toString()));
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "call iClientCtrl is null");
            return;
        }
        new StreamTask(DuplexActivity.this, iClientCtrl, Settings.dataSource, storageBtToNet).execute();
        new RedirectDataTask(DuplexActivity.this, iServerCtrl, Settings.dataSource, storageNetToBt).execute();
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverStarted");
        btnCall.setEnabled(true);
        this.iServerCtrl = iServerCtrl;
    }

    @Override
    public void serverCantStart() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverCantStart");
        final DuplexActivity activity = this;
        btnCall.setEnabled(false);
        Toast.makeText(activity, "CANT START SERVER", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setRedirect(IRedirectCtrl iRedirectCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "setRedirect");
        this.iRedirectCtrl = iRedirectCtrl;
    }

    @Override
    public void setStream(IStream iStream) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "setStream");
        this.iStream = iStream;
    }

    @Override
    public void clientStarted(IClientCtrl iClientCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "clientStarted");
        this.iClientCtrl = iClientCtrl;
    }

    @Override
    public void messageSended() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "messageSended");
    }
}