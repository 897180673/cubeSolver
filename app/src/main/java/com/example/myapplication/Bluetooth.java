package com.example.myapplication;

import static android.app.PendingIntent.getActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth);

        Button btn11 = findViewById(R.id.scanBlue);

        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        ArrayList<String> dataList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        ListView listView = findViewById(R.id.blueDevices);
        listView.setAdapter(adapter);

        final BlueToothThread[] blueToothThread = {null};

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 创建Dialog对象
                AlertDialog.Builder builder = new AlertDialog.Builder(Bluetooth.this);

                 // 2. Chain together various setter methods to set the dialog characteristics
                String  adderss = (String) adapterView.getItemAtPosition(i);
                builder.setMessage( "蓝牙名,地址信息:"+adderss)
                      .setTitle( "连接这个蓝牙?");

                 // 3. Get the AlertDialog from create()
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("qwerty","ok");
                        bluetoothAdapter.cancelDiscovery();

                        //开始连接蓝牙

                        BluetoothDevice bluetoothDevice= bluetoothAdapter.getRemoteDevice(adderss.split("->")[1]);


                        while (   bluetoothAdapter.isDiscovering()){
                            bluetoothAdapter.cancelDiscovery();
                        }

                         blueToothThread[0] = new BlueToothThread(bluetoothDevice);

                        blueToothThread[0].start();



                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("qwerty","cancle");
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                Log.i("qwerty",adderss);
            }
        });

        Button sendMessage=findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blueToothThread[0].wrireDate("hello bluetooth");
            }
        });



        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);

        }


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }


        btn11.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {

                Log.i("bluetooth123", "开始扫描");
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();

            }
        });


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


                    @SuppressLint("MissingPermission")
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if(!dataList.contains(deviceName+"->"+deviceHardwareAddress)){
                        if(deviceName!=null && BluetoothDevice.DEVICE_TYPE_CLASSIC== device.getType())
                            dataList.add(deviceName+"->"+deviceHardwareAddress);
                    }
                    Log.i("bluetooth123",deviceName+"->"+deviceHardwareAddress);

                    adapter.notifyDataSetChanged();

                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);




    }


}

