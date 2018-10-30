package ns.com.batmanfloatwindow.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ns.com.batmanfloatwindow.service.FloatWindowService;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //后边的XXX.class就是要启动的服务
        Log.d("batmanfloatwindow", "onReceive: "+intent.getAction());
        String action =intent.getAction();
        if("android.intent.action.BOOT_COMPLETED".equals(action)) {
            Intent service = new Intent(context, FloatWindowService.class);
            context.startService(service);
        }else if("ns.com.batmanfloatwindow.hide".equals(action)){
                FloatWindowService.isShow=false;
        }else if("ns.com.batmanfloatwindow.show".equals(action)){
            FloatWindowService.isShow=true;
        }


    }
}
