package com.hxlxz.hxl.iottest;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    double lightSenserValue, distanceSenserValue;
    FlashLight flashLight;
    Sensors sensors;
    TextView Light;
    TextView Distance;
    private final Timer sensorTimer = new Timer();
    private TimerTask sensorTimerTask;
    IOT_MQTT MQTTClient;
    IOT_WebSockets WebSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final boolean[] ed = {false, false};
        SensorsInit();
        WebSocketClient = new IOT_WebSockets(getApplicationContext());
        MQTTClient = new IOT_MQTT(getApplicationContext());
        MQTTClient.Connect(new ConnectCallBack() {
            @Override
            public void call(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus status) {
                if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                    if (ed[0] == true)
                        return;
                    ed[0] = true;
                    MQTTClient.SubScribe("TEST", new SubScribeCallback() {
                        @Override
                        public void call(String Topic, final String Message, byte[] bytes) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    MQTTClient.Publish("TEST", "Test Message form Android MQTT Client");
                }
            }
        });

        WebSocketClient.Connect(new ConnectCallBack() {
            @Override
            public void call(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus status) {
                if (status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                    if (ed[1] == true)
                        return;
                    ed[1] = true;
                    WebSocketClient.SubScribe("TEST", new SubScribeCallback() {
                        @Override
                        public void call(String Topic, final String Message, byte[] bytes) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    WebSocketClient.Publish("TEST", "Test Message form Android WebSocket Client");
                }
            }
        });

        IOTData_WebSocket WebSocketShadowClient = new IOTData_WebSocket(getApplicationContext());
        WebSocketShadowClient.UpdateShadow_Start("light", Double.toString(lightSenserValue), new UpdateShadowCallback() {
            @Override
            public void call(final String key, final String value, final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Set " + key + ":" + value + "  " + result, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        WebSocketShadowClient.GetShadow_Start("light", new GetShadowCallback() {
            @Override
            public void call(final String key, final String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Got " + key + ":" + value, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void SensorsInit() {
        sensors = new Sensors(getApplicationContext());
        Light = (TextView) findViewById(R.id.Light);
        Distance = (TextView) findViewById(R.id.Distance);

        Light.setText("FlashLight:" + sensors.lightSensor.GetValue());
        Distance.setText("Distance:" + sensors.distanceSensor.GetValue());

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                lightSenserValue = sensors.lightSensor.GetValue();
                distanceSenserValue = sensors.distanceSensor.GetValue();
                Light.setText("FlashLight:" + lightSenserValue);
                Distance.setText("Distance:" + distanceSenserValue);
                super.handleMessage(msg);
            }
        };

        sensorTimerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        sensorTimer.schedule(sensorTimerTask, 1, 1);

        flashLight = new FlashLight();
    }

    public void onClickListener(View v) {
        if (v.getId() == R.id.LightButtom) {
            switch (flashLight.GetStatus()) {
                case ON:
                    flashLight.SetStatus(FlashLight.Status.OFF);
                    break;
                case OFF:
                    flashLight.SetStatus(FlashLight.Status.ON);
                    break;
            }
        }
        if (v.getId() == R.id.BeepButtom) {
            BeepAndVibrate();
        }
    }

    private void BeepAndVibrate() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }
}
