package com.aprbrother.ablight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		initHttpServer();
	}

	private void initHttpServer(){
		server = new HelloServer();
		try {
			server.start();
		} catch(IOException ioe) {
			Log.w("Httpd", "The server could not start.");
		}
		Log.w("Httpd", "Web server initialized.");
	}

	/**
	 * 初始化操作
	 */

	public static Beacon getBeaconByMacAdd(String s){
		//find from minor id
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
		super.onStop();
	}
}
