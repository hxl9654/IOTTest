package com.hxlxz.hxl.iottest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

class Sensors {
    LightSensor lightSensor;
    DistanceSensor distanceSensor;

    public class LightSensor {
        private Sensor lightSensor;
        private float value;

        LightSensor(Context context) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            SensorEventListener sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    value = event.values[0];
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        public float GetValue() {
            return value;
        }

    }

    public class DistanceSensor {
        private Sensor distanceSensor;
        private float value;

        DistanceSensor(Context context) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            distanceSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            SensorEventListener sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    value = event.values[0];
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            sensorManager.registerListener(sensorEventListener, distanceSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        public float GetValue() {
            return value;
        }
    }

    Sensors(Context context) {
        lightSensor = new LightSensor(context);
        distanceSensor = new DistanceSensor(context);
    }
}
