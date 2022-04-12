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

import posintegrado.ingenico.com.mposintegrado.mposLib;

public class MainActivity extends CommonActivity {

    private static final int connectingColor = Color.rgb(255, 195, 0);
    private String selectedDevice;
    private Boolean isConnected = false;
    private EditText editTextAmount, editTextOperationId;
    private TextView textViewStatus;
    private Button btnConnect;
    mposLib posLib;

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
        disconnectDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy Main Activity");
        disconnectDevice();
    }

    @Override
    void onStateChanged(String state) {
        if(state.equals("CONNECTED")) {
            textViewStatus.setText(R.string.connected);
            textViewStatus.setTextColor(Color.GREEN);
            btnConnect.setText(R.string.btn_disconnect);
            isConnected = true;

            posLib = new mposLib(mPclService);
            posLib.setOnTransactionFinishedListener(response -> {
                Log.i(TAG, posLib.convertHexToString(response));
            });
            return;
        }

        textViewStatus.setText(R.string.disconnected);
        textViewStatus.setTextColor(Color.RED);
        btnConnect.setText(R.string.btn_connect);
        isConnected = false;
        posLib.setOnTransactionFinishedListener(null);
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

    private byte LRC(String command) {
        byte lrc = 0;
        byte[] hexCommand = posLib.hexStringToByteArray(command);

        for(int i = 1; i < hexCommand.length; i++) {
            lrc ^= hexCommand[i];
        }

        return lrc;
    }

    private void sendToPOS(String command) {
        final String STX = "02";
        final String ETX = "03";

        String hexCommand = STX + posLib.convertStringToHex(command) + ETX;
        byte lrc = LRC(hexCommand);
        String fullCommand = hexCommand + String.format("%02X",lrc);
        Log.i(TAG, posLib.convertHexToString(fullCommand));
        posLib.startTransaction(fullCommand);
    }

    public void loadKeys(View view) {
        try {
            Log.i(TAG, "Carga de llaves");
            String command = "0800";
            sendToPOS(command);
        }
        catch(Exception e) {
            Log.e(TAG, e.toString());
            makeToast("Error al realizar la carga de llaves.");
        }
    }

    public void lastSale(View view) {
        try {
            Log.i(TAG, "Última venta");
            String command = "0250|";
            sendToPOS(command);
        }
        catch(Exception e) {
            Log.e(TAG, e.toString());
            makeToast("Error al obtener la última venta.");
        }
    }

    public void totals(View view) {
        try {
            Log.i(TAG, "Total de ventas");
            String command = "0700||";
            sendToPOS(command);
        }
        catch(Exception e) {
            Log.e(TAG, e.toString());
            makeToast("Error al obtener el total de ventas.");
        }
    }

    public void close(View view) {
        try {
            Log.i(TAG, "Cierre de POS");
            String command = "0500||";
            sendToPOS(command);
        }
        catch(Exception e) {
            Log.e(TAG, e.toString());
            makeToast("Error al realizar el cierre del POS.");
        }
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