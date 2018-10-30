package ns.com.batmanfloatwindow.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ns.com.batmanfloatwindow.R;
import ns.com.batmanfloatwindow.adapter.ClearMemoryAdapter;
import ns.com.batmanfloatwindow.bean.AppProcessInfo;
import ns.com.batmanfloatwindow.bean.StorageSize;
import ns.com.batmanfloatwindow.service.CoreService;
import ns.com.batmanfloatwindow.util.StorageUtil;
import ns.com.batmanfloatwindow.widget.textcounter.CounterView;
import ns.com.batmanfloatwindow.widget.textcounter.formatters.DecimalFormatter;


public class FloatWindowBigView extends LinearLayout  implements CoreService.OnPeocessActionListener{

    // 记录大悬浮窗的宽度
    public static int viewWidth;

    // 记录大悬浮窗的高度
    public static int viewHeight;
    ClearMemoryAdapter mAdapter;
    List<AppProcessInfo> mAppProcessInfos = new ArrayList<>();
    ListView mListView;
    CounterView textCounter;
    Context mContext;
    LinearLayout bottom_lin, mProgressBar,Nodata;
    RelativeLayout header,rl_close;
    RecyclerView rv ;
    ImageView close,back;
    private CoreService mCoreService;
    TextView mProgressBarText,sufix;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.ProcessServiceBinder) service).getService();
            mCoreService.setOnActionListener(FloatWindowBigView.this);
            mCoreService.scanRunProcess();
            //  updateStorageUsage();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService.setOnActionListener(null);
            mCoreService = null;
        }
    };
    public FloatWindowBigView(final Context context) {
        super(context);
        mContext=context;
        LayoutInflater.from(context).inflate(R.layout.float_window_ddr, this);
        View view = findViewById(R.id.big_window_layout);

        mProgressBarText =findViewById(R.id.progressBarText);
        mProgressBar =findViewById(R.id.progressBar);
        Nodata= findViewById(R.id.Nodata);

        textCounter =findViewById(R.id.textCounter);
        sufix =findViewById(R.id.sufix);
        header=findViewById(R.id.header);
        rl_close=findViewById(R.id.rl_close);

        bottom_lin=findViewById(R.id.bottom_lin);
        rv=findViewById(R.id.listview);


        int footerHeight = context.getResources().getDimensionPixelSize(R.dimen.footer_height);
        textCounter.setAutoFormat(false);
        textCounter.setFormatter(new DecimalFormatter());
        textCounter.setAutoStart(false);
        textCounter.setIncrement(5f); // the amount the number increments at each time interval
        textCounter.setTimeInterval(50); // the time interval (ms) at which the text changes
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
         close = (ImageView) findViewById(R.id.close);
         back = (ImageView) findViewById(R.id.back_button_my);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
//                MyWindowManager.removeBigWindow(context);
//                MyWindowManager.removeSmallWindow(context);
//                Intent intent = new Intent(getContext(), FloatWindowService.class);
//                context.stopService(intent);
                clear();
            }
        });
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回的时候，移除大悬浮窗，创建小悬浮窗
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);

            }
        });


        context.bindService(new Intent(context, CoreService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 初始化列表
     *
     * @param data
     */
    private void initRvAdapter( List<AppProcessInfo> data) {

        if (mAdapter == null) {
            mAdapter = new ClearMemoryAdapter(data);

            rv.setAdapter(mAdapter);
            ViewGroup.LayoutParams lp = rv.getLayoutParams();
            if (data.size() > 3) {
                lp.height = 360;
                rv.setLayoutParams(lp);
            }

             LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
             rv.setLayoutManager(linearLayoutManager);

        } else {
            ViewGroup.LayoutParams lp = rv.getLayoutParams();
            if (data.size() > 3) {
                lp.height = 360;
                rv.setLayoutParams(lp);
            }
            mAdapter.notifyData(data, true);
        }

        rl_close.setVisibility(data==null||data.size()==0?View.GONE:View.VISIBLE);
        Nodata.setVisibility(data==null||data.size()==0?View.VISIBLE:View.GONE);
    }
    private  void  clear(){
        long killAppmemory = 0;


        for (int i = mAppProcessInfos.size() - 1; i >= 0; i--) {
            if (mAppProcessInfos.get(i).checked) {
                killAppmemory += mAppProcessInfos.get(i).memory;
                mCoreService.killBackgroundProcesses(mAppProcessInfos.get(i).processName);
                mAppProcessInfos.remove(mAppProcessInfos.get(i));

            }
        }
        initRvAdapter(mAppProcessInfos);
        Allmemory = Allmemory - killAppmemory;
        Toast.makeText(getContext(), "共清理" + StorageUtil.convertStorage(killAppmemory) + "内存", Toast.LENGTH_LONG).show();
//        T.showLong(mContext, "共清理" + StorageUtil.convertStorage(killAppmemory) + "内存");
        if (Allmemory > 0) {
//            refeshTextCounter();
        }
    }
    @Override
    public void onScanStarted(Context context) {
        mProgressBarText.setText(R.string.scanning);
        showProgressBar(true);
    }

    @Override
    public void onScanProgressUpdated(Context context, int current, int max) {
        mProgressBarText.setText(context.getString(R.string.scanning_m_of_n, current, max));
    }
    public long Allmemory;
    @Override
    public void onScanCompleted(Context context, List<AppProcessInfo> apps) {
        mAppProcessInfos.clear();

        Allmemory = 0;
        for (AppProcessInfo appInfo : apps) {
            if (!appInfo.isSystem) {
                mAppProcessInfos.add(appInfo);
                Allmemory += appInfo.memory;
            }
        }


//        refeshTextCounter();

       initRvAdapter(mAppProcessInfos);
        showProgressBar(false);


//        if (apps.size() > 0) {
//            header.setVisibility(View.VISIBLE);
//            bottom_lin.setVisibility(View.VISIBLE);
//
//
//        } else {
//            header.setVisibility(View.GONE);
//            bottom_lin.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onCleanStarted(Context context) {

    }

    @Override
    public void onCleanCompleted(Context context, long cacheSize) {

    }
    private void showProgressBar(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.startAnimation(AnimationUtils.loadAnimation(
                    mContext, android.R.anim.fade_out));
            mProgressBar.setVisibility(View.GONE);
        }
    }
    private void refeshTextCounter() {
//        mwaveView.setProgress(20);
        StorageSize mStorageSize = StorageUtil.convertStorageSize(Allmemory);
        textCounter.setStartValue(0f);
        textCounter.setEndValue(mStorageSize.value);
        sufix.setText(mStorageSize.suffix);
        //  textCounter.setSuffix(mStorageSize.suffix);
        textCounter.start();
    }
    public void  Destory(){
        Intent intent = new Intent(getContext(), CoreService.class);
        mContext.stopService(intent);
    }
}