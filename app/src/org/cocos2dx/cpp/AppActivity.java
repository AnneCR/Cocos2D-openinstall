/****************************************************************************
Copyright (c) 2015 Chukong Technologies Inc.
 
http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package org.cocos2dx.cpp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallListener;
import com.fm.openinstall.listener.AppWakeUpAdapter;
import com.fm.openinstall.model.AppData;
import com.fm.openinstall.model.Error;
import com.lucky.hello.R;

import org.cocos2dx.lib.Cocos2dxActivity;

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
