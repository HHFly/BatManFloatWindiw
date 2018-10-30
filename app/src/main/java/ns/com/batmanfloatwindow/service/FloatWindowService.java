package ns.com.batmanfloatwindow.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ns.com.batmanfloatwindow.bean.AppProcessInfo;
import ns.com.batmanfloatwindow.widget.MyWindowManager;

public class FloatWindowService extends Service  {

    // 用于在线程中创建或移除悬浮窗
    private Handler handler = new Handler();
    public  static  boolean isShow=true;
    // 定时器，定时进行检测当前应该创建还是移除悬浮窗
    private Timer timer;
    List<AppProcessInfo> mAppProcessInfos = new ArrayList<>();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔0.5秒刷新一次
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                MyWindowManager.createSmallWindow(getApplicationContext());
//            }
//        });
        Log.d("FloatWindowService", "onStartCommand: ");
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 1000);
        }
        init_time();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service被终止的同时也停止定时器继续运行
        timer.cancel();
        timer = null;
        flag=false;
    }


    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
            if (isHome() && !MyWindowManager.isWindowShowing()&&isShow) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.createSmallWindow(getApplicationContext());
                    }
                });
            }
//             当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
            else if (!isShow && MyWindowManager.isWindowShowing()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.removeSmallWindow(getApplicationContext());
                        MyWindowManager.removeBigWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
            else if (isHome() && MyWindowManager.isWindowShowing()&&isShow) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
//                        MyWindowManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }
        }
    }
    public  static int dismisstime =0;//透明时间标志位
//    复位标志位
    public  static void reSetDismisstime(){
        dismisstime=0;
    }
    private boolean flag =true;
    //日期 时间模块初始化
    private void init_time() {
        ExecutorService timePool = Executors.newSingleThreadExecutor();  //采用线程池单一线程方式，防止被杀死
        timePool.execute(new Runnable() {
            @Override
            public void run() {

                while (flag) {
                    try {
                        if (dismisstime >2) {
                            alpha =0.3f;
                            mHandler.sendEmptyMessage(0);
                        } else {
                            alpha =1f;
                            mHandler.sendEmptyMessage(0);
                            dismisstime++;
                        }
                        //延时一秒作用

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private  float alpha =1f;
    Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                if(MyWindowManager.getAlphaSmallWindow()!=alpha){
                    MyWindowManager.setAlphaSmallWindow(alpha);
                }

            }

        };
    };
    // 判断当前界面是否是桌面
    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }


    // 获得属于桌面的应用的应用包名称
    // @return 返回包含所有包名的字符串列表
    private List<String> getHomes() {
        List<String> names = new ArrayList<>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }
}
