package com.jkwu.wsforever.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baidu.speech.VoiceRecognitionService;
import com.baidu.speech.asr.SpeechConstant;
import com.jkwu.wsforever.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RecognitionListenerDialog extends AppCompatActivity implements RecognitionListener {
    private static final String TAG ="MainActivity" ;
    private SpeechRecognizer speechRecognizer;
//    private BaiduASRDigitalDialog dialog;

    public static final int STATUS_None = 0;
    public static final int STATUS_WaitingReady = 2;
    public static final int STATUS_Ready = 3;
    public static final int STATUS_Speaking = 4;
    public static final int STATUS_Recognition = 5;
    private Button setting,start;
    private TextView result,txtLog;
    private long time;
    private long speechEndTime=-1;
    private static final int REQUEST_UI=1;
    private int status = STATUS_None;
    private static final int EVENT_ERROR = 11;





    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recognition_listener_dialog);

//        setting= (Button) findViewById(R.id.setting);
        start= (Button) findViewById(R.id.start);
        result= (TextView) findViewById(R.id.result);
//        txtLog= (TextView) findViewById(R.id.txtLog);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

//        setting= (Button) findViewById(R.id.setting);
        start= (Button) findViewById(R.id.start);
        result= (TextView) findViewById(R.id.result);
//        txtLog= (TextView) findViewById(R.id.txtLog);
        // 创建识别器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        }
        // 注册监听器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            speechRecognizer.setRecognitionListener(this);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    //设置界面
    public void setTing(View view){
        Intent intent = new Intent("com.baidu.speech.asr.demo.setting");
        startActivity(intent);
    }
    //开始
    public void start(View view){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean api = sp.getBoolean("api", false);
        if (api) {


            switch (status) {
                case STATUS_None:
                    startASR();
                    start.setText("取消");
                    status = STATUS_WaitingReady;
                    break;
                case STATUS_WaitingReady:
                    cancel();
                    status = STATUS_None;
                    start.setText("开始");
                    break;
                case STATUS_Ready:
                    cancel();
                    status = STATUS_None;
                    start.setText("开始");
                    break;
                case STATUS_Speaking:
                    stop();
                    status = STATUS_Recognition;
                    start.setText("识别中");
                    break;
                case STATUS_Recognition:
                    cancel();
                    status = STATUS_None;
                    start.setText("开始");
                    break;
            }
        } else {
            startASR();
        }
    }

    private void stop() {
        speechRecognizer.stopListening();
        print("点击了“说完了”");
    }

    private void cancel() {
        speechRecognizer.cancel();
        status = STATUS_None;
        print("点击了“取消”");
    }
    // 开始识别
    void startASR() {
        // txtLog.setText("");
        print("点击了“开始”");
        Intent intent = new Intent();
        bindParams(intent);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        {

            String args = sp.getString("args", "");
            if (null != args) {
                print("参数集：" + args);
                intent.putExtra("args", args);
            }
        }
        boolean api = sp.getBoolean("api", false);
        if (api) {
            speechEndTime = -1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                speechRecognizer.startListening(intent);
            }
        } else {
            intent.setAction("com.baidu.action.RECOGNIZE_SPEECH");
            startActivityForResult(intent, REQUEST_UI);

        }

        // result.setText("");



    }
    void bindParams(Intent intent) {
        // 设置识别参数
        //       intent.putExtra("sample", 16000); // 离线仅支持16000采样率
        //      intent.putExtra("language", "cmn-Hans-CN"); // 离线仅支持中文普通话
//        intent.putExtra("prop", 20000); // 输入
//        intent.putExtra("prop", 10060); // 地图
//        intent.putExtra("prop", 10001); // 音乐
//        intent.putExtra("prop", 10003); // 应用
//        intent.putExtra("prop", 10008); // 电话
//        intent.putExtra("prop", 100014); // 联系人
//        intent.putExtra("prop", 100016); // 手机设置
//        intent.putExtra("prop", 100018); // 电视指令
//        intent.putExtra("prop", 100019); // 播放器指令
//        intent.putExtra("prop", 100020); // 收音机指令
//        intent.putExtra("prop", 100021); // 命令词


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        if (sp.getBoolean("tips_sound", true)) {
//            intent.putExtra(Constant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
//            intent.putExtra(Constant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
//            intent.putExtra(Constant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
//            intent.putExtra(Constant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
//            intent.putExtra(Constant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
//        }
//        if (sp.contains(Constant.EXTRA_INFILE)) {
//            String tmp = sp.getString(Constant.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
//            intent.putExtra(Constant.EXTRA_INFILE, tmp);
//        }
//        if (sp.getBoolean(Constant.EXTRA_OUTFILE, false)) {
//            intent.putExtra(Constant.EXTRA_OUTFILE, "sdcard/outfile.pcm");
//        }
//        if (sp.getBoolean(Constant.EXTRA_GRAMMAR, false)) {
//            intent.putExtra(Constant.EXTRA_GRAMMAR, "assets:///baidu_speech_grammar.bsg");
//        }
//        if (sp.contains(Constant.EXTRA_SAMPLE)) {
//            String tmp = sp.getString(Constant.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
//            if (null != tmp && !"".equals(tmp)) {
//                intent.putExtra(Constant.EXTRA_SAMPLE, Integer.parseInt(tmp));
//            }
//        }
//        if (sp.contains(Constant.EXTRA_LANGUAGE)) {
//            String tmp = sp.getString(Constant.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
//            if (null != tmp && !"".equals(tmp)) {
//                intent.putExtra(Constant.EXTRA_LANGUAGE, tmp);
//            }
//        }
//        if (sp.contains(Constant.EXTRA_NLU)) {
//            String tmp = sp.getString(Constant.EXTRA_NLU, "").replaceAll(",.*", "").trim();
//            if (null != tmp && !"".equals(tmp)) {
//                intent.putExtra(Constant.EXTRA_NLU, tmp);
//            }
//        }
//
//        if (sp.contains(Constant.EXTRA_VAD)) {
//            String tmp = sp.getString(Constant.EXTRA_VAD, "").replaceAll(",.*", "").trim();
//            if (null != tmp && !"".equals(tmp)) {
//                intent.putExtra(Constant.EXTRA_VAD, tmp);
//            }
//        }
//        String prop = null;
//        if (sp.contains(Constant.EXTRA_PROP)) {
//            String tmp = sp.getString(Constant.EXTRA_PROP, "").replaceAll(",.*", "").trim();
//            if (null != tmp && !"".equals(tmp)) {
//                intent.putExtra(Constant.EXTRA_PROP, Integer.parseInt(tmp));
//                prop = tmp;
//            }
//        }
//
//        // offline asr
//        {
//            intent.putExtra(Constant.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
//            if (null != prop) {
//                int propInt = Integer.parseInt(prop);
//                if (propInt == 10060) {
//                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_Navi");
//                } else if (propInt == 20000) {
//                    intent.putExtra(Constant.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
//                }
//            }
//            intent.putExtra(Constant.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
//        }
    }
    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        JSONArray name = new JSONArray().put("李涌泉").put("郭下纶");
        JSONArray song = new JSONArray().put("七里香").put("发如雪");
        JSONArray artist = new JSONArray().put("周杰伦").put("李世龙");
        JSONArray app = new JSONArray().put("手机百度").put("百度地图");
        JSONArray usercommand = new JSONArray().put("关灯").put("开门");
//        try {
//            slotData.put(SpeechConstant.EXTRA_OFFLINE_SLOT_NAME, name);
//            slotData.put(Constant.EXTRA_OFFLINE_SLOT_SONG, song);
//            slotData.put(Constant.EXTRA_OFFLINE_SLOT_ARTIST, artist);
//            slotData.put(Constant.EXTRA_OFFLINE_SLOT_APP, app);
//            slotData.put(Constant.EXTRA_OFFLINE_SLOT_USERCOMMAND, usercommand);
//        } catch (JSONException e) {
//        }
        return slotData.toString();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        // 准备就绪
        print("准备就绪，可以开始说话");

    }

    @Override
    public void onBeginningOfSpeech() {
        // 开始说话处理
        time = System.currentTimeMillis();
        status = STATUS_Speaking;
        start.setText("说完了");
        print("检测到用户的已经开始说话");



    }

    @Override
    public void onRmsChanged(float v) {
        // 音量变化处理

    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        // 录音数据传出处理

    }

    @Override
    public void onEndOfSpeech() {
        // 说话结束处理
        speechEndTime = System.currentTimeMillis();
        status = STATUS_Recognition;
        print("检测到用户的已经停止说话");
        start.setText("识别中");
    }

    @Override
    public void onError(int i) {
        // 出错处理
        time = 0;
        StringBuilder sb = new StringBuilder();
        switch (i) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }
        sb.append(":" + i);
        print("识别失败：" + sb.toString());
        start.setText("开始");

    }

    @Override
    public void onResults(Bundle bundle) {
        // 最终结果处理
        long end2finish = System.currentTimeMillis() - speechEndTime;
        status = STATUS_None;
        ArrayList<String> nbest = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        print("识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
        String json_res = bundle.getString("origin_result");
        try {
            print("origin_result=\n" + new JSONObject(json_res).toString(4));
        } catch (Exception e) {
            print("origin_result=[warning: bad json]\n" + json_res);
        }
        start.setText("开始");
        String strEnd2Finish = "";
        if (end2finish < 60 * 1000) {
            strEnd2Finish = "(waited " + end2finish + "ms)";
        }
        result.setText(nbest.get(0) + strEnd2Finish);
        time = 0;

    }

    @Override
    public void onPartialResults(Bundle bundle) {
        // 临时结果处理
        ArrayList<String> nbest = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (nbest.size() > 0) {
            print("~临时识别结果：" + Arrays.toString(nbest.toArray(new String[0])));
            result.setText(nbest.get(0));
        }
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        // 处理事件回调

        switch (i) {
            case EVENT_ERROR:
                String reason = bundle.get("reason") + "";
                print("EVENT_ERROR, " + reason);
                break;
            case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                int type = bundle.getInt("engine_type");
                print("*引擎切换至" + (type == 0 ? "在线" : "离线"));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            onResults(data.getExtras());
        }
    }

    private void print(String msg) {

        long t = System.currentTimeMillis() - time;
        if (t > 0 && t < 100000) {
            txtLog.append(t + "ms, " + msg + "\n");
        } else {
            txtLog.append("" + msg + "\n");
        }
        ScrollView sv = (ScrollView) txtLog.getParent();
        sv.smoothScrollTo(0, 1000000);
        Log.d("Tag", "----" + msg);
    }
}
