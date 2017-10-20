package com.hxlxz.hxl.iottest;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.security.KeyStore;
import java.util.UUID;


class IOT_MQTT {
    private KeyStore clientKetStore;
    private AWSIotMqttManager mqttManager;
    private IotMqttClientStatus status;
    private boolean SubScribeStatus = false;

    public enum IotMqttClientStatus {Connecting, Connected, ConnectionLost, Reconnecting}

    IOT_MQTT(Context context) {
        String clientid = UUID.randomUUID().toString();
        mqttManager = new AWSIotMqttManager(clientid, "a3bwasu2cbypll.iot.ap-northeast-1.amazonaws.com");
        mqttManager.setKeepAlive(1000);

        String keystorePath = context.getFilesDir().getPath();
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, "CertPhone")) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias("certphone", keystorePath, "CertPhone", "CertPhonePassword")) {
                    Log.i("IOTMQTT", "Certificate \"certphone\" found in keystore - using for MQTT.");
                    clientKetStore = AWSIotKeystoreHelper.getIotKeystore("certphone", keystorePath, "CertPhone", "CertPhonePassword");
                } else
                    Log.e("IOTMQTT", "Key/cert \"certphone\" not found in keystore.");
            } else Log.e("IOTMQTT", "Keystore " + keystorePath + "/\"CertPhone\" not found.");
        } catch (Exception e) {
            Log.e("IOTMQTT", "An error occurred retrieving cert/key from keystore.", e);
        }
    }

    public void Connect(final ConnectCallBack callback) {
        try {
            mqttManager.connect(clientKetStore, new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(AWSIotMqttClientStatus awsIotMqttClientStatus, Throwable throwable) {
                    Log.d("IOTMQTT", "Status = " + String.valueOf(awsIotMqttClientStatus));
                    switch (awsIotMqttClientStatus) {
                        case Connecting:
                            status = IotMqttClientStatus.Connecting;
                            break;
                        case Connected:
                            status = IotMqttClientStatus.Connected;
                            break;
                        case ConnectionLost:
                            status = IotMqttClientStatus.ConnectionLost;
                            break;
                        case Reconnecting:
                            status = IotMqttClientStatus.Reconnecting;
                            break;
                    }
                    callback.call(status);
                }
            });
        } catch (final Exception e) {
            Log.e("IOTMQTT", "Connection error.", e);
        }
    }

    public boolean Disconnect() {
        try {
            if (status != IotMqttClientStatus.ConnectionLost)
                mqttManager.disconnect();
            SubScribeStatus = false;
            return true;
        } catch (Exception e) {
            Log.e("IOTMQTT", "Disconnect error.", e);
            return false;
        }
    }

    public boolean Publish(String Topic, String Message) {
        try {
            mqttManager.publishString(Message, Topic, AWSIotMqttQos.QOS0);
            return true;
        } catch (Exception e) {
            Log.e("IOTMQTT", "Publish error.", e);
            return false;
        }
    }

    public boolean SubScribe(String Topic, final SubScribeCallback callback) {
        try {
            mqttManager.subscribeToTopic(Topic, AWSIotMqttQos.QOS1, new AWSIotMqttNewMessageCallback() {
                @Override
                public void onMessageArrived(String topic, byte[] bytes) {
                    Log.d("IOTMQTT", "Message arrived:");
                    Log.d("IOTMQTT", "   Topic: " + topic);
                    Log.d("IOTMQTT", " Message: " + new String(bytes));
                    callback.call(topic, new String(bytes), bytes);
                }
            });
            SubScribeStatus = true;
            return true;
        } catch (Exception e) {
            Log.e("IOTMQTT", "SubScribe error.", e);
            return false;
        }
    }

    public IotMqttClientStatus GetStatus() {
        return status;
    }

    public boolean GetSubScribeStatus() {
        return SubScribeStatus;
    }
}

interface ConnectCallBack {
    public void call(IOT_MQTT.IotMqttClientStatus status);
}

interface SubScribeCallback {
    public void call(String Topic, String Message, byte[] bytes);
}