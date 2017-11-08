package com.hxlxz.hxl.iotggtest;

import android.content.Context;
import android.util.Log;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;

import java.security.KeyStore;
import java.util.UUID;

class IOTData_MQTT {
    private AWSIotDevice awsIotDevice1, awsIotDevice2;
    private AWSIotMqttClient awsIotMqttClient1, awsIotMqttClient2;


    IOTData_MQTT(Context context, AWSIotDevice object, String thingName) {
        String clientid1 = UUID.randomUUID().toString().substring(0, 20);
        String clientid2 = UUID.randomUUID().toString().substring(0, 20);
        Log.d("IOTDataMQTT", "ClientID1:" + clientid1);
        Log.d("IOTDataMQTT", "ClientID2:" + clientid2);
        try {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(context.openFileInput("CertPhone"), "CertPhonePassword".toCharArray());
            Log.d("IOTDataMQTT", "using keystore CertPhone.");
            awsIotMqttClient1 = new AWSIotMqttClient(context.getResources().getString(R.string.EndPoint), clientid1, keyStore, "CertPhonePassword");
            awsIotMqttClient2 = new AWSIotMqttClient(context.getResources().getString(R.string.EndPoint), clientid2, keyStore, "CertPhonePassword");
            Log.d("IOTDataMQTT", "AWSIotMqttClient inited");
        } catch (Exception e) {
            Log.e("IOTDataMQTT", "An error occurred retrieving cert/key from keystore.", e);
        }
        awsIotDevice1 = object;
        try {
            awsIotDevice2 = new AWSIotDevice(thingName);
            awsIotMqttClient1.attach(awsIotDevice1);
            Log.d("IOTDataMQTT", "awsIotDevice1 attached");
            awsIotMqttClient2.attach(awsIotDevice2);
            Log.d("IOTDataMQTT", "awsIotDevice2 attached");
            awsIotMqttClient1.connect();
            Log.d("IOTDataMQTT", "AWSIotMqttClient1 connected");
            awsIotMqttClient2.connect();
            Log.d("IOTDataMQTT", "AWSIotMqttClient2 connected");
        } catch (AWSIotException e) {
            Log.e("IOTDataMQTT", "attach thing error", e);
        }
        Log.d("IOTDataMQTT", "MQTTShadowClient1 created.");
    }

    String getJSON() {
        try {
            return awsIotDevice2.get();
        } catch (AWSIotException e) {
            e.printStackTrace();
            return null;
        }
    }

    void setJSON(String JSON) {
        try {
            awsIotDevice2.update(JSON);
        } catch (AWSIotException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() {
        try {
            super.finalize();

            if (awsIotMqttClient1 != null && awsIotMqttClient1.getConnectionStatus() == AWSIotConnectionStatus.CONNECTED) {
                awsIotMqttClient1.disconnect();
                if (awsIotMqttClient1.getDevices().get("phone") != null)
                    awsIotMqttClient1.detach(awsIotDevice1);
            }
            if (awsIotMqttClient2 != null && awsIotMqttClient2.getConnectionStatus() == AWSIotConnectionStatus.CONNECTED) {
                awsIotMqttClient2.disconnect();
                if (awsIotMqttClient2.getDevices().get("phone") != null)
                    awsIotMqttClient2.detach(awsIotDevice1);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}