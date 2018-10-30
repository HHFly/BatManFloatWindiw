package ns.com.batmanfloatwindow.widget;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;

import ns.com.batmanfloatwindow.R;
import ns.com.batmanfloatwindow.bean.AppProcessInfo;
import ns.com.batmanfloatwindow.service.CoreService;
import ns.com.batmanfloatwindow.util.StorageUtil;

import static ns.com.batmanfloatwindow.service.FloatWindowService.reSetDismisstime;

public class FloatWindowSmallView extends LinearLayout implements CoreService.OnPeocessActionListener {

    // 记录小悬浮窗的宽度
    public static int viewWidth;

    // 记录小悬浮窗的高度
    public static int viewHeight;

    // 记录系统状态栏的高度
    private static int statusBarHeight;

    // 用于更新小悬浮窗的位置
    private WindowManager windowManager;

    // 小悬浮窗的参数
    private WindowManager.LayoutParams mParams;

    // 记录当前手指位置在屏幕上的横坐标值
    private float xInScreen;

    //记录当前手指位置在屏幕上的纵坐标值
    private float yInScreen;

    // 记录手指按下时在屏幕上的横坐标的值
    private float xDownInScreen;

    // 记录手指按下时在屏幕上的纵坐标的值
    private float yDownInScreen;

    // 记录手指按下时在小悬浮窗的View上的横坐标的值
    private float xInView;

    // 记录手指按下时在小悬浮窗的View上的纵坐标的值
    private float yInView;
    private ImageView home ,back ,clean;
    private CoreService mCoreService;
    private  Context mContext;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(FloatWindowSmallView.this);
            mCoreService.cleanAllProcess();
            //  updateStorageUsage();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService.setOnActionListener(null);
            mCoreService = null;
        }
    };

    public FloatWindowSmallView(final Context context) {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        xInScreen = windowManager.getDefaultDisplay().getWidth();
        mContext =context;
        LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
        View view = findViewById(R.id.small_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        TextView percentView = (TextView) findViewById(R.id.percent);
        home = (ImageView) findViewById(R.id.iv_home);
        back =(ImageView) findViewById(R.id.iv_back);
        clean=(ImageView) findViewById(R.id.iv_ddr);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("onTouchEvent", "onTouchEvent: ");
                reSetDismisstime();
                new Thread() {
                    public void run () {
                        try {
                            Instrumentation inst= new Instrumentation();
                            inst.sendKeyDownUpSync(KeyEvent. KEYCODE_BACK);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        home.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                reSetDismisstime();
                Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

                mHomeIntent.addCategory(Intent.CATEGORY_HOME);
                mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(mHomeIntent);
            }


        });
        clean.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reSetDismisstime();
                context.bindService(new Intent(context, CoreService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        });
        clean.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                reSetDismisstime();
                openBigWindow();
                return true;
            }

        });
        percentView.setText(MyWindowManager.getUsedPercentValue(context));

    }
    public  void  setAlpha(float alpha){
        back.setAlpha(alpha);
        clean.setAlpha(alpha);
        home.setAlpha(alpha);
    }
    public  float  getAlpha(){
        return home.getAlpha();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                reSetDismisstime();
//                xInView = event.getX();
                yInView = event.getY();
//                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
//                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();

                break;
            case MotionEvent.ACTION_MOVE:
//                xInScreen = event.getRawX();
                reSetDismisstime();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
//                    openBigWindow();

                }

                break;
            default:
                break;
        }
        return true;
    }

    // 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
    // @param params 小悬浮窗的参数
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    // 更新小悬浮窗在屏幕中的位置
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }

    // 打开大悬浮窗，同时关闭小悬浮窗
    private void openBigWindow() {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }

    // 用于获取状态栏的高度
    //@return 返回状态栏高度的像素值
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    @Override
    public void onScanStarted(Context context) {

    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {

    }

    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {

    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {
        if (cacheSize > 0) {
            Toast.makeText(getContext(), "为您释放" + StorageUtil.convertStorage(cacheSize) + "内存", Toast.LENGTH_LONG).show();
//            T.showLong(mContext, "一键清理 开源版,为您释放" + StorageUtil.convertStorage(cacheSize) + "内存");
        } else {
            Toast.makeText(getContext(), "您刚刚清理过内存,请稍后再来~", Toast.LENGTH_LONG).show();
//            T.showLong(mContext, "您刚刚清理过内存,请稍后再来~");

        }
        mContext.unbindService(mServiceConnection);
    }
}