package cn.ugee.app;



import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;





public class BaseApplication extends Application {
    protected static BaseApplication appInstance;

    public static int LEN=2;
    public static BaseApplication getAppInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance=this;
            LogToFile.init(this,1);
        //腾讯bugly
         CrashReport.initCrashReport(getApplicationContext(), "17d43c192e", false);
    }

}
