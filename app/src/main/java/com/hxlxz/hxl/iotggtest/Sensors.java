package com.hxlxz.hxl.iotggtest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

class Sensors {
    LightSensor lightSensor;
    DistanceSensor distanceSensor;

    private class LightSensor {
        private Sensor lightSensor;

        LightSensor(Context context, final SensorCallback callback) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            SensorEventListener sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    callback.call(event.values[0]);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private class DistanceSensor {
        private Sensor distanceSensor;

        DistanceSensor(Context context, final SensorCallback callback) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            distanceSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            SensorEventListener sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    callback.call(event.values[0]);
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(sensorEventListener, distanceSensor, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    Sensors(Context context, SensorCallback LightCallBack, SensorCallback DistanceCallBack) {
        lightSensor = new LightSensor(context, LightCallBack);
        distanceSensor = new DistanceSensor(context, DistanceCallBack);
    }
}

interface SensorCallback {
    void call(float value);
}