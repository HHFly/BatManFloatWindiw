package ns.com.batmanfloatwindow;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import ns.com.batmanfloatwindow.bean.SDCardInfo;
import ns.com.batmanfloatwindow.service.CleanerService;
import ns.com.batmanfloatwindow.service.FloatWindowService;
import ns.com.batmanfloatwindow.util.StorageUtil;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button fab = (Button) findViewById(R.id.start_float_window);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBlock();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private CleanerService mCleanerService;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCleanerService = ((CleanerService.CleanerServiceBinder) service).getService();
//            mCleanerService.setOnActionListener(FloatWindowService.this);

            //  updateStorageUsage();

            if (!mCleanerService.isScanning() ) {
                mCleanerService.scanCache();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCleanerService.setOnActionListener(null);
            mCleanerService = null;
        }
    };
    private void CheckBlock(){
        SDCardInfo mSDCardInfo = StorageUtil.getSDCardInfo();
        SDCardInfo mSystemInfo = StorageUtil.getSystemSpaceInfo(getApplicationContext());

        long nAvailaBlock;
        long TotalBlocks;
        if (mSDCardInfo != null) {
            nAvailaBlock = mSDCardInfo.free + mSystemInfo.free;
            TotalBlocks = mSDCardInfo.total + mSystemInfo.total;
        } else {
            nAvailaBlock = mSystemInfo.free;
            TotalBlocks = mSystemInfo.total;
        }

        if(StorageUtil.IsLowStorage(TotalBlocks - nAvailaBlock)){
            Log.d("FloatWindowService", "startCleanerService: ");
            bindService(new Intent(getApplicationContext(), CleanerService.class),
                    mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}
