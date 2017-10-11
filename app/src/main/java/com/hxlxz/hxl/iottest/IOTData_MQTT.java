package com.hxlxz.hxl.iottest;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;

import java.security.KeyStore;
import java.util.UUID;

class IOTData_MQTT {
    private AWSIotMqttClient awsIotMqttClient;
    public PhoneMQTT phoneMQTT;

    IOTData_MQTT(Context context) {
        String clientid = UUID.randomUUID().toString().substring(0, 20);
        Log.d("IOTDataMQTT", "ClientID:" + clientid);

        try {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(context.openFileInput("CertPhone"), "CertPhonePassword".toCharArray());
            Log.d("IOTDataMQTT", "using keystore CertPhone.");
            awsIotMqttClient = new AWSIotMqttClient("a3bwasu2cbypll.iot.ap-northeast-1.amazonaws.com", clientid, keyStore, "CertPhonePassword");
            Log.d("IOTDataMQTT", "AWSIotMqttClient inited");
        } catch (Exception e) {
            Log.e("IOTDataMQTT", "An error occurred retrieving cert/key from keystore.", e);
        }
        phoneMQTT = new PhoneMQTT("phoneMQTT");
        try {
            awsIotMqttClient.attach(phoneMQTT);
            Log.d("IOTDataMQTT", "AWSIotMqttClient attached");
            awsIotMqttClient.connect();
            Log.d("IOTDataMQTT", "AWSIotMqttClient connected");
        } catch (AWSIotException e) {
            Log.e("IOTDataMQTT", "attach thing error", e);
        }
        Log.d("IOTDataMQTT", "MQTTShadowClient created.");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (awsIotMqttClient != null && awsIotMqttClient.getConnectionStatus() == AWSIotConnectionStatus.CONNECTED) {
            awsIotMqttClient.disconnect();
            if (awsIotMqttClient.getDevices().get("phone") != null)
                awsIotMqttClient.detach(phoneMQTT);
        }
    }
}