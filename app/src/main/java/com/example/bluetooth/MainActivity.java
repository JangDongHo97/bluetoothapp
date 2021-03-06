package com.example.bluetooth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    TextView mTvBluetoothStatus;
    TextView mTvdomain1;
    TextView mTvdomain2;
    Button mBtnBluetoothOn;
    Button mBtnBluetoothOff;
    Button mBtnScan;
    ListView listView;

    Set<BluetoothDevice> mPairedDevices;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private BluetoothAdapter mBluetoothAdapter;


    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;

    private final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvBluetoothStatus = (TextView) findViewById(R.id.tvBluetoothStatus);
        mBtnBluetoothOn = (Button) findViewById(R.id.btnBluetoothOn);
        mBtnBluetoothOff = (Button) findViewById(R.id.btnBluetoothOff);
        mBtnScan = (Button) findViewById(R.id.btnScan);
        mTvdomain1 = (TextView) findViewById(R.id.domain1);
        mTvdomain2 = (TextView) findViewById(R.id.domain2);
        listView = (ListView) findViewById(R.id.listview);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // ???????????? ???????????? ????????? ???????????? ??????

        mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });

        mBtnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });

        mBtnScan.setOnClickListener(new Button.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                scan(listView);
            }
        });


        //????????? ?????? ????????????
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        if(mBluetoothAdapter.isEnabled()) {
            mTvBluetoothStatus.setText("?????????");
        }

        // show paired devices
        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();
        listView.setAdapter(btArrayAdapter);
    }

    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "??????????????? ?????? ????????? ?????? ????????????.", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("?????????");
            } else {
                Toast.makeText(getApplicationContext(), "??????????????? ????????? ?????? ?????? ????????????.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
                if(mBluetoothAdapter.isEnabled()) {
                    mTvBluetoothStatus.setText("?????????");
                }
            }
        }
    }

    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("????????????");
        } else {
            Toast.makeText(getApplicationContext(), "??????????????? ?????? ???????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("????????????");
        }
    }

    private void scan(View view) {
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Canceling discovery", Toast.LENGTH_SHORT).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Starting discovery", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.startDiscovery();
                if(btArrayAdapter != null && !btArrayAdapter.isEmpty()) {
                    btArrayAdapter.clear();
                }
                if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                    deviceAddressArray.clear();
                }
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
            } else {
                Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName != null) {
                    btArrayAdapter.add(deviceName);
                    deviceAddressArray.add(deviceHardwareAddress);
                    btArrayAdapter.notifyDataSetChanged();
                }


            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

//    public void viewList(){
//        btArrayAdapter.clear();
//        if(deviceAddressArray!=null && !deviceAddressArray.isEmpty()){ deviceAddressArray.clear(); }
//        mPairedDevices = mBluetoothAdapter.getBondedDevices();
//        if (mPairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : mPairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                btArrayAdapter.add(deviceName);
//                deviceAddressArray.add(deviceHardwareAddress);
//            }
//        }
//    }
}