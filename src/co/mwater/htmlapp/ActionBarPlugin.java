package co.mwater.htmlapp;

import java.io.File;
import java.io.IOException;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

/**
 * ActionBarPlugin is a PhoneGap plugin that allows hooking into the action bar:
 */
public class ActionBarPlugin extends Plugin {
	private static String TAG = ActionBarPlugin.class.getSimpleName();
	private String menuCallback = null;
	JSONArray menuArgs;
	
	// Set this to the path under which icons will be found. No termination slash
	static public String baseIconPath;

	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            JSONArray of arguments for the plugin.
	 * @param callbackId
	 *            The callback id used when calling back into JavaScript.
	 * @return A PluginResult object with a status and message.
	 */
	public PluginResult execute(String action, final JSONArray args, String callbackId) {
		if (action.equals("menu")) {
			// Parse menu items (array of dicts of id, text*, icon*, ifRoom*,
			// enabled*)
			menuArgs = args;
			menuCallback = callbackId;

			this.cordova.getActivity().runOnUiThread(new Runnable() {
				public void run() {
					((ActionBarPluginActivity) ActionBarPlugin.this.cordova.getActivity()).reloadMenu(ActionBarPlugin.this);
				}
			});

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			result.setKeepCallback(true);
			return result;
		}
		if (action.equals("title")) {
			// Set title of actionbar
			try {
				final String title = args.get(0).toString();
				this.cordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						String defaultAppName = getSherlockActivity().getString(R.string.app_name);
						getSherlockActivity().getSupportActionBar().setTitle(title != "null" && title != null ? title : defaultAppName); 
					}
				});
			} catch (JSONException e) {
				return new PluginResult(PluginResult.Status.INVALID_ACTION);
			}

			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			return result;
		}
		if (action.equals("up")) {
			// Set state of up display on home button
			try {
				final Boolean enabled = args.getBoolean(0);
				this.cordova.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
					}
				});
			} catch (JSONException e) {
				return new PluginResult(PluginResult.Status.INVALID_ACTION);
			}
			PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
			return result;
		}
		return new PluginResult(PluginResult.Status.INVALID_ACTION);
	}

	void handleMenuItemClick(String id) {
		Log.d(TAG, "Menu click " + id);
		PluginResult result = new PluginResult(PluginResult.Status.OK, id);
		result.setKeepCallback(true);
		this.success(result, this.menuCallback);
	}

	SherlockActivity getSherlockActivity() {
		return (SherlockActivity) this.cordova.getActivity();
	}

	public void createMenu(Menu menu) {
		Log.d(TAG, "Menu create");
		try {
			// For each menu item
			for (int i = 0; i < menuArgs.length(); i++) {
				JSONObject item = menuArgs.getJSONObject(i);

				final String id = item.getString("id");
				MenuItem menuItem = menu.add(item.getString("title"));
				menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						handleMenuItemClick(id);
						return true;
					}
				});
				if (item.has("icon")) {
					Drawable d = Drawable.createFromPath(baseIconPath + File.separator  + item.getString("icon"));
					menuItem.setIcon(d);
				}
				if (item.has("enabled")) {
					menuItem.setEnabled(item.getBoolean("enabled"));
				}
				if (item.has("ifRoom") && item.getBoolean("ifRoom")) {
					menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Invalid menu", e);
		} 
	}

	public void homeClicked() {
		handleMenuItemClick("home");
	}
}

interface ActionBarPluginActivity {
	void reloadMenu(ActionBarPlugin actionBarPlugin);
}