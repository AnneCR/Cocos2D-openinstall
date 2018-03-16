1.注册/登录openinstall开发者平台http://developer.openinstall.io/并创建应用

2.下载导入SDK

3.新建自定义Application类 APP继承Application并初始化App启动时，调用OpenInstall.init(context)方法完成sdk初始化
public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OpenInstall.init(this);
    }
}

4.在AppActivity中编写代码

public class AppActivity extends Cocos2dxActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // 在唤醒页面中如下调用相关代码，获取web端传过来的自定义参数
        OpenInstall.getWakeUp(getIntent(), wakeUpAdapter);

       // 在APP需要个性化安装参数时（由web网页中传递过来的，如邀请码、游戏房间号等自定义参数），
        // 调用OpenInstall.getInstall方法，在回调中获取参数（可重复获取）
        getInstall();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 此处要调用，否则App在后台运行时，会无法截获
        OpenInstall.getWakeUp(intent, wakeUpAdapter);
    }

    AppWakeUpAdapter wakeUpAdapter = new AppWakeUpAdapter() {
        @Override
        public void onWakeUp(AppData appData) {
            //获取渠道数据
            String channelCode = appData.getChannel();
            //获取绑定数据
            String bindData = appData.getData();
            Log.d("OpenInstall", "getWakeUp : wakeupData = " + appData.toString());
            Toast.makeText(AppActivity.getContext(),"wakeup:"+appData.toString(),Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeUpAdapter = null;
    }

    public void getInstall(){
        //获取OpenInstall数据
        final SharedPreferences sp = getSharedPreferences("cocosdemo", MODE_PRIVATE);
        boolean needInstall = sp.getBoolean("needInstall", true);
        if (needInstall) {  //是否需要多次调用getInstall获取参数
            OpenInstall.getInstall(new AppInstallListener() {
                @Override
                public void onInstallFinish(AppData appData, Error error) {
                    if (error == null) {
                        if (appData == null || appData.isEmpty()) return;
                        OpenInstall.reportRegister();//注册上报统计
                        Log.d("openinstall", "reportRegister success");
                        //获取自定义数据
                        Log.d("OpenInstall", "getInstall : bindData = " + appData.getData());
                        //获取渠道数据
                        Log.d("OpenInstall", "getInstall : channelCode = " + appData.getChannel());

                        //使用数据后，不想再调用，将needInstall设置为false
                        sp.edit().putBoolean("needInstall", false).apply();
                        Toast.makeText(AppActivity.getContext(), "install:" + appData.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("OpenInstall", "getInstall : errorMsg = " + error.toString());
                    }
                }
            });
        }
    }
}

5.配置AndroidManifest.xml

在AndroidManifest.xml中添加权限声明

<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

在AndroidManifest.xml的application标签内设置AppKey

<meta-data android:name="com.openinstall.APP_KEY" android:value="openinstall平台生成的APP_KEY"/>

在AndroidManifest.xml中的application标签中添加 android:name=".APP"指定自定义的Application类，以便程序启动的时候初始化自定义Application类，而不是系统默认的Application类

<activity
android:name="org.cocos2dx.cpp.AppActivity"
 android:screenOrientation="landscape"
  android:configChanges="orientation|keyboardHidden|screenSize"
   android:label="@string/app_name"
   android:theme="@android:style/Theme.NoTitleBar.Fullscreen" >
