package com.hxlxz.hxl.iottest;

import android.content.Context;
import android.util.Log;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.KeyStore;
import java.util.UUID;

class IOTData_MQTT {
    public PhoneMQTT phoneMQTT;
    private AWSIotMqttClient awsIotMqttClient1, awsIotMqttClient2;
    private AWSIotDevice awsIotDevice;
    private ObjectMapper objectMapper = new ObjectMapper();


    IOTData_MQTT(Context context) {
        String clientid1 = UUID.randomUUID().toString().substring(0, 20);
        String clientid2 = UUID.randomUUID().toString().substring(0, 20);
        Log.d("IOTDataMQTT", "ClientID1:" + clientid1);
        Log.d("IOTDataMQTT", "ClientID2:" + clientid2);
        try {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(context.openFileInput("CertPhone"), "CertPhonePassword".toCharArray());
            Log.d("IOTDataMQTT", "using keystore CertPhone.");
            awsIotMqttClient1 = new AWSIotMqttClient("a3bwasu2cbypll.iot.ap-northeast-1.amazonaws.com", clientid1, keyStore, "CertPhonePassword");
            awsIotMqttClient2 = new AWSIotMqttClient("a3bwasu2cbypll.iot.ap-northeast-1.amazonaws.com", clientid2, keyStore, "CertPhonePassword");
            Log.d("IOTDataMQTT", "AWSIotMqttClient inited");
        } catch (Exception e) {
            Log.e("IOTDataMQTT", "An error occurred retrieving cert/key from keystore.", e);
        }
        phoneMQTT = new PhoneMQTT("phone");
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            awsIotDevice = new AWSIotDevice("phone");
            awsIotMqttClient1.attach(awsIotDevice);
            Log.d("IOTDataMQTT", "awsIotDevice attached");
            awsIotMqttClient2.attach(phoneMQTT);
            Log.d("IOTDataMQTT", "AWSIotMqttClient attached");
            awsIotMqttClient1.connect();
            Log.d("IOTDataMQTT", "AWSIotMqttClient1 connected");
            awsIotMqttClient2.connect();
            Log.d("IOTDataMQTT", "AWSIotMqttClient2 connected");
        } catch (AWSIotException e) {
            Log.e("IOTDataMQTT", "attach thing error", e);
        }
        Log.d("IOTDataMQTT", "MQTTShadowClient created.");
    }

    public PhoneJSON getPhoneJSON() {
        PhoneJSON phoneJSON = new PhoneJSON();
        String shadowState;
        try {
            shadowState = awsIotDevice.get();
            phoneJSON = objectMapper.readValue(shadowState, PhoneJSON.class);
            return phoneJSON;
        } catch (AWSIotException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setPhoneJSON(PhoneJSON phoneJSON) {
        try {
            String jsonState = objectMapper.writeValueAsString(phoneJSON);
            awsIotDevice.update(jsonState);
        } catch (JsonProcessingException | AWSIotException e) {
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
                    awsIotMqttClient1.detach(phoneMQTT);
            }
            if (awsIotMqttClient2 != null && awsIotMqttClient2.getConnectionStatus() == AWSIotConnectionStatus.CONNECTED) {
                awsIotMqttClient2.disconnect();
                if (awsIotMqttClient2.getDevices().get("phone") != null)
                    awsIotMqttClient2.detach(phoneMQTT);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}