package ns.com.batmanfloatwindow.app;


import android.app.Application;


/**
 * Created by z on 2018/4/10.
 */

public class App extends Application {

    private static App s_app;

    public static App get() {
        return s_app;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        s_app = this;


    }


}
