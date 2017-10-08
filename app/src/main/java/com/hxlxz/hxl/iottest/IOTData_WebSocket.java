package com.hxlxz.hxl.iottest;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;

import java.nio.ByteBuffer;


class IOTData_WebSocket {
    AWSIotDataClient iotDataClient;
    CognitoCachingCredentialsProvider credentialsProvider;

    IOTData_WebSocket(Context context) {
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-northeast-1:de02d42c-7126-4d85-a7f8-611546099b6a", // 身份池 ID
                Regions.AP_NORTHEAST_1 // 区域
        );

        iotDataClient = new AWSIotDataClient(credentialsProvider);
        iotDataClient.setEndpoint("a3bwasu2cbypll.iot.ap-northeast-1.amazonaws.com");
    }

    public void GetShadow_Start(String key, final GetShadowCallback callback) {
        GetShadowTask getShadowTask = new GetShadowTask(callback, key);
        getShadowTask.execute();
    }

    public void UpdateShadow_Start(String key, String value, final UpdateShadowCallback callback) {
        UpdateShadowTask updateShadowTask = new UpdateShadowTask(callback, key, value);
        updateShadowTask.execute();
    }

    private class GetShadowTask extends AsyncTask<Void, Void, String> {
        GetShadowCallback callBack;
        String key;

        GetShadowTask(GetShadowCallback mCallBack, String mKey) {
            callBack = mCallBack;
            key = mKey;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                GetThingShadowRequest getThingShadowRequest = new GetThingShadowRequest().withThingName("phone");
                GetThingShadowResult getThingShadowResult = iotDataClient.getThingShadow(getThingShadowRequest);
                byte[] bytes = new byte[getThingShadowResult.getPayload().remaining()];
                getThingShadowResult.getPayload().get(bytes);
                String resultString = new String(bytes);
                return resultString;
            } catch (Exception e) {
                Log.e("IOTDataWebSocket", "getshadow", e);
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("IOTDataWebSocket", "getshadow:" + key + ":" + result);
            callBack.call(key, result);
        }
    }

    private class UpdateShadowTask extends AsyncTask<Void, Void, String> {
        UpdateShadowCallback callBack;
        String key, value;
        String state;

        UpdateShadowTask(UpdateShadowCallback mCallBack, String mKey, String mValue) {
            callBack = mCallBack;
            key = mKey;
            value = mValue;
            state = String.format("{\"state\":{\"desired\":{\"%s\":%s}}}", key, value);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                UpdateThingShadowRequest updateThingShadowRequest = new UpdateThingShadowRequest();
                updateThingShadowRequest.setThingName("phone");

                ByteBuffer payloadBuffer = ByteBuffer.wrap(state.getBytes());
                updateThingShadowRequest.setPayload(payloadBuffer);

                UpdateThingShadowResult updateThingShadowResult = iotDataClient.updateThingShadow(updateThingShadowRequest);

                byte[] bytes = new byte[updateThingShadowResult.getPayload().remaining()];
                updateThingShadowResult.getPayload().get(bytes);
                String resultString = new String(bytes);
                return resultString;
            } catch (Exception e) {
                Log.e("IOTDataWebSocket", "updateShadowTask", e);
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("IOTDataWebSocket", "updateshadow:" + key + ":" + value + "  " + result);
            callBack.call(key, value, result);
        }
    }
}

interface GetShadowCallback {
    public void call(String key, String value);
}

interface UpdateShadowCallback {
    public void call(String key, String value, String result);
}