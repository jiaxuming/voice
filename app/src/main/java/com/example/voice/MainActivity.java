package com.example.voice;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.media.MediaRecorder;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int UPDATE_INTERVAL = 1000; // 每秒更新时间间隔 (ms)

    private MediaRecorder mRecorder;
    private Handler mHandler;
    private TextView mTextView;
    private Button mButton;
    private static final int PERMISSIONS_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textView);
        mButton = findViewById(R.id.button);
        mHandler = new Handler();
        mHandler.post(mUpdateMicStatus);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRecorder == null) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        // 请求录音权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            Toast.makeText(this,"录音权限开启",Toast.LENGTH_SHORT).show();
        }
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        File audioFile = new File(getExternalFilesDir(null), "audio.3gp");
        mRecorder.setOutputFile(audioFile.getAbsolutePath());

        try {
            mRecorder.prepare();
            mRecorder.start();
            mButton.setText("停止检测");
            mTextView.setText("请稍等...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (IllegalStateException e) {
                // 处理 IllegalStateException 异常
                e.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;

            // 更新按钮文字以及启用/禁用状态
            mButton.setText("Start Recording");
            mButton.setEnabled(true);
        }
    }

    private Runnable mUpdateMicStatus = new Runnable() {
        @Override
        public void run() {
            updateMicStatus();
            mHandler.postDelayed(mUpdateMicStatus, UPDATE_INTERVAL);
        }
    };

    private void updateMicStatus() {
        if (mRecorder == null) {
            return;
        }

        int maxAmplitude = 0;
        try {
            // 检查MediaRecorder是否正在录音状态
            if (mRecorder != null && mRecorder.getAudioSourceMax() == MediaRecorder.AudioSource.MIC && mRecorder.getMaxAmplitude() != 0) {
                // 如果正在录制，获取最大振幅
                Toast.makeText(this,"正在录制，获取最大振幅",Toast.LENGTH_SHORT).show();
                maxAmplitude = mRecorder.getMaxAmplitude();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        // 计算分贝值
        double amplitudeDb = 20 * Math.log10((double) Math.abs(maxAmplitude) / 32767);
        Toast.makeText(this,"分贝数"+String.valueOf(maxAmplitude),Toast.LENGTH_SHORT).show();
        String sleepStatus = "";
        if (amplitudeDb < 3) {
            sleepStatus = "浅睡眠状态";
        } else if (amplitudeDb > 3 && amplitudeDb < 100) {
            sleepStatus = "深睡眠状态";
        } else {
            sleepStatus = "清醒状态";
        }

        if (!TextUtils.equals(mTextView.getText(), sleepStatus)) {
            mTextView.setText(sleepStatus);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateMicStatus);

        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied, disable the functionality that depends on this permission.
                }
                break;
        }
    }
}
