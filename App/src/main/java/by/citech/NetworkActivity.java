package by.citech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import by.citech.ClientActivity;
import by.citech.R;

public class NetworkActivity extends Activity {
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