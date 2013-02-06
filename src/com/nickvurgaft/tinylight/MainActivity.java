package com.nickvurgaft.tinylight;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Context context;
	public boolean torchIsOn = false;
	protected Camera camera;
	protected Parameters params;
	protected Button btn;
	protected TextView tv;
	protected BatteryManager bm;
	protected Dialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialize assets
		context = getBaseContext();
		tv = (TextView) findViewById(R.id.battery_life);
		bm = new BatteryManager();
		btn = (Button) findViewById(R.id.btn);

		// get the battery life info
		BroadcastReceiver br = new BroadcastReceiver() {
			int level = -1;

			@Override
			public void onReceive(Context context, Intent i) {
				level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				tv.setText("Battery Level: " + level + "%");
			}
		};
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(br, filter);

		// check if a battery flash exists, if not disable turning it on
		boolean isOk = context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH);
		if (isOk)
			Toast.makeText(this, "camera flash detected", Toast.LENGTH_SHORT)
					.show();
		else {
			Toast.makeText(this, "camera flash not found :(",
					Toast.LENGTH_SHORT).show();
			btn.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	protected void onPause() {
		super.onPause();
		if (camera != null) {
			camera.release();
			btn.setText("Turn on!");
			torchIsOn = false;
		}
	}

	public void click(View v) {
		if (!torchIsOn) {
			try {
				turnLedOn();
				btn.setText("Turn off!");
			} catch (Exception e) {
				Toast.makeText(this, "something went wrong :/",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			turnLedOff();
			btn.setText("Turn on!");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about_item:
			showAboutDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showAboutDialog() {
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog);
		dialog.setTitle("About");
		dialog.setCancelable(true);
		dialog.show();
	}

	public void turnLedOn() {
		camera = Camera.open();
		params = camera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(params);
		camera.startPreview();
		torchIsOn = true;
	}

	public void turnLedOff() {
		camera.stopPreview();
		camera.release();
		torchIsOn = false;
	}
}
