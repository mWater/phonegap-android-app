package co.mwater.clientapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

public class AppUpdater {
	private static final String TAG = AppUpdater.class.getCanonicalName();

	private final static String FOLDER_DATA_ROOT = "Android/data";
	private final static String APP_FOLDER = "app";
	private final static String TEMP_FOLDER = "temp";

	/**
	 * Gets the install path of an app version
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getInstalledPath(Context context, int version) throws IOException {
		return buildExternalPath(context, APP_FOLDER) + File.separator + version;
	}
	
	public static void downloadUpdates(Context context) {
		// Send image to be processed
		Intent intent = new Intent(context,
				UpdateIntentService.class);
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage());
			return;
		}
		
		int androidVersion = pInfo.versionCode;
		int appVersion;
		try {
			appVersion = getLatestInstalledVersion(context);
		} catch (IOException e) {
			Log.e(TAG, e.getLocalizedMessage());
			return;
		}
		String url = 
				"http://data.mwater.co/update_app?app=mwater&platform=android&platform_version=" + androidVersion 
				+ "&app_version=" + appVersion;
		intent.putExtra("url", url);

		Log.d(TAG, "Calling update intent service");
		context.startService(intent);
	}
	
	/**
	 * Gets the latest installed version of the app
	 * 
	 * @return
	 * @throws IOException
	 */
	public static int getLatestInstalledVersion(Context context) throws IOException {
		File dir = new File(buildExternalPath(context, APP_FOLDER));
		if (!dir.exists())
			return 0;

		String[] files = dir.list();
		if (files == null)
			return 0;

		// Get maximum version number
		int maxver = 0;
		for (String filename : dir.list()) {
			try {
				int ver = Integer.parseInt(filename);
				if (ver > maxver)
					maxver = ver;
			} catch (NumberFormatException ex) {
			}
		}

		return maxver;
	}

	/**
	 * Installs an app update which consists of a zip with one single folder
	 * for version number. e.g. 12/...files
	 * 
	 * @param context
	 * @param zipFilePath
	 * @throws IOException
	 */
	public static void installAppUpdate(Context context, InputStream zipStream) throws IOException {
		// Create temp folder
		String tempName = UUID.randomUUID().toString();
		String tempDir = buildExternalPath(context, TEMP_FOLDER + File.separator + tempName);

		// Unpack zip
		try {
			Log.d(TAG, "Unzipping app update");
			unpackZip(zipStream, tempDir);

			// Get first inner folder
			File zipDir = new File(tempDir);
			String[] files = zipDir.list();
			if (files == null)
				throw new IOException("Unzip is not directory");
			if (files.length != 1)
				throw new IOException("Incorrect contents of zip update");

			// Move directory inside zip to app folder, replacing existing
			File dest = new File(buildExternalPath(context, APP_FOLDER + File.separator + files[0]));

			// Delete if exists
			if (dest.exists())
				deleteRecursive(dest);

			// Create parent paths
			dest.getParentFile().mkdirs();

			// Move directory
			File src = new File(zipDir + File.separator + files[0]);
			src.renameTo(dest);
			
			Log.i(TAG, "Updated app to version " + files[0]);
		} catch (IOException ex) {
			// Remove temp dir
			deleteRecursive(new File(tempDir));
		}
	}

	/**
	 * Recursively delete a file
	 * @param f
	 * @throws IOException
	 */
	static void deleteRecursive(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				deleteRecursive(c);
		}
		if (!f.delete())
			throw new IOException("Failed to delete file: " + f);
	}

	/**
	 * This static function provides a string to the external storage desired
	 * file.
	 * 
	 * @param context
	 *            Android Context.
	 * @param fileName
	 *            File or directory name.
	 * @return
	 * @throws IOException
	 *             Thrown if the external storage is not available or not
	 *             writeable.
	 */
	public static String buildExternalPath(Context context, String fileName) throws IOException {
		// The following code has been taken from the Android dev guide:
		// http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		if (mExternalStorageAvailable == false) {
			throw new IOException("The external storage is not available");
		}

		if (mExternalStorageWriteable == false) {
			throw new IOException("The external storage is not writeable");
		}

		File extPath = Environment.getExternalStorageDirectory();
		String fullPath = extPath.getAbsolutePath() + File.separator + FOLDER_DATA_ROOT + File.separator + context.getPackageName() + File.separator + fileName;
		return fullPath;
	}

	/**
	 * Unzips a zip file
	 * 
	 * @param zipFilePath
	 * @param outputPath
	 *            directory to put file contents to. Must exist and not end with
	 *            separator
	 * @throws IOException
	 */
	private static void unpackZip(InputStream zipStream, String outputPath) throws IOException
	{
		InputStream is;
		ZipInputStream zis;
		String filename;
		zis = new ZipInputStream(new BufferedInputStream(zipStream));
		ZipEntry ze;
		byte[] buffer = new byte[1024];
		int count;

		while ((ze = zis.getNextEntry()) != null)
		{
			// Get name
			filename = ze.getName();
			
			// Skip if is directory
			if (filename.endsWith(File.separator)) {
				zis.closeEntry();
				continue;
			}

			// Get file object
			File file = new File(outputPath + File.separator + filename);

			// Make directories
			file.getParentFile().mkdirs();

			// Create output stream
			FileOutputStream fout = new FileOutputStream(file);

			// Write file contents
			while ((count = zis.read(buffer)) != -1)
			{
				fout.write(buffer, 0, count);
			}

			fout.close();
			zis.closeEntry();
		}

		zis.close();
	}
}
