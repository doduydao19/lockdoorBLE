package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.data.LoginDataSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class InApp extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private final UUID BLUETOOTH_LE_CC254X_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final UUID BLUETOOTH_LE_CC254X_CHAR_RW = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic1;
    private BluetoothGattCharacteristic mCharacteristic2;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_LOCATION_PERMISSION = 1;
    Handler handler = new Handler(Looper.getMainLooper());
    private ScanCallback mScanCallback;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothDevice mBluetoothDevice;
    private TextView myEditText;
    private Button button2;
    private Button button3;
    private boolean mWrite1Finished = false;


    // GATT callback
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Connected to the device

                Log.i(TAG, "Connected to GATT server.");
                // Discover services
                gatt.discoverServices();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myEditText.setText("연결됨");
                        button2.setEnabled(false);
                        button3.setEnabled(false);
                    }
                }, 250);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from the device
                Log.i(TAG, "Disconnected from GATT server.");
                mBluetoothGatt.close();


            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                // Disconnected from the device
                Log.i(TAG, "Connecting to GATT server.");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myEditText.setText("연결중");
                        button2.setEnabled(false);
                        button3.setEnabled(false);
                    }
                }, 250);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Services discovered
                Log.i(TAG, "Services discovered: " + gatt.getServices());
                BluetoothGattService gattService = mBluetoothGatt.getService(BLUETOOTH_LE_CC254X_SERVICE);
                mCharacteristic1 = gattService.getCharacteristic(BLUETOOTH_LE_CC254X_CHAR_RW);
                mCharacteristic2 = gattService.getCharacteristic(BLUETOOTH_LE_CC254X_CHAR_RW);
                mBluetoothGatt.setCharacteristicNotification(mCharacteristic1, true);

            } else {
                Log.e(TAG, "Failed to discover services: " + status);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            super.onCharacteristicWrite(gatt, characteristic, status);
            // check if the write operation was successful
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // get the response here
                byte[] response = characteristic.getValue();
                String response_string = new String(response, StandardCharsets.UTF_8);
                Log.i(TAG, "Send = " + response_string);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Lấy giá trị mới từ characteristic
            byte[] value = characteristic.getValue();
            String newValue = new String(value, StandardCharsets.UTF_8);
            Log.i(TAG, "Received new value: " + newValue);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_in_app);

        // check the permission and grant for them
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        requestPermission();


        myEditText = findViewById(R.id.textView4);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // click handling code
                deactivteAction();
                try {
                    String username = getIntent().getStringExtra("Username");
                    String url = "https://local.estech777.com/v1/guest/local/ble-access-key";
                    class PostRequestApi extends AsyncTask<Void, Void, Boolean> {
                        private LoginDataSource t;

                        public PostRequestApi(LoginDataSource t) {
                            this.t = t;
                        }

                        @SuppressLint("WrongThread")
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            t.sendGet(url, username);
                            return true;
                        }
                    }
                    LoginDataSource t = new LoginDataSource();
                    PostRequestApi postRequestApi = new PostRequestApi(t);
                    postRequestApi.execute();
                    postRequestApi.get();

                    if (!t.keyAccessBLE.equals("")) {
                        String roomNumber = getIntent().getStringExtra("RoomNumber"); // just RoomNumber
                        String lockDoorDevice = "EstBM(" + roomNumber + ")";
                        callAction(lockDoorDevice, t.keyAccessBLE);
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // click handling code
                deactivteAction();
                try {
                    String username = getIntent().getStringExtra("Username");
                    String url = "https://local.estech777.com/v1/guest/local/elevator-access-key";
                    @SuppressLint("StaticFieldLeak")
                    class PostRequestApi extends AsyncTask<Void, Void, Boolean> {
                        private LoginDataSource t;
                        public PostRequestApi(LoginDataSource t) {
                            this.t = t;
                        }
                        @SuppressLint("WrongThread")
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            t.sendGet(url, username);
                            return true;
                        }
                    }
                    LoginDataSource t = new LoginDataSource();
                    PostRequestApi postRequestApi = new PostRequestApi(t);
                    postRequestApi.execute();
                    postRequestApi.get();

                    if (!t.keyAccessBLE.equals("")) {
                        String elevatorDevice = "EstEV";
                        callAction(elevatorDevice, t.keyAccessBLE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

    }


    @SuppressLint("MissingPermission")
    public void callAction(String nameDevice, String code) {
        // BLE connection:
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Initialize ScanCallback to process scan results
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                String deviceAddress = result.getDevice().getAddress();
                int rssi = result.getRssi();
                String scanResult = result.toString();
//                Log.i(TAG, "Device: " + scanResult);
                if (scanResult.contains(nameDevice) && rssi > -65) {
                    Log.i(TAG, "Device name: " + nameDevice);
                    Log.i(TAG, "Device address: " + deviceAddress);
                    Log.i(TAG, "Signal strength (RSSI): " + rssi);

                    if (connectToDevice(deviceAddress)) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopScan();
                                Log.i(TAG, "Successful connection");
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.i(TAG, "Send the key: " + code);
                                        transferKey(code);
                                    }
                                }, 2500);
                            }
                        }, 500);
                    } else {
                        Log.i(TAG, "Connection failed");
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e("ScanError", "Scan failed with error code " + errorCode);
            }
        };

        startScan(nameDevice);
    }

    // Connect to a specific device by its MAC address
    @SuppressLint("MissingPermission")
    public boolean connectToDevice(String deviceAddress) {
        // Get the remote device by its MAC address
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        // Check if the device is available
        if (mBluetoothDevice == null) {
            Log.e(TAG, "Device not found. Unable to connect.");
            return false;
        }
        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        return mBluetoothGatt != null;

    }

    public InApp getActivity() {
        return this;
    }


    @SuppressLint("MissingPermission")
    private void transferKey(String code) {
        String callAction_string = "Act:5282";
        mCharacteristic1.setValue(callAction_string);
        mBluetoothGatt.writeCharacteristic(mCharacteristic1);

        try {
            Thread.sleep(500);
            System.out.println("Wait to send signal number 2");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        handler.postDelayed(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                byte[] data = code.getBytes(StandardCharsets.UTF_8);
                int maxLength = 20;
                int offset = 0;
                while (offset < data.length) {
                    int length = Math.min(data.length - offset, maxLength);
                    byte[] packet = Arrays.copyOfRange(data, offset, offset + length);
                    if (packet.length != 0) {
                        mCharacteristic2.setValue(packet);
                        mBluetoothGatt.writeCharacteristic(mCharacteristic2);
                        offset += length;
                        try {
                            Thread.sleep(100); // đợi 100ms trước khi gửi gói dữ liệu tiếp theo
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return;
                    }
                }
            }
        }, 1000);

        try {
            Thread.sleep(500);
            System.out.println("Disconnecting...");
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            mCharacteristic1= null;

            myEditText.setText("연결안됨");
            button2.setEnabled(true);
            button3.setEnabled(true);
            button2.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.white));
            button2.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.black));
            button3.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.white));
            button3.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.black));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("MissingPermission")
    private void startScan(String nameOfDevice) {

        Log.i(TAG,"Scanning...");
        // params for BLE searching
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setDeviceName(nameOfDevice)
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(scanFilter);

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activteAction();
                    }
                }, 250);
            }
        }, 5000);
    }


    @SuppressLint("MissingPermission")
    private void stopScan() {
        Log.i(TAG,"Stop scan...");
        mBluetoothLeScanner.stopScan(mScanCallback);
    }

    private void activteAction() {
        myEditText.setText("연결안됨");
        button2.setEnabled(true);
        button3.setEnabled(true);
        button2.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.white));
        button2.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.black));
        button3.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.white));
        button3.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.black));
    }

    private void deactivteAction() {
        button2.setEnabled(false);
        button3.setEnabled(false);
        button2.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.black));
        button2.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.white));
        button3.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.black));
        button3.setTextColor(ContextCompat.getColorStateList(getActivity(), R.color.white));
    }

    private void requestPermission(){
        // Get BluetoothAdapter
        if (mBluetoothAdapter == null) {
            // Devices that do not support Bluetooth
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is off, requires user to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_CODE);
            }

            if(Build.VERSION_CODES.M <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN},
                            PERMISSION_REQUEST_CODE);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Bluetooth is activated");
            } else {
                Log.i(TAG, "Bluetooth is not turned on");
            }
        }
    }
}