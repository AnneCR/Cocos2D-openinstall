有这么一个场景，甲给乙分享了链接，乙使用并下载APP，推荐者甲和乙的关系这个思路是怎样的？

你首先想到的也许会说，那当然就是给对方一个邀请码去辨识啊。

1,扫码下载，二维码里面有甲的推荐邀请号,

2,APP下载包在本服务器上下载，通过地址也是可以知道甲的邀请信息，

3,在APP下载输入甲的邀请码 。
没错，这个是可以实现的，但是太传统，用户转化率严重低下。

 最近在使用一个叫openinstall的SDK，openisntall就可以解决这个场景，通过它实现免填邀请码的功能，集成到Cocos2d-x游戏开发中。对App安装流程的优化，尤其是免填写邀请码安装，App推广的有奖邀请活动更大程度的达到推广爆炸式的效果。

在分享链接自定义各种动态参数（如推广渠道号，邀请码，游戏房间号，用户id等等）。通过在分享链接url中附带app邀请人的用户id，就可达到免填邀请码的效果;或者app通过在url中附带游戏房间号实现直达游戏房间也可建立上下级关系；Cocos2d-x开发中免不了邀请用户获得奖励；新老用户直达游戏场景；从各种浏览器一键拉起游戏场景等等；那么如何在Cocos2d集成openinstall呢？

1.注册/登录openinstall开发者平台http://developer.openinstall.io/并创建应用

2.下载导入SDK

3.新建自定义Application类 APP继承Application并初始化App启动时，调用`OpenInstall.init(context)`方法完成sdk初始化
``` java
public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OpenInstall.init(this);
    }
}
```

4.在AppActivity中编写代码

``` java

public class AppActivity extends Cocos2dxActivity {

     Button effercPoint;
     Button install;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        effercPoint= (Button)findViewById(R.id.effectPoint);
        install = (Button) findViewById(R.id.install);
        effercPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportEffectPoint("effect_test",100);
            }
        });

        install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInstall();
            }
        });


        // 在唤醒页面中如下调用相关代码，获取web端传过来的自定义参数
        OpenInstall.getWakeUp(getIntent(), wakeUpAdapter);
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


    /**
     * 安装来源追踪（无码邀请，加入游戏房间，）
     */
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

    /**
     * 上报效果点统计
     * @param effectId
     * @param effectValue
     */
    public  void reportEffectPoint(String effectId,long effectValue){
        OpenInstall.reportEffectPoint(effectId,effectValue);
        Toast.makeText(AppActivity.getContext(),"上报成功，可能延迟几秒",Toast.LENGTH_SHORT).show();
    }

}

```

5.配置AndroidManifest.xml

在AndroidManifest.xml中添加权限声明
``` java
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

在AndroidManifest.xml的application标签内设置AppKey

``` java
<meta-data android:name="com.openinstall.APP_KEY" android:value="openinstall平台生成的APP_KEY"/>
```


在AndroidManifest.xml中的application标签中添加 `android:name=".APP"` 指定自定义的Application类，以便程序启动的时候初始化自定义Application类，而不是系统默认的Application类

``` java
<activity
android:name="org.cocos2dx.cpp.AppActivity"
 android:screenOrientation="landscape"
  android:configChanges="orientation|keyboardHidden|screenSize"
   android:label="@string/app_name"
   android:theme="@android:style/Theme.NoTitleBar.Fullscreen" >
 ```

最后打安装包上传到openinstall后台测试完毕。
