package com.jkwu.wsforever.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import com.jkwu.wsforever.MainActivity;
import com.jkwu.wsforever.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

public class SendWeatherSMS extends AppCompatActivity {
    FloatingActionButton fab = null;
    EditText sendSmsPhoneNumber = null;
    EditText sendSmsPhoneContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_weather_sms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initWeather();
    }

    public void sendCode() {
        RegisterPage page = new RegisterPage();
        page.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    // 处理成功的结果
                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                    String country = (String) phoneMap.get("country"); // 国家代码，如“86”
                    String phone = (String) phoneMap.get("phone"); // 手机号码，如“13800138000”
                    // TODO 利用国家代码和手机号码进行后续的操作
                } else{
                    // TODO 处理错误的结果
                }
            }
        });
        page.show(this);
    }

    private void initWeather() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        sendSmsPhoneNumber = (EditText) findViewById(R.id.send_sms_phone_number);
        sendSmsPhoneContent = (EditText) findViewById(R.id.send_sms_phone_content);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                sendCode();
                // tencent
//                try {
//                    int appid = 1400174692;
//                    String appkey = "83423bbecbe39d00bf5cd3abfacc79c9";
//                    SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
//                    String phoneNumber = sendSmsPhoneNumber.getText().toString();
//                    String content = sendSmsPhoneContent.getText().toString();
//                    if (phoneNumber != null && content != null) {
//                        String[] params = {"5678"};
//                        //数组具体的元素个数和模板中变量个数必须一致，例如事例中templateId:5678对应一个变量，参数数组中元素个数也必须是一个
//                        SmsSingleSenderResult result = ssender.sendWithParam("86", phoneNumber,
//                                5678, params, null, "", "");  // 签名参数未提供或者为空时，会使用默认签名发送短信
//                        System.out.println(result);
//                    }
//
//                } catch (HTTPException e) {
//                    // HTTP响应码错误
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    // json解析错误
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    // 网络IO错误
//                    e.printStackTrace();
//                }
                // tencent
            }
        });
    }

}
