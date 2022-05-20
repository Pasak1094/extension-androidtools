package org.haxe.extension;

import android.os.Vibrator;
import android.os.Environment;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Point;
import android.view.Display;
import android.content.res.AssetManager;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.provider.Settings;
import android.util.Log;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.lang.Object;
import java.io.File;

import com.google.gson.Gson;
import org.haxe.lime.HaxeObject;

/* 
	You can use the Android Extension class in order to hook
	into the Android activity lifecycle. This is not required
	for standard Java code, this is designed for when you need
	deeper integration.
	
	You can access additional references from the Extension class,
	depending on your needs:
	
	- Extension.assetManager (android.content.res.AssetManager)
	- Extension.callbackHandler (android.os.Handler)
	- Extension.mainActivity (android.app.Activity)
	- Extension.mainContext (android.content.Context)
	- Extension.mainView (android.view.View)
	
	You can also make references to static or instance methods
	and properties on Java classes. These classes can be included 
	as single files using <java path="to/File.java" /> within your
	project, or use the full Android Library Project format (such
	as this example) in order to include your own AndroidManifest
	data, additional dependencies, etc.
	
	These are also optional, though this example shows a static
	function for performing a single task, like returning a value
	back to Haxe from Java.
*/
public class Tools extends Extension {
	public static Gson gson = new Gson();
	public static HaxeObject callback;
	public static Point size;
	public static final int ORIENTATION_UNSPECIFIED = 0;
	public static final int ORIENTATION_PORTRAIT = 1;
	public static final int ORIENTATION_LANDSCAPE = 2;
	private static KeyguardLock keyguardLock = null;
	private static int fixedOrientation = 0;

	public static void requestPermissions(String per[], int reqcode){
		try {
			Extension.mainActivity.requestPermissions(per, reqcode);
		} catch (Exception e) {
			Log.e("Tools", e.toString());
		}
	}

	public static String getFileUrl(String path){
		try {		
			return Uri.fromFile(new File(path)).toString();
		} catch (Exception e) {
			Log.d("Tools", e.toString());
			return "";
		}
	}

	public static String[] getGrantedPermissions(){
		List<String> granted = new ArrayList<String>();
		try {
			PackageInfo pi = Extension.mainContext.getPackageManager().getPackageInfo(Extension.mainContext.getPackageName(), PackageManager.GET_PERMISSIONS);
			for (int i = 0; i < pi.requestedPermissions.length; i++) {
				if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
					granted.add(pi.requestedPermissions[i]);
				}
			}
		} catch (Exception e) {
			Log.e("Tools", e.toString());
		}
		return granted.toArray(new String[granted.size()]);
	}

	public static String getExternalStorageDirectory(){
		try {
			return Environment.getExternalStorageDirectory().getPath();
		} catch (Exception e) {
			Log.d("Tools", e.toString());
			return "";
		}
	}

	public static void appSettings(String settings, int reqcode){
		try {
			Intent appSettings = new Intent(settings, Uri.parse("package:" + Extension.packageName));
			Extension.mainActivity.startActivityForResult(appSettings, reqcode);
		} catch (Exception e) {
			Log.e("Tools", e.toString());
		}
	}

	public static void openFileManager(String action, String dir, String type, String title, int reqcode){
		try {
			Intent intent = new Intent(action);
			Uri uri = Uri.parse(dir);
			intent.setDataAndType(uri, type);
			Extension.mainActivity.startActivityForResult(Intent.createChooser(intent, title), reqcode);
		} catch (Exception e) {
			Log.e("Tools", e.toString());
		}
	}

	public static void setRequestedOrientation(int SCREEN_ORIENTATION) {
		try {
			int requestedOrientation;

			switch (SCREEN_ORIENTATION) {
				case ORIENTATION_PORTRAIT:
					requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				case ORIENTATION_LANDSCAPE:
					requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
					break;
				default:
					requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
			}

			Extension.mainActivity.setRequestedOrientation(requestedOrientation);
			fixedOrientation = requestedOrientation;
		} catch (Exception e) {
			Log.d("Tools", e.toString());
		}
	}

	public static void setBrightness(float brightness) {
		try {
			WindowManager.LayoutParams layout = Extension.mainActivity.getWindow().getAttributes();
			layout.screenBrightness = brightness;
			Extension.mainActivity.getWindow().setAttributes(layout);
		} catch (Exception e) {
			Log.d("Tools", e.toString());
		}
	}

	public static String objectToJson(Object obj){
		try {
			return gson.toJson(obj);
		} catch (Exception e) {
			Log.d("Tools", e.toString());
			return "{}";
		}
	}

	public static void vibrate(int duration){
		try {
			((Vibrator) mainContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(duration);
		} catch (Exception e) {
			Log.d("Tools", e.toString());
		}
	}

	public static void wakeUp(){
		try {
			PowerManager pm = (PowerManager) mainContext.getSystemService(Context.POWER_SERVICE);
			WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
			PowerManager.ACQUIRE_CAUSES_WAKEUP, "Hardware.class");
			wakeLock.acquire();
			wakeLock.release();
			wakeLock = null;

			KeyguardManager keyguardManager = (KeyguardManager) mainActivity.getSystemService(Activity.KEYGUARD_SERVICE); 
			if(keyguardLock == null) {
				keyguardLock = keyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE); 
			}
			keyguardLock.disableKeyguard();
		} catch (Exception e) {
			Log.d("Tools", e.toString());
		}
	}

	public static int getScreenHeight() {
		return size.y;
	}

	public static int getScreenWidth() {
		return size.x;
	}

	public static void setCallback(final HaxeObject _callback){
		callback = _callback;
	}
	
	/**
	 * Called when an activity you launched exits, giving you the requestCode 
	 * you started it with, the resultCode it returned, and any additional data 
	 * from it.
	 */
    @Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data){
		callback.call("onActivityResult", new Object[] {requestCode, resultCode, data});
		return true;
	}

	/**
	 * Called when the activity receives th results for permission requests.
	 */
    @Override
	public boolean onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
		callback.call("onRequestPermissionsResult", new Object[] {requestCode, permissions, grantResults});
		return true;
	}
	
	/**
	 * Called when the activity is starting.
	 */
    @Override
	public void onCreate(Bundle savedInstanceState) {}
	
	/**
	 * Perform any final cleanup before an activity is destroyed.
	 */
    @Override
	public void onDestroy() {
		if(keyguardLock != null) {
			keyguardLock.reenableKeyguard();
			keyguardLock = null;
		}
	}
	
	/**
	 * Called as part of the activity lifecycle when an activity is going into
	 * the background, but has not (yet) been killed.
	 */
    @Override
	public void onPause() {
		if(keyguardLock != null) {
			keyguardLock.reenableKeyguard();
		}
	}
	
	/**
	 * Called after {@link #onStop} when the current activity is being 
	 * re-displayed to the user (the user has navigated back to it).
	 */
    @Override
	public void onRestart() {}
	
	/**
	 * Called after {@link #onRestart}, or {@link #onPause}, for your activity 
	 * to start interacting with the user.
	 */
    @Override
	public void onResume() {
		if (fixedOrientation != 0) {
			Extension.mainActivity.setRequestedOrientation(fixedOrientation);
		}
	}
	
	/**
	 * Called after {@link #onCreate} &mdash; or after {@link #onRestart} when  
	 * the activity had been stopped, but is now again being displayed to the 
	 * user.
	 */
    @Override
	public void onStart() {
		Display display = mainActivity.getWindowManager().getDefaultDisplay();
		size = new Point();
		display.getSize(size);
	}
	
	/**
	 * Called when the activity is no longer visible to the user, because 
	 * another activity has been resumed and is covering this one. 
	 */
    @Override
	public void onStop() {}
}