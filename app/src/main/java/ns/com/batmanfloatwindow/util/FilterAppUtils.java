package ns.com.batmanfloatwindow.util;

public class FilterAppUtils {
    private static AppPkgName pkgName = new AppPkgName();
    public static boolean filter(String name){
        for (String pkg:pkgName.data){
            if(pkg.equals(name)){
                return false;
            }
        }

        return true;
    }

}

//屏蔽包名
class AppPkgName{
String[] data =new String[]{
        "com.android.launcher",
        "com.lkmotion.entertainmentbase",
        "com.kandi.nscarlauncher",
        "ns.com.batmanfloatwindow",
        "com.kandi.systemui"
};
}
