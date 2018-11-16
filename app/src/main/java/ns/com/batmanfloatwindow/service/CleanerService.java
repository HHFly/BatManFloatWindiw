package ns.com.batmanfloatwindow.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ns.com.batmanfloatwindow.R;
import ns.com.batmanfloatwindow.bean.CacheListItem;
import ns.com.batmanfloatwindow.util.FilterAppUtils;

public class CleanerService extends Service {

    public static final String ACTION_CLEAN_AND_EXIT = "com.yzy.cache.cleaner.CLEAN_AND_EXIT";

    private static final String TAG = "CleanerService";

    private Method mGetPackageSizeInfoMethod, mFreeStorageAndNotifyMethod;
    private OnActionListener mOnActionListener;
    private boolean mIsScanning = false;
    private boolean mIsCleaning = false;
    private long mCacheSize = 0;
    final List<CacheListItem> apps = new ArrayList<CacheListItem>();
    public static interface OnActionListener {
        public void CleaneronScanStarted(Context context);

        public void CleaneronScanProgressUpdated(Context context, int current, int max);

        public void CleaneronScanCompleted(Context context, List<CacheListItem> apps);

        public void CleaneronCleanStarted(Context context);

        public void CleaneronCleanCompleted(Context context, long cacheSize);
    }

    public class CleanerServiceBinder extends Binder {

        public CleanerService getService() {
            return CleanerService.this;
        }
    }

    private CleanerServiceBinder mBinder = new CleanerServiceBinder();

    private class TaskScan extends AsyncTask<Void, Integer, List<CacheListItem>> {

        private int mAppCount = 0;

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.CleaneronScanStarted(CleanerService.this);
            }
        }

        @Override
        protected List<CacheListItem> doInBackground(Void... params) {
            mCacheSize = 0;

            final List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(
                    PackageManager.GET_META_DATA);

            publishProgress(0, packages.size());

            final CountDownLatch countDownLatch = new CountDownLatch(packages.size());


            try {
                for (final ApplicationInfo pkg : packages) {


                    mGetPackageSizeInfoMethod.invoke(getPackageManager(), pkg.packageName,
                            new IPackageStatsObserver.Stub() {

                                @Override
                                public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                                        throws RemoteException {
                                    synchronized (apps) {
                                        publishProgress(++mAppCount, packages.size());
                                        if ((pkg.flags & pkg.FLAG_SYSTEM) != 0 || !FilterAppUtils.filter(pkg.packageName)) {
                                            //系统应用
                                        }else {
                                            //非系统应用
                                            if (succeeded && pStats.dataSize > 0) {
                                                try {

                                                    apps.add(new CacheListItem(pStats.packageName,
                                                            getPackageManager().getApplicationLabel(
                                                                    getPackageManager().getApplicationInfo(
                                                                            pStats.packageName,
                                                                            PackageManager.GET_META_DATA)
                                                            ).toString(),
                                                            getPackageManager().getApplicationIcon(
                                                                    pStats.packageName),
                                                            pStats.dataSize
                                                    ));

                                                    mCacheSize += pStats.dataSize;
                                                } catch (PackageManager.NameNotFoundException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        }


                                    synchronized (countDownLatch) {
                                        countDownLatch.countDown();
                                    }
                                }
                            }
                    );
                }

                countDownLatch.await();
            } catch (InvocationTargetException | InterruptedException | IllegalAccessException e) {
                e.printStackTrace();
            }

            return new ArrayList<>(apps);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (mOnActionListener != null) {
                mOnActionListener.CleaneronScanProgressUpdated(CleanerService.this, values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(List<CacheListItem> result) {
            if (mOnActionListener != null) {
                mOnActionListener.CleaneronScanCompleted(CleanerService.this, result);
            }
            Log.d("FloatWindowService", "getCacheSize: "+mCacheSize);
            mIsScanning = false;
            if (getCacheSize() > 0) {
                cleanCache();
            }
        }
    }

    private class TaskClean extends AsyncTask<Void, Void, Long> {

        @Override
        protected void onPreExecute() {
            if (mOnActionListener != null) {
                mOnActionListener.CleaneronCleanStarted(CleanerService.this);
            }
        }

        @Override
        protected Long doInBackground(Void... params) {
            final CountDownLatch countDownLatch = new CountDownLatch(apps.size());

//            StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());

            //                mFreeStorageAndNotifyMethod.invoke(getPackageManager(),
//                        (long) stat.getBlockCount() * (long) stat.getBlockSize(),
//                        new IPackageDataObserver.Stub() {
//                            @Override
//                            public void onRemoveCompleted(String packageName, boolean succeeded)
//                                    throws RemoteException {
//                                countDownLatch.countDown();
//                            }
//                        }
//                );
            clearUserData(apps.get(5).getPackageName());
//                for(int i=0;i<apps.size();i++){
//                    final int finalI = i;
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            clearUserData(apps.get(finalI).getPackageName());
//                            countDownLatch.countDown();
//                        }
//                    }).start();
//                }
//                countDownLatch.await();

//                String msg = getString(R.string.cleaned, Formatter.formatShortFileSize(
//                        CleanerService.this, getCacheSize()));

//                Log.d(TAG, msg);

//                Toast.makeText(CleanerService.this, msg, Toast.LENGTH_LONG).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 5000);

            return mCacheSize;
        }

        @Override
        protected void onPostExecute(Long result) {
            mCacheSize = 0;

            if (mOnActionListener != null) {
                mOnActionListener.CleaneronCleanCompleted(CleanerService.this, result);
            }

            mIsCleaning = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        try {
            mGetPackageSizeInfoMethod = getPackageManager().getClass().getMethod(
                    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);

            mFreeStorageAndNotifyMethod = getPackageManager().getClass().getMethod(
                    "freeStorageAndNotify", long.class, IPackageDataObserver.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(ACTION_CLEAN_AND_EXIT)) {
                setOnActionListener(new OnActionListener() {
                    @Override
                    public void CleaneronScanStarted(Context context) {

                    }

                    @Override
                    public void CleaneronScanProgressUpdated(Context context, int current, int max) {

                    }

                    @Override
                    public void CleaneronScanCompleted(Context context, List<CacheListItem> apps) {
                        if (getCacheSize() > 0) {
                            cleanCache();
                        }
                    }

                    @Override
                    public void CleaneronCleanStarted(Context context) {

                    }

                    @Override
                    public void CleaneronCleanCompleted(Context context, long cacheSize) {
                        String msg = getString(R.string.cleaned, Formatter.formatShortFileSize(
                                CleanerService.this, cacheSize));

                        Log.d(TAG, msg);

                        Toast.makeText(CleanerService.this, msg, Toast.LENGTH_LONG).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                stopSelf();
                            }
                        }, 5000);
                    }
                });

                scanCache();
            }
        }

        return START_NOT_STICKY;
    }

    public void scanCache() {
        mIsScanning = true;

        new TaskScan().execute();
    }

    public void cleanCache() {
        mIsCleaning = true;

        new TaskClean().execute();
    }

    public void setOnActionListener(OnActionListener listener) {
        mOnActionListener = listener;
    }

    public boolean isScanning() {
        return mIsScanning;
    }

    public boolean isCleaning() {
        return mIsCleaning;
    }

    public long getCacheSize() {
        return mCacheSize;
    }
    /**
     *
     * @param packageName 要清除数据的应用的包名
     */
    private void clearUserData(String packageName){
        try {

            // 获取其他应用的上下文
            Context c = createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            ActivityManager am = (ActivityManager)
                    c.getSystemService(Context.ACTIVITY_SERVICE);
            // 清除对应应用的数据 需要 这个权限(这个权限是系统应用才能有的)"android.permission.CLEAR_APP_USER_DATA"
            am.clearApplicationUserData();
        }  catch (Exception e) {
            e.printStackTrace();
        }

    }
}
