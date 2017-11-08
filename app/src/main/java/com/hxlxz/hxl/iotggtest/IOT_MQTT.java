package com.hxlxz.hxl.iotggtest;

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
        mqttManager = new AWSIotMqttManager(clientid, context.getResources().getString(R.string.EndPoint));
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

    void Connect(final ConnectCallBack callback) {
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

    void Disconnect() {
        try {
            if (status != IotMqttClientStatus.ConnectionLost)
                mqttManager.disconnect();
            SubScribeStatus = false;
        } catch (Exception e) {
            Log.e("IOTMQTT", "Disconnect error.", e);
        }
    }

    void Publish(String Topic, String Message) {
        try {
            mqttManager.publishString(Message, Topic, AWSIotMqttQos.QOS0);
        } catch (Exception e) {
            Log.e("IOTMQTT", "Publish error.", e);
        }
    }

    void SubScribe(String Topic, final SubScribeCallback callback) {
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
        } catch (Exception e) {
            Log.e("IOTMQTT", "SubScribe error.", e);
        }
    }

    IotMqttClientStatus GetStatus() {
        return status;
    }

    boolean GetSubScribeStatus() {
        return SubScribeStatus;
    }
}

interface ConnectCallBack {
    void call(IOT_MQTT.IotMqttClientStatus status);
}

interface SubScribeCallback {
    void call(String Topic, String Message, byte[] bytes);
}