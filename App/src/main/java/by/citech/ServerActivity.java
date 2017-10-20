package by.citech;

import android.app.Activity;
import android.media.AudioAttributes;
import android.os.Handler;
import android.os.Message;
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

import by.citech.R;
import by.citech.connection.IReceiverRegister;
import by.citech.param.Settings;
import by.citech.server.asynctask.RedirectDataTask;
import by.citech.server.asynctask.ServerOffTask;
import by.citech.server.asynctask.ServerOnTask;
import by.citech.server.network.IRedirectCtrl;
import by.citech.server.network.IServerOff;
import by.citech.server.network.IServerOn;
import by.citech.server.network.IServerCtrl;
import by.citech.server.network.IRedirectOn;
import by.citech.server.network.websockets.WebSocketFrame;
import by.citech.param.StatusMessages;
import by.citech.param.Tags;
import static by.citech.util.Decode.bytesToHex;
import static by.citech.util.NetworkInfo.getIPAddress;

public class ServerActivity extends Activity implements OnCheckedChangeListener, IServerOn, IServerOff, IRedirectOn {
    public IServerCtrl serverCtrl;
    public IRedirectCtrl iRedirectCtrl;
    private Handler handler;

    public ToggleButton btnSrvRedirectData;
    public ToggleButton btnSrvSpeakerOn;

    public Button btnSrvOn;
    public Button btnSrvOff;
    public Button btnSrvSendMsg;

    public EditText editTextSrvPort;
    public EditText editTextSrvIp;
    public EditText editTextSrvToCltText;
    public EditText editTextSrvBuffSize;

    public TextView textViewSrvStatus;
    public TextView textViewSrvFromCltByte;
    public TextView textViewSrvFromCltText;

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
//                      String textText = ((WebSocketFrame) msg.obj).getTextPayload();
//                      byte[] bytes = ((WebSocketFrame) msg.obj).getBinaryPayload();
//                      String textBytes = bytesToHex(bytes);
//                      textViewSrvFromCltText.setText(textText);
                        textViewSrvFromCltText.setText(((WebSocketFrame) msg.obj).getTextPayload());
                        textViewSrvFromCltByte.setText(bytesToHex(((WebSocketFrame) msg.obj).getBinaryPayload()));
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

        btnSrvOn = findViewById(R.id.btnSrvOn);
        btnSrvOff = findViewById(R.id.btnSrvOff);
        btnSrvRedirectData = findViewById(R.id.btnSrvRedirectData);
        btnSrvSpeakerOn = findViewById(R.id.btnSrvSpeakerOn);
        btnSrvSendMsg = findViewById(R.id.btnSrvSendMsg);

        editTextSrvPort = findViewById(R.id.editTextSrvPort);
        editTextSrvIp = findViewById(R.id.editTextSrvIp);
        editTextSrvToCltText =  findViewById(R.id.editTextSrvToCltText);
        editTextSrvBuffSize = findViewById(R.id.editTextSrvBuffSize);

        textViewSrvStatus = findViewById(R.id.textViewSrvStatus);
        textViewSrvFromCltByte = findViewById(R.id.textViewSrvFromCltByte);
        textViewSrvFromCltText = findViewById(R.id.textViewSrvFromCltText);

        editTextSrvToCltText.setVisibility(View.INVISIBLE);
        editTextSrvBuffSize.setText(String.format("%d", Settings.bufferSize));
        editTextSrvPort.setText(String.format("%d", Settings.serverLocalPortNumber));
        editTextSrvIp.setText(getIPAddress(Settings.ipv4));
        editTextSrvIp.setFocusable(false);

        btnSrvOff.setEnabled(false);
        btnSrvRedirectData.setOnCheckedChangeListener(this);
        btnSrvSpeakerOn.setOnCheckedChangeListener(this);
        btnSrvSendMsg.setEnabled(false);

        textViewSrvStatus.setText("Состояние: ожидание.");
    }

    public void offServer(View view) {
        Log.i(Tags.ACT_SRV, "offServer");
        try {
            new ServerOffTask(this, serverCtrl).execute();
        } catch (Exception e) {
            Log.i(Tags.ACT_SRV, "offServer serverCtrl is null");
        }
    }

    public void onServer(View view) {
        Log.i(Tags.ACT_SRV, "onServer");
        btnSrvOn.setEnabled(false);
        new ServerOnTask(this, handler).execute(editTextSrvPort.getText().toString());
    }

    public void sendMessage(View view) {
        Log.i(Tags.ACT_SRV, "sendMessage");
        serverCtrl.getTransmitter().sendMessage(editTextSrvToCltText.getText().toString());
        editTextSrvToCltText.setText("");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(Tags.ACT_SRV, "onCheckedChanged");

        switch (buttonView.getId()) {
            case (R.id.btnSrvRedirectData):
                if (isChecked) {
                    Log.i(Tags.ACT_SRV, "onCheckedChanged redirect on");
//                  Context context = getApplicationContext();
//                  AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//                  audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    new RedirectDataTask(this, (IReceiverRegister) serverCtrl, Settings.dataSource).execute(editTextSrvBuffSize.getText().toString());
                } else {
                    Log.i(Tags.ACT_SRV, "onCheckedChanged redirect off");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(Tags.ACT_CLT, "redirectOff run in new thread");
                            iRedirectCtrl.redirectOff();
                            Log.i(Tags.ACT_CLT, "redirectOff run done in new thread");
                            iRedirectCtrl = null;
                        }
                    }).start();
                }
            case (R.id.btnSrvSpeakerOn):
                if (isChecked) {
                    Log.i(Tags.ACT_SRV, String.format("AudioAttributes.USAGE-1 is %d", Settings.audioUsage));
                    //Settings.audioUsage = AudioAttributes.USAGE_MEDIA;
                    Settings.audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
                    Log.i(Tags.ACT_SRV, String.format("AudioAttributes.USAGE-2 is %d", Settings.audioUsage));
                    Log.i(Tags.ACT_SRV, "onCheckedChanged speaker on");
                } else {
                    Log.i(Tags.ACT_SRV, String.format("AudioAttributes.USAGE-1 is %d", Settings.audioUsage));
                    Settings.audioUsage = AudioAttributes.USAGE_VOICE_COMMUNICATION;
                    Log.i(Tags.ACT_SRV, String.format("AudioAttributes.USAGE-2 is %d", Settings.audioUsage));
                    Log.i(Tags.ACT_SRV, "onCheckedChanged speaker off");
                }
        }
    }

    @Override
    protected void onStop() {
        Log.i(Tags.ACT_SRV, "onStop");
        super.onStop();
        try {
            new ServerOffTask(this, serverCtrl).execute();
        } catch (Exception e) {
            Log.i(Tags.ACT_SRV, "onStop serverCtrl is null");
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(Tags.ACT_SRV, "onDestroy");
        super.onDestroy();
        try {
            new ServerOffTask(this, serverCtrl).execute();
        } catch (Exception e) {
            Log.i(Tags.ACT_SRV, "onDestroy serverCtrl is null");
        }
    }

    @Override
    protected void onPause() {
        Log.i(Tags.ACT_SRV, "onPause");
        super.onPause();
        try {
            new ServerOffTask(this, serverCtrl).execute();
        } catch (Exception e) {
            Log.i(Tags.ACT_SRV, "onPause serverCtrl is null");
        }
    }

    @Override
    public void serverStarted(IServerCtrl iServerCtrl) {
        textViewSrvStatus.setText("Состояние: сервер включен.");
        btnSrvOff.setEnabled(true);
    }

    @Override
    public void serverCantStart() {
        textViewSrvStatus.setText("Состояние: не удалось запустить сервер.");
        btnSrvOn.setEnabled(true);
    }

    @Override
    public void serverStopped() {
        btnSrvOff.setEnabled(false);
        btnSrvOn.setEnabled(true);
        textViewSrvStatus.setText("Состояние: сервер выключен.");
    }

    @Override
    public void setRedirect(IRedirectCtrl iRedirectCtrl) {
        this.iRedirectCtrl = iRedirectCtrl;
    }
}
