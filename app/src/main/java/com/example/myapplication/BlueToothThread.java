package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BlueToothThread extends Thread {

    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private   InputStream mmInStream;
    private   OutputStream mmOutStream;

    public BlueToothThread(BluetoothDevice bluetoothDevice ) {
        this.bluetoothDevice = bluetoothDevice;

    }

    @SuppressLint("MissingPermission")
    @Override
    public void run() {
        // 在这里编写线程要执行的任务

        try {

             bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

            Class<?> clazz = BluetoothDevice.class;
            Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
            Object[] params = new Object[]{Integer.valueOf(1)};
            bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothSocket.getRemoteDevice(), params);
            bluetoothSocket.connect();


            InputStream inputStream=   bluetoothSocket.getInputStream();

            String line="";
            byte[]   mmBuffer = new byte[1024];
            int numBytes=0;
            while(true){
                numBytes = inputStream.read(mmBuffer);
                Log.w("AAAAAAAAAA",new String (mmBuffer, StandardCharsets.UTF_8));
            }


        } catch (Exception e) {
            e.printStackTrace();

        }

    }



    public void wrireDate(String message)  {

        OutputStream os= null;
        try {

            os = bluetoothSocket.getOutputStream();
            if(os!=null) {
                os.write(message.getBytes(StandardCharsets.UTF_8));
                os.flush();
           //     os.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}