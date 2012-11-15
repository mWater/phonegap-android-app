package co.mwater.htmlapp;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.IPlugin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity implements CordovaInterface, ActionBarPluginActivity {
	private static String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "onRestart");
		super.onRestart(); 
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	ActionBarPlugin actionBarPlugin;

	private IPlugin activityResultCallback;

	private Object activityResultKeepRunning;

	private boolean keepRunning;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");

		setContentView(R.layout.activity_main); 
		CordovaWebView cwv = (CordovaWebView) findViewById(R.id.tutorialView);
		SpecialChromeClient client = new SpecialChromeClient(this, cwv);
		cwv.setWebChromeClient(client);
		cwv.loadUrl("file:///android_asset/www/index.html?cordova=true");
	}

	public void reloadMenu(ActionBarPlugin actionBarPlugin) {
		Log.d(TAG, "reloadMenu");
		this.actionBarPlugin = actionBarPlugin;
		this.supportInvalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "createMenu: " + ((actionBarPlugin != null) ? "ok" : "notready"));

		if (actionBarPlugin != null)
			actionBarPlugin.createMenu(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			actionBarPlugin.homeClicked();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void cancelLoadUrl() {
		// TODO Auto-generated method stub
		Log.d("MainActivity", "cancelLoad");
	}

	public Activity getActivity() {
		return this;
	}

	public Context getContext() {
		return this;
	}

	/**
     * Called when a message is sent to plugin.
     *
     * @param id            The message id
     * @param data          The message data
     * @return              Object or null
     */
    public Object onMessage(String id, Object data) {
        Log.d(TAG, "onMessage(" + id + "," + data + ")");
        return null;
    }

	public void setActivityResultCallback(IPlugin plugin) {
		this.activityResultCallback = plugin;
	}

	@Override
	/**
	 * Called when an activity you launched exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 *
	 * @param requestCode       The request code originally supplied to startActivityForResult(),
	 *                          allowing you to identify who this result came from.
	 * @param resultCode        The integer result code returned by the child activity through its setResult().
	 * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		IPlugin callback = this.activityResultCallback;
		if (callback != null) {
			callback.onActivityResult(requestCode, resultCode, intent);
		}
	}

    @Override
	protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	/**
     * Launch an activity for which you would like a result when it finished. When this activity exits,
     * your onActivityResult() method will be called.
     *
     * @param command           The command object
     * @param intent            The intent to start
     * @param requestCode       The request code that is passed to callback to identify the activity
     */
    public void startActivityForResult(IPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        // If multitasking turned on, then disable it for activities that return results
        if (command != null) {
            this.keepRunning = false;
        }

        // Start activity
        super.startActivityForResult(intent, requestCode);
    }
}
