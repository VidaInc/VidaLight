package com.aprbrother.ablight;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.UUID;
import com.aprilbrother.aprilbrothersdk.Beacon;
/**
 * Created by btkfrank on 15-06-07.
 */
public class RssiAutoControl extends Activity{
    private Beacon beacon;
    private BluetoothDevice myDevice;
    private BluetoothGatt mBluetoothGatt;
    public final static UUID BEACONSERVICEUUID = UUID
            .fromString("0000fab0-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONPROXIMITYUUID = UUID
            .fromString("0000fab1-0000-1000-8000-00805f9b34fb");
    public final static UUID BEACONMAJORUUID = UUID
            .fromString("0000fab2-0000-1000-8000-00805f9b34fb");
    private static final String TAG = "DetailActivity";

    public static final UUID CCCD = UUID
            .fromString("00002901-0000-1000-8000-00805f9b34fb");

    private long lastTime;

    private void init() {
        beacon = BeaconList.getBeaconByMacAdd("D0:FF:50:67:7C:4A");
        myDevice = deviceFromBeacon(beacon);
        mBluetoothGatt = myDevice.connectGatt(this, false, myGattCallback);
        write(100);

    }

    @SuppressLint("NewApi")
    private BluetoothDevice deviceFromBeacon(Beacon beacon) {
        BluetoothManager bluetoothManager = (BluetoothManager) this
                .getSystemService("bluetooth");
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        return bluetoothAdapter.getRemoteDevice(beacon.getMacAddress());
    }

    BluetoothGattService RxService;
    BluetoothGattCharacteristic RxChar;

    private void write(int i) {
        byte[] value = new byte[6];
        value[0] = 'e';
        value[1] = (byte) i;
        value[2] = 1;
        value[3] = 1;
        value[4] = 1;
        value[5] = (byte) (value[1] ^ value[2] ^ value[3] ^ value[4]);
        writeCharacteristic(value, BEACONSERVICEUUID, BEACONPROXIMITYUUID);
    }

    public boolean writeCharacteristic(byte[] value, UUID mService,
                                       UUID mCharacteristic) {
        if (RxService == null)
            RxService = mBluetoothGatt.getService(mService);

        showMessage("mBluetoothGatt null" + mBluetoothGatt);
        if (RxService == null) {
            showMessage("Rx service not found!");
            return false;
        }
        if (RxChar == null)
            RxChar = RxService.getCharacteristic(mCharacteristic);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            return false;
        }
        RxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
        Log.d(TAG, "write TXchar - status=" + status);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return status;
    }

    private BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.i(TAG, "connect newState = " + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicWrite status = " + status);
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

    };

    @Override
    protected void onStop() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        super.onStop();
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }



}

