package by.citech.websocketduplex;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import by.citech.R;

public class NetworkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
    }

    public void goSetServer(View view) {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

    public void goSetDuplex(View view) {
        Intent intent = new Intent(this, DuplexActivity.class);
        startActivity(intent);
    }

    public void goSetClient(View view) {
        Intent intent = new Intent(this, ClientActivity.class);
        startActivity(intent);
    }
}
