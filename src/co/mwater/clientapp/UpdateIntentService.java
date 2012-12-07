package co.mwater.clientapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class UpdateIntentService extends IntentService {
	private static final String TAG = UpdateIntentService.class.getCanonicalName();

	String url;

	public UpdateIntentService() {
		super("Update Intent Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		url = intent.getStringExtra("url");

		Log.d(TAG, "Attempting download of " + url);

		InputStream input = null;
		HttpURLConnection connection = null;
		try {
			// Open connection
			connection = (HttpURLConnection) new URL(url).openConnection();
			input = connection.getInputStream();

			// Install update
			Log.d(TAG, "Installing update from " + url);
			AppUpdater.installAppUpdate(getApplicationContext(), input);
		} catch (IOException e) {
			Log.w(TAG, "Failed to download update: " + e.getLocalizedMessage());
			if (connection != null) {
				try {
					int code = connection.getResponseCode();
					Log.d(TAG, "Update response code : " + code);
					if (code == 204) // No update
						return;
				} catch (IOException clex) {
					// Ignore not getting code
				}
			}

			Log.e(TAG, e.getMessage());
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException clex) {
				// Ignore close exceptions
			}
		}
	}
}
