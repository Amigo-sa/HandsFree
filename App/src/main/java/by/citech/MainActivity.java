package by.citech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import by.citech.bluetoothlegatt.DeviceScanActivity;
import by.citech.websocketduplex.NetworkActivity;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    Button btnBLE;
    Button btnNet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBLE = (Button) findViewById(R.id.btnBLE);
        btnNet = (Button) findViewById(R.id.btnNetwork);

    }

    public void btnBLE(View view) {
        final Intent intent = new Intent(this, DeviceScanActivity.class);
        startActivity(intent);

    }

    public void btnNetwork(View view) {
        final Intent intent = new Intent(this, NetworkActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //textState.setText(App.getInstance().getState().getName());
    }
}
