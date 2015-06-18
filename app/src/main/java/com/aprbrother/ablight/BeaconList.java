package com.aprbrother.ablight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.aprilbrother.aprilbrothersdk.Beacon;
import com.aprilbrother.aprilbrothersdk.BeaconManager;
import com.aprilbrother.aprilbrothersdk.BeaconManager.MonitoringListener;
import com.aprilbrother.aprilbrothersdk.BeaconManager.RangingListener;
import com.aprilbrother.aprilbrothersdk.Region;

/**
 * 搜索展示beacon列表
 */
public class BeaconList extends Activity {
	private static final int REQUEST_ENABLE_BT = 1234;
	private static final String TAG = "BeaconList";
	// private static final Region ALL_BEACONS_REGION = new Region("apr",
	// "B9407F30-F5F8-466E-AFF9-25556B57FE6D",
	// null, null);
	private static final Region ALL_BEACONS_REGION = new Region("apr", null,
			null, null);
//	private static final Region ALL_BEACONS_REGION = new Region("apr", "e2c56db5-dffb-48d2-b060-d0f5a71096e0",
//			985,211);
	//扫描所有uuid为"aa000000-0000-0000-0000-000000000000"的beacon
//	private static final Region ALL_BEACONS_REGION = new Region("apr", "aa000000-0000-0000-0000-000000000000",
//			null, null);
	private BeaconAdapter adapter;
	private BeaconManager beaconManager;
	private ArrayList<Beacon> myBeacons;
	private RelativeLayout ll_beacons_progress;
	private HelloServer server;


	private Beacon beacon;
	private BluetoothDevice myDevice;
	private BluetoothGatt mBluetoothGatt;
	public final static UUID BEACONSERVICEUUID = UUID
			.fromString("0000fab0-0000-1000-8000-00805f9b34fb");
	public final static UUID BEACONPROXIMITYUUID = UUID
			.fromString("0000fab1-0000-1000-8000-00805f9b34fb");
	public final static UUID BEACONMAJORUUID = UUID
			.fromString("0000fab2-0000-1000-8000-00805f9b34fb");

	public static final UUID CCCD = UUID
			.fromString("00002901-0000-1000-8000-00805f9b34fb");

	private long lastTime;

	private BluetoothGattCallback myGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
											int newState) {
			Log.i(TAG, "connect newState = " + newState);
			showMessage("status change " + newState);
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.i(TAG, "Attempting to start service discovery:"
						+ mBluetoothGatt.discoverServices());

			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				Log.i(TAG, "Disconnected from GATT server.");
			}
			super.onConnectionStateChange(gatt, status, newState);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			showMessage("service discovered!!!!!");
			Log.i(TAG, "onServicesDiscovered status = " + status);
//			DetailActivity.this.runOnUiThread(new Runnable() {
//
//				@Override
//				public void run() {
//					tv.setText("disconnect");
//				}
//			});
//			enableTXNotification();
			super.onServicesDiscovered(gatt, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
										  BluetoothGattCharacteristic characteristic, int status) {
			showMessage("Writing status:"+status);
			Log.i(TAG, "onCharacteristicWrite status = " + status);
			super.onCharacteristicWrite(gatt, characteristic, status);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
//		control();
	}

	private void initHttpServer(){
		server = new HelloServer(this);
		try {
			server.start();
		} catch(IOException ioe) {
			Log.w("Httpd", "The server could not start.");
		}
		Log.w("Httpd", "Web server initialized.");
	}

	public void control(String macAdd) {

		System.out.println("ddebug inside control");
		beacon = this.getBeaconByMacAdd("D0:FF:50:67:7C:4A");
//		beacon = this.getBeaconByMacAdd(macAdd);
		myDevice = deviceFromBeacon(beacon);
		mBluetoothGatt = myDevice.connectGatt(this, false, myGattCallback);
		mBluetoothGatt.connect();
		mBluetoothGatt.discoverServices();
		System.out.println("ddebug about to write 100 to" + beacon.getMacAddress());


		int i = 0;
		int[] a = {10,50,100,0};
		if(i==4){i=0;}
		write(a[i++]);
		try{
			Thread.sleep(5000);
			showMessage("sleeping");
		}catch(Exception e){

		}
		if(mBluetoothGatt != null) {
			showMessage("closing and disconnect");
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
		}
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
		System.out.println("ddebug writing"+i);
		int NumOfTries = 10;
		boolean status = false;

			status = writeCharacteristic(value, BEACONSERVICEUUID, BEACONPROXIMITYUUID);
showMessage("writing status "+status);
	}

	public boolean writeCharacteristic(byte[] value, UUID mService, UUID mCharacteristic) {
		int tries = 10;

		if (RxService == null) {
			int i=0;
			while(RxService == null && i < tries) {
				i++;
				RxService = mBluetoothGatt.getService(mService);
				System.out.println("ddebug here RxService"+RxService.toString());
			}

		}
//		showMessage("mBluetoothGatt RxService null" + mBluetoothGatt);
		if (RxService == null) {
			showMessage("Rx service not found! in control");
			return false;
		}
		if (RxChar == null){
			int i=0;
			while(RxChar == null && i < tries) {
				i++;
				RxChar = RxService.getCharacteristic(mCharacteristic);
				System.out.println("ddebug here RxChar"+RxChar.getDescriptor(mCharacteristic));
			}
		}
		if (RxChar == null) {
			showMessage("Rx charateristic not found! in control");
			return false;
		}
		RxChar.setValue(value);
		showMessage("RxChar.setValue(value)"+ value);

		boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
		showMessage("status          " + status);



		Log.d(TAG, "write TXchar - status=" + status);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return status;
	}

	private void showMessage(String msg) {
		System.out.println("ddebug"+msg);
		Log.e(TAG, msg);
	}

	public Beacon getBeaconByMacAdd(String s){
		//find from mac address
		for (Beacon bc : this.myBeacons){
			if(bc.getMacAddress().equals(s)){
				return bc;
			}
		}
		return null;
	}

	private void init() {
		myBeacons = new ArrayList<Beacon>();
		ListView lv = (ListView) findViewById(R.id.lv);
		adapter = new BeaconAdapter(this);
		lv.setAdapter(adapter);
		
		ll_beacons_progress = (RelativeLayout) findViewById(R.id.ll_beacons_progress);
		
		beaconManager = new BeaconManager(this);
		beaconManager.setMonitoringExpirationMill(10L);
		beaconManager.setRangingExpirationMill(10L);
		beaconManager.setForegroundScanPeriod(200, 0);
		beaconManager.setRangingListener(new RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region region,
					final List<Beacon> beacons) {
				if(beacons!=null && beacons.size()>0){
					for(Beacon beacon : beacons){
						Log.d("beacon", "" + beacon.getMinor());
						Log.d("beacon", "" + beacon.getMacAddress());
						Log.d("beacon", "" + beacon.getName());
						if(beacon.getName()!=null && !myBeacons.contains(beacon)){							
							myBeacons.add(beacon);
						}
					}
				}
				BeaconList.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(myBeacons.size()>0){
							ll_beacons_progress.setVisibility(View.GONE);
						}
						adapter.replaceWith(myBeacons);
					}
				});
			}
		});
		
		beaconManager.setMonitoringListener(new MonitoringListener() {
			
			@Override
			public void onExitedRegion(Region arg0) {
				Toast.makeText(BeaconList.this, "通知离开", 0).show();
				
			}
			
			@Override
			public void onEnteredRegion(Region arg0, List<Beacon> arg1) {
				Toast.makeText(BeaconList.this, "通知进入", 0).show();
			}
		});
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(BeaconList.this,
						DetailActivity.class);
				Beacon beacon = myBeacons.get(arg2);
				Bundle bundle = new Bundle();
				bundle.putParcelable("beacon", beacon);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	/**
	 * 连接服务 开始搜索beacon
	 */
	private void connectToService() {
		adapter.replaceWith(Collections.<Beacon> emptyList());
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_BEACONS_REGION);
//					beaconManager.startMonitoring(ALL_BEACONS_REGION);
				} catch (RemoteException e) {
					
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				connectToService();
			} else {
				Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG)
						.show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initHttpServer();

		if (!beaconManager.hasBluetooth()) {
			Toast.makeText(this, "Device does not have Bluetooth Low Energy",
					Toast.LENGTH_LONG).show();
			return;
		}

		if (!beaconManager.isBluetoothEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		} else {
			connectToService();
		}
	}

	@Override
	protected void onDestroy() {
//		beaconManager.disconnect();
		super.onDestroy();
		if (server != null)
			server.stop();
	}

	@Override
	protected void onStop() {
		try {
			myBeacons.clear();
			beaconManager.stopRanging(ALL_BEACONS_REGION);
			beaconManager.disconnect();
		} catch (RemoteException e) {
			Log.d(TAG, "Error while stopping ranging", e);
		}

		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
		}
		if (server != null){
			server.stop();
		server = null;}
		super.onStop();
	}
}
