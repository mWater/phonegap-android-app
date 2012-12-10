package co.mwater.clientapp;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "0AiOtqbLhVi5ddHlmZ1M1dzg5T0ZpbmdFUjZ2aXltNEE")
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		if (!isDebug())
			ACRA.init(this);
		super.onCreate();
	}

	public boolean isDebug() {
		return ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0);
	}
}