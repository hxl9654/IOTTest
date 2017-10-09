package com.hxlxz.hxl.iottest;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;

import java.util.UUID;


class IOT_WebSockets {
    private AWSIotMqttManager mqttManager;
    private AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus status;
    CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mqttManager.disconnect();
    }

    IOT_WebSockets(Context context) {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-northeast-1:de02d42c-7126-4d85-a7f8-611546099b6a", // 身份池 ID
                Regions.AP_NORTHEAST_1 // 区域
        );
        String clientid = UUID.randomUUID().toString();
        mqttManager = new AWSIotMqttManager(clientid, "a3bwasu2cbypll.iot.ap-northeast-1.amazonaws.com");
        mqttManager.setKeepAlive(1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                credentialsProvider.getCredentials();
            }
        }).start();
    }

    public void Connect(final ConnectCallBack callback) {
        try {
            mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus awsIotMqttClientStatus, Throwable throwable) {
                    Log.d("IOTWebSockets", "Status = " + String.valueOf(awsIotMqttClientStatus));
                    status = awsIotMqttClientStatus;
                    callback.call(awsIotMqttClientStatus);
                }
            });
        } catch (final Exception e) {
            Log.e("IOTWebSockets", "Connection error.", e);
        }
    }

    public boolean Disconnect() {
        try {
            if (status != AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost)
                mqttManager.disconnect();
            return true;
        } catch (Exception e) {
            Log.e("IOTWebSockets", "Disconnect error.", e);
            return false;
        }
    }

    public boolean Publish(String Topic, String Message) {
        try {
            mqttManager.publishString(Message, Topic, AWSIotMqttQos.QOS0);
            return true;
        } catch (Exception e) {
            Log.e("IOTWebSockets", "Publish error.", e);
            return false;
        }
    }

    public boolean SubScribe(String Topic, final SubScribeCallback callback) {
        try {
            mqttManager.subscribeToTopic(Topic, AWSIotMqttQos.QOS1, new AWSIotMqttNewMessageCallback() {
                @Override
                public void onMessageArrived(String topic, byte[] bytes) {
                    Log.d("IOTWebSockets", "Message arrived:");
                    Log.d("IOTWebSockets", "   Topic: " + topic);
                    Log.d("IOTWebSockets", " Message: " + new String(bytes));
                    callback.call(topic, new String(bytes), bytes);
                }
            });
            return true;
        } catch (Exception e) {
            Log.e("IOTWebSockets", "SubScribe error.", e);
            return false;
        }
    }
}
