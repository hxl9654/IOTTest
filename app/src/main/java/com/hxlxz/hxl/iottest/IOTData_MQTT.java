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
            awsIotMqttClient = new AWSIotMqttClient("a3bwasu2cbypll.iot.ap-northeast-1.amazonaws.com", clientid, keyStore, "CertPhonePassword");

        } catch (Exception e) {
            Log.e("IOTDataMQTT", "An error occurred retrieving cert/key from keystore.", e);
        }
        phoneMQTT = new PhoneMQTT("phoneMQTT");
        try {
            awsIotMqttClient.attach(phoneMQTT);
            awsIotMqttClient.connect();
        } catch (AWSIotException e) {
            Log.e("IOTDataMQTT", "attach thing error", e);
        }
        phoneMQTT.setLight(233);
        phoneMQTT.setBeep(true);
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