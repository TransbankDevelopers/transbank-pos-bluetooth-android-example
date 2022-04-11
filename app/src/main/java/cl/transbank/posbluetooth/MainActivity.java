package cl.transbank.posbluetooth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ingenico.pclservice.PclService;
import com.ingenico.pclutilities.PclUtilities;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends CommonActivity {

    private static final int connectingColor = Color.rgb(255, 195, 0);
    private String selectedDevice;
    private Boolean isConnected = false;
    private EditText editTextAmount, editTextOperationId;
    private TextView textViewStatus;
    private Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPclUtil = new PclUtilities(this, getPackageName(), "device.txt");

        textViewStatus = findViewById(R.id.textViewStatus);
        btnConnect = findViewById(R.id.btnConnect);
        //editTextAmount = findViewById(R.id.etxt_amount);
        //editTextOperationId = findViewById(R.id.etxt_operationID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume Main Activity");

        ArrayList<PosBluetooth> btDevices = this.getDevices();

        if (btDevices != null)
        {
            if (btDevices.size() > 0) {
                // Loop through paired devices
                for (PosBluetooth device : btDevices) {
                    Log.d(TAG, device.getAddress() + " - " + device.getName());

                    if (device.isActivated() && isConnected) {
                        selectedDevice = device.getAddress();
                        connectDevice(selectedDevice);
                        return;
                    }
                }

                selectedDevice = btDevices.get(0).getAddress();
            }
        }

        if (btDevices.size() == 0)
            this.makeToast(R.string.no_paired_device);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause Main Activity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy Main Activity");
    }

    @Override
    void onStateChanged(String state) {
        if(state.equals("CONNECTED")) {
            textViewStatus.setText(R.string.connected);
            textViewStatus.setTextColor(Color.GREEN);
            btnConnect.setText(R.string.btn_disconnect);
            isConnected = true;
            return;
        }

        textViewStatus.setText(R.string.disconnected);
        textViewStatus.setTextColor(Color.RED);
        btnConnect.setText(R.string.btn_connect);
        isConnected = false;
    }

    @Override
    void onPclServiceConnected() {

    }

    private void startPclService() {
        if (!mServiceStarted) {
            SharedPreferences settings = this.getSharedPreferences("PCLSERVICE", Context.MODE_PRIVATE);
            boolean enableLog = settings.getBoolean("ENABLE_LOG", true);
            Intent i = new Intent(this, PclService.class);
            i.putExtra("PACKAGE_NAME", getPackageName());
            i.putExtra("FILE_NAME", "device.txt");
            i.putExtra("ENABLE_LOG", enableLog);

            if (this.getApplicationContext().startService(i) != null)
                mServiceStarted = true;
        }
    }

    private void stopPclService() {
        if (mServiceStarted) {
            Intent i = new Intent(this, PclService.class);
            if (this.getApplicationContext().stopService(i))
                mServiceStarted = false;
        }
    }

    private void connectDevice(String deviceAddress) {
        textViewStatus.setText(R.string.connecting);
        textViewStatus.setTextColor(connectingColor);
        mPclUtil.ActivateCompanion(deviceAddress);
        startPclService();
        initService();
    }

    private void disconnectDevice() {
        releaseService();
        stopPclService();
    }

    private ArrayList<PosBluetooth> getDevices() {
        Set<PclUtilities.BluetoothCompanion> btCompanions = mPclUtil.GetPairedCompanions();
        ArrayList<PosBluetooth> btDevices = new ArrayList<>();

        for (PclUtilities.BluetoothCompanion comp : btCompanions) btDevices.add(new PosBluetooth(comp));

        return btDevices;
    }

    public void toggleConnection(View view) {
        if(!isConnected){
            connectDevice(selectedDevice);
            return;
        }

        disconnectDevice();
    }

    private void makeToast(String text) {
        MainActivity activity = this;
        activity.runOnUiThread(() -> Toast.makeText(activity, text, Toast.LENGTH_SHORT).show());
    }

    private void makeToast(int resId) {
        MainActivity activity = this;
        activity.runOnUiThread(() -> Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show());
    }
}