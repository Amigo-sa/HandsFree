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

import by.citech.client.asynctask.TaskConnect;
import by.citech.connection.IStreamCtrl;
import by.citech.connection.TaskStream;
import by.citech.client.network.IClientCtrl;
import by.citech.client.network.IClientCtrlRegister;
import by.citech.connection.IMessage;
import by.citech.connection.IStreamCtrlRegister;
import by.citech.connection.IReceiverListenerRegister;
import by.citech.connection.ITransmitter;
import by.citech.data.StorageData;
import by.citech.param.Settings;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;
import by.citech.connection.TaskRedirect;
import by.citech.server.asynctask.TaskServerOn;
import by.citech.connection.IRedirectCtrl;
import by.citech.connection.IRedirectCtrlRegister;
import by.citech.server.network.IServerCtrlRegister;
import by.citech.server.network.IServerCtrl;
import by.citech.server.network.websockets.WebSocketFrame;
import static by.citech.util.NetworkInfo.getIPAddress;

public class DuplexActivity extends Activity implements IServerCtrlRegister, IRedirectCtrlRegister, IStreamCtrlRegister, IClientCtrlRegister, IMessage {
    private EditText editTextSrvLocPortDpl;
    private EditText editTextSrvLocAddrDpl;
    private EditText editTextSrvRemPortDpl;
    private EditText editTextSrvRemAddrDpl;
    private Button btnCallOutDpl;
    private Button btnCallInDpl;
    private StorageData storageBtToNet;
    private StorageData storageNetToBt;
    private Handler handler;
    private IServerCtrl iServerCtrl;
    private IClientCtrl iClientCtrl;
    private IRedirectCtrl iRedirectCtrl;
    private IStreamCtrl iStreamCtrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplex);
        final DuplexActivity activity = this;
        storageBtToNet = new StorageData(Tags.NET_STORE_BT2NET);
        storageNetToBt = new StorageData(Tags.NET_STORE_NET2BT);

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
                            new TaskConnect(DuplexActivity.this, handler).execute(String.format("ws://%s:%s",
                                    editTextSrvRemAddrDpl.getText().toString(),
                                    editTextSrvRemPortDpl.getText().toString()));
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

        btnCallOutDpl = findViewById(R.id.btnCallOutDpl);
        btnCallInDpl = findViewById(R.id.btnCallInDpl);
        editTextSrvLocPortDpl = findViewById(R.id.editTextSrvLocPortDpl);
        editTextSrvLocAddrDpl = findViewById(R.id.editTextSrvLocAddrDpl);
        editTextSrvRemPortDpl = findViewById(R.id.editTextSrvRemPortDpl);
        editTextSrvRemAddrDpl = findViewById(R.id.editTextSrvRemAddrDpl);

        btnCallOutDpl.setEnabled(false);
        btnCallInDpl.setEnabled(false);
        editTextSrvLocAddrDpl.setText(getIPAddress(Settings.ipv4));
        editTextSrvLocAddrDpl.setFocusable(false);
        editTextSrvLocPortDpl.setText(String.format("%d", Settings.serverLocalPortNumber));
        editTextSrvRemPortDpl.setText(String.format("%d", Settings.serverRemotePortNumber));
        editTextSrvRemAddrDpl.setText(Settings.serverRemoteIpAddress);

        new TaskServerOn(this, handler).execute(editTextSrvLocPortDpl.getText().toString());

        btnCallOutDpl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableTransmitData();
                callOut();
            }
        });
        btnCallInDpl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableTransmitData();
                callIn();
            }
        });
    }

    private void callIn() {
    }

    private void enableTransmitData() {
    }

    private void callOut() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "call");
        new TaskConnect(DuplexActivity.this, handler).execute(String.format("ws://%s:%s",
                editTextSrvRemAddrDpl.getText().toString(),
                editTextSrvRemPortDpl.getText().toString()));
        if (iClientCtrl == null) {
            if (Settings.debug) Log.i(Tags.ACT_DPL, "call iClientCtrl is null");
            return;
        }
        new TaskStream(DuplexActivity.this, (ITransmitter) iClientCtrl, Settings.dataSource, storageBtToNet).execute();
        new TaskRedirect(DuplexActivity.this, (IReceiverListenerRegister) iServerCtrl, Settings.dataSource, storageNetToBt).execute();
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "serverStarted");
        btnCallOutDpl.setEnabled(true);
        if (iServerCtrl == null) {
            btnCallOutDpl.setEnabled(false);
            Toast.makeText(this, "CANT START SERVER", Toast.LENGTH_SHORT).show();
        } else {
            this.iServerCtrl = iServerCtrl;
        }
    }

    @Override
    public void registerRedirectCtrl(IRedirectCtrl iRedirectCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "registerRedirectCtrl");
        this.iRedirectCtrl = iRedirectCtrl;
    }

    @Override
    public void registerStreamCtrl(IStreamCtrl iStreamCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "registerStreamCtrl");
        this.iStreamCtrl = iStreamCtrl;
    }

    @Override
    public void registerClientCtrl(IClientCtrl iClientCtrl) {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "registerClientCtrl");
        this.iClientCtrl = iClientCtrl;
    }

    @Override
    public void messageSended() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "messageSended");
    }

    @Override
    public void messageCantSend() {
        if (Settings.debug) Log.i(Tags.ACT_DPL, "messageCantSend");
    }
}
