package by.citech.websocketduplex;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import by.citech.websocketduplex.client.asynctask.DisconnectTask;
import by.citech.websocketduplex.client.asynctask.OpenWebSocketTask;
import by.citech.websocketduplex.client.asynctask.SendMessageToServerTask;
import by.citech.websocketduplex.client.asynctask.StreamTask;
import by.citech.websocketduplex.client.network.IClientOff;
import by.citech.websocketduplex.client.network.IClientOn;
import by.citech.websocketduplex.client.network.IMessage;
import by.citech.websocketduplex.client.network.IStream;
import by.citech.websocketduplex.client.network.IStreamOn;
import by.citech.websocketduplex.client.network.IClientCtrl;
import by.citech.websocketduplex.param.Settings;
import by.citech.websocketduplex.param.StatusMessages;
import by.citech.websocketduplex.param.Tags;
import okio.ByteString;

public class ClientActivity extends Activity implements IClientOn, IClientOff, IStreamOn, IMessage {
    private static final int RECORD_AUDIO_REQUEST_CODE = 1;

    private Handler handler;
    private boolean isPermittedAudioRecord;
    public IClientCtrl iClientCtrl;
    public IStream iStream;

    public TextView textViewCltStatus;
    public TextView textViewCltFromSrvText;
    public TextView textViewCltFromSrvByte;

    public EditText editTextCltBuffSize;
    public EditText editTextCltToSrvText;
    public EditText editTextCltRemSrvAddr;
    public EditText editTextCltRemSrvPort;

    public Button btnCltConnToSrv;
    public Button btnCltDiscFromSrv;
    public Button btnCltStreamOff;
    public Button btnCltStreamOn;
    public Button btnCltSendMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Tags.ACT_CLT, "onCreate");
        setContentView(R.layout.activity_client);
        final ClientActivity activity = this;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case StatusMessages.CLT_ONMESSAGE_TEXT:
                        textViewCltFromSrvText.setText((String) msg.obj);
                        break;
                    case StatusMessages.CLT_ONMESSAGE_BYTES:
                        textViewCltFromSrvByte.setText("Received " + String.format("%d", (((ByteString) msg.obj).size())) + " bytes");
                        Toast.makeText(activity, "BYTES", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusMessages.CLT_ONCLOSED:
                        turnOffStream();
                        textViewCltStatus.setText("Состояние: сокет закрыт.");
                        Toast.makeText(activity, "CLOSED", Toast.LENGTH_SHORT).show();
                        editTextCltToSrvText.setVisibility(View.INVISIBLE);
                        btnCltConnToSrv.setEnabled(true);
                        btnCltStreamOn.setEnabled(false);
                        btnCltStreamOff.setEnabled(false);
                        btnCltDiscFromSrv.setEnabled(false);
                        btnCltSendMsg.setEnabled(false);
                        break;
                    case StatusMessages.CLT_ONOPEN:
                        textViewCltStatus.setText("Состояние: сокет открыт.");
                        editTextCltToSrvText.setVisibility(View.VISIBLE);
                        activity.editTextCltToSrvText.setText("РОИССЯ ВПЕРДЕ");
                        activity.btnCltDiscFromSrv.setEnabled(true);
                        activity.btnCltSendMsg.setEnabled(true);
                        activity.btnCltStreamOn.setEnabled(true);
                        Toast.makeText(activity, "OPENED", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusMessages.CLT_ONFAILURE:
                        turnOffStream();
                        textViewCltStatus.setText("Состояние: ошибка подключения.");
                        Toast.makeText(activity, "FAILURE", Toast.LENGTH_SHORT).show();
                        editTextCltToSrvText.setVisibility(View.INVISIBLE);
                        btnCltStreamOn.setEnabled(false);
                        btnCltStreamOff.setEnabled(false);
                        btnCltDiscFromSrv.setEnabled(false);
                        btnCltSendMsg.setEnabled(false);
                        btnCltConnToSrv.setEnabled(true);
                        break;
                    case StatusMessages.CLT_ONCLOSING:
                        turnOffStream();
                        Toast.makeText(activity, "CLOSING", Toast.LENGTH_SHORT).show();
                        editTextCltToSrvText.setVisibility(View.INVISIBLE);
                        btnCltConnToSrv.setEnabled(false);
                        btnCltStreamOn.setEnabled(false);
                        btnCltStreamOff.setEnabled(false);
                        btnCltDiscFromSrv.setEnabled(false);
                        btnCltSendMsg.setEnabled(false);
                        break;
                    default:
                        Toast.makeText(activity, "UNKNOWN", Toast.LENGTH_SHORT).show();
                }
            }
        };

        btnCltConnToSrv = findViewById(R.id.btnCltConnToSrv);
        btnCltDiscFromSrv = findViewById(R.id.btnCltDiscFromSrv);
        btnCltSendMsg = findViewById(R.id.btnCltSendMsg);
        btnCltStreamOn = findViewById(R.id.btnCltStreamOn);
        btnCltStreamOff = findViewById(R.id.btnCltStreamOff);

        editTextCltToSrvText = findViewById(R.id.editTextCltToSrvText);
        editTextCltRemSrvAddr = findViewById(R.id.editTextCltRemSrvAddr);
        editTextCltRemSrvPort = findViewById(R.id.editTextCltRemSrvPort);
        editTextCltBuffSize = findViewById(R.id.editTextCltBuffSize);

        textViewCltStatus = findViewById(R.id.textViewCltStatus);
        textViewCltFromSrvText = findViewById(R.id.textViewCltFromSrvText);
        textViewCltFromSrvByte = findViewById(R.id.textViewCltFromSrvByte);

        editTextCltToSrvText.setVisibility(View.INVISIBLE);
        editTextCltBuffSize.setText(String.format("%d", Settings.bufferSize));
        editTextCltRemSrvAddr.setText(Settings.serverRemoteIpAddress);
        editTextCltRemSrvPort.setText(String.format("%d", Settings.serverRemotePortNumber));

        btnCltDiscFromSrv.setEnabled(false);
        btnCltSendMsg.setEnabled(false);
        btnCltStreamOn.setEnabled(false);
        btnCltStreamOff.setEnabled(false);

        textViewCltStatus.setText("Состояние: ожидание.");
    }

    public void disconnect(View view) {
        Log.i(Tags.ACT_CLT, "disconnect");
        btnCltDiscFromSrv.setEnabled(false);
        new DisconnectTask(this).execute(iClientCtrl);
    }

    public void streamOn(View view) {
        Log.i(Tags.ACT_CLT, "streamOn");
        requestRecordAudioPermission();

        /*-------------------------- TEST --------------------------->>
        byte[] bytes = {0x2c, 0x56, 0x78, 0x7b};
        byte[] bytes = {127, 0x56, 0x78, -128};
        byte[] bytes = {(byte) 0x9a, (byte) 0x56, (byte) 0x78, (byte) 0xff};
        Log.i(Tags.ACT_CLT, bytesToHex(bytes));
        <<-------------------------- TEST ---------------------------*/

        if (isPermittedAudioRecord) {
            iStream = null;
            btnCltStreamOn.setEnabled(false);
            btnCltDiscFromSrv.setEnabled(false);
            btnCltSendMsg.setEnabled(false);
            new StreamTask(this, iClientCtrl, Settings.dataSource).execute(editTextCltBuffSize.getText().toString());
            btnCltStreamOff.setEnabled(true);
        }
    }

    public void streamOff(View view) {
        Log.i(Tags.ACT_CLT, "streamOff");
        btnCltStreamOff.setEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(Tags.ACT_CLT, "streamOff run in new thread");
                iStream.streamOff();
                Log.i(Tags.ACT_CLT, "streamOff run done in new thread");
                iStream = null;
            }
        }).start();

        btnCltStreamOn.setEnabled(true);
        btnCltDiscFromSrv.setEnabled(true);
        btnCltSendMsg.setEnabled(true);
    }

    public void connect(View view) {
        Log.i(Tags.ACT_CLT, "connect");
        btnCltConnToSrv.setEnabled(false);
        new OpenWebSocketTask(this, handler).execute(String.format("ws://%s:%s",
                editTextCltRemSrvAddr.getText().toString(),
                editTextCltRemSrvPort.getText().toString()));
    }

    public void sendMessage(View view) {
        Log.i(Tags.ACT_CLT, "sendMessage");
        btnCltSendMsg.setEnabled(false);
        new SendMessageToServerTask(this, iClientCtrl).execute(editTextCltToSrvText.getText().toString());
    }

    private void requestRecordAudioPermission() {
        Log.i(Tags.ACT_CLT, "requestRecordAudioPermission");

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
            Log.i(Tags.ACT_CLT, "requestRecordAudioPermission need runtime-permission");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.i(Tags.ACT_CLT, "requestRecordAudioPermission permission is not pre-granted, request for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
                return;
            }
        }

        Log.i(Tags.ACT_CLT, "requestRecordAudioPermission runtime-permission is pre-granted");
        isPermittedAudioRecord = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermittedAudioRecord = true;
                    textViewCltStatus.setText("Состояние: аудиозапись разрешена.");
                    Log.i(Tags.ACT_CLT, "onRequestPermissionsResult permission granted");
                } else {
                    isPermittedAudioRecord = false;
                    textViewCltStatus.setText("Состояние: аудиозапись запрещена.");
                    Log.i(Tags.ACT_CLT, "onRequestPermissionsResult permission denied");
                    finish();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(Tags.ACT_CLT, "onStop");
        turnOffStream();
        turnOffSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(Tags.ACT_CLT, "onDestroy");
        turnOffStream();
        turnOffSocket();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(Tags.ACT_CLT, "onPause");
        turnOffStream();
        turnOffSocket();
    }

    private void turnOffStream() {
        Log.i(Tags.ACT_CLT, "turnOffStream");

        if (iStream != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(Tags.ACT_CLT, "streamOff run in new thread");
                    iStream.streamOff();
                    Log.i(Tags.ACT_CLT, "streamOff run done in new thread");
                    iStream = null;
                }
            }).start();
        }
    }

    private void turnOffSocket() {
        Log.i(Tags.ACT_CLT, "turnOffSocket");
        if (iClientCtrl != null) {
            new DisconnectTask(this).execute(iClientCtrl);
        }
    }

    @Override
    public void setStream(IStream iStream) {
        this.iStream = iStream;
    }

    @Override
    public void clientStopped(String reason) {
        textViewCltStatus.setText("Состояние: " + reason);
        editTextCltToSrvText.setVisibility(View.INVISIBLE);
        btnCltSendMsg.setEnabled(false);
        btnCltStreamOn.setEnabled(false);
        btnCltStreamOff.setEnabled(false);
        btnCltConnToSrv.setEnabled(true);
        btnCltConnToSrv.setEnabled(true);
    }

    @Override
    public void clientStarted(IClientCtrl iClientCtrl) {
        this.iClientCtrl = iClientCtrl;
    }

    @Override
    public void messageSended() {
        editTextCltToSrvText.setText("");
        btnCltSendMsg.setEnabled(true);
        textViewCltStatus.setText("Состояние: сообщение отправлено");
    }
}
