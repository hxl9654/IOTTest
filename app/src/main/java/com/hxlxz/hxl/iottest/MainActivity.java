package com.hxlxz.hxl.iottest;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;

import com.hxlxz.hxl.iottest.IOT_MQTT.IotMqttClientStatus;

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
    IOTData_WebSocket WebSocketShadowClient;
    IOTData_MQTT MQTTShadowClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("IOT", "MainActivity.onCreate-start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorsInit();
        WebSocketClient = new IOT_WebSockets(getApplicationContext());
        MQTTClient = new IOT_MQTT(getApplicationContext());

        Log.d("IOT", "MainActivity.onCreate-end");
    }

    private void SensorsInit() {
        sensors = new Sensors(getApplicationContext());
        Light = (TextView) findViewById(R.id.Light);
        Distance = (TextView) findViewById(R.id.Distance);

        Light.setText("Light:" + sensors.lightSensor.GetValue());
        Distance.setText("Distance:" + sensors.distanceSensor.GetValue());

        sensorTimerTask = new TimerTask() {
            @Override
            public void run() {
                lightSenserValue = sensors.lightSensor.GetValue();
                distanceSenserValue = sensors.distanceSensor.GetValue();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Light.setText("Light:" + lightSenserValue);
                        Distance.setText("Distance:" + distanceSenserValue);
                    }
                });
                if (MQTTShadowClient != null) {
                    final PhoneJSON phoneJSON = MQTTShadowClient.getPhoneJSON();
                    if (phoneJSON != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MQTTShadowClient.phoneMQTT.setLight(lightSenserValue);
                                if (MQTTShadowClient.phoneMQTT.getBeepDesired() == true)
                                    BeepAndVibrate();
                                if (phoneJSON.state.reported.beep)
                                    ((TextView) findViewById(R.id.MQTTBeepStatus)).setText("Beep:T");
                                else
                                    ((TextView) findViewById(R.id.MQTTBeepStatus)).setText("Beep:F");

                                if (phoneJSON.state.reported.flashLight)
                                    ((TextView) findViewById(R.id.MQTTFlashStatus)).setText("Flash:T");
                                else
                                    ((TextView) findViewById(R.id.MQTTFlashStatus)).setText("Flash:F");

                                ((TextView) findViewById(R.id.MQTTDistanceStatus)).setText("Distance:" + phoneJSON.state.reported.distance);
                                ((TextView) findViewById(R.id.MQTTLightStatus)).setText("Light:" + phoneJSON.state.reported.light);
                            }
                        });
                    }
                }
                if (WebSocketShadowClient != null) {
                    WebSocketShadowClient.UpdateShadow_Start("distance", Double.toString(distanceSenserValue), new UpdateShadowCallback() {
                        @Override
                        public void call(final String key, final String value, final String result) {

                        }
                    });

                    WebSocketShadowClient.GetShadow_Start(new GetShadowCallback() {
                        @Override
                        public void call(final String value) {
                            Gson gson = new Gson();
                            final PhoneJSON phoneJSON = gson.fromJson(value, PhoneJSON.class);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (phoneJSON.state.desired.flashLight == true)
                                        flashLight.SetStatus(FlashLight.Status.ON, WebSocketShadowClient);
                                    else
                                        flashLight.SetStatus(FlashLight.Status.OFF, WebSocketShadowClient);

                                    ((TextView) findViewById(R.id.WebSocketLightStatus)).setText("Light:" + phoneJSON.state.reported.light);
                                    if (phoneJSON.state.reported.beep == true)
                                        ((TextView) findViewById(R.id.WebSocketBeepStatus)).setText("Beep:T");
                                    else
                                        ((TextView) findViewById(R.id.WebSocketBeepStatus)).setText("Beep:F");

                                    ((TextView) findViewById(R.id.WebSocketDistanceStatus)).setText("Distance:" + phoneJSON.state.reported.distance);

                                    if (phoneJSON.state.reported.flashLight == true)
                                        ((TextView) findViewById(R.id.WebSocketFlashStatus)).setText("Flash:T");
                                    else
                                        ((TextView) findViewById(R.id.WebSocketFlashStatus)).setText("Flash:F");
                                }
                            });
                        }
                    });
                }
            }
        };

        sensorTimer.schedule(sensorTimerTask, 1000, 1000);
        flashLight = new FlashLight();
    }

    public void onClickListener(View v) {
        if (v.getId() == R.id.LightButtom) {
            switch (flashLight.GetStatus()) {
                case ON:
                    WebSocketShadowClient.UpdateShadow_Start("flashLight", Boolean.toString(false), new UpdateShadowCallback() {
                        @Override
                        public void call(final String key, final String value, final String result) {

                        }
                    }, "desired");
                    break;
                case OFF:
                    WebSocketShadowClient.UpdateShadow_Start("flashLight", Boolean.toString(true), new UpdateShadowCallback() {
                        @Override
                        public void call(final String key, final String value, final String result) {

                        }
                    }, "desired");
                    break;
            }
        } else if (v.getId() == R.id.BeepButtom) {
            BeepAndVibrate();
        } else if (v.getId() == R.id.MQTTConnect) {
            if (MQTTClient.GetStatus() != IotMqttClientStatus.Connected) {
                if (MQTTShadowClient == null)
                    MQTTShadowClient = new IOTData_MQTT(getApplicationContext());
                MQTTClient.Connect(new ConnectCallBack() {
                    @Override
                    public void call(IotMqttClientStatus status) {
                        if (status == IotMqttClientStatus.Connected) {
                            if (MQTTClient.GetSubScribeStatus())
                                return;
                            MQTTClient.SubScribe("TEST", new SubScribeCallback() {
                                @Override
                                public void call(String Topic, final String Message, byte[] bytes) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((EditText) findViewById(R.id.MQTTMsg)).setText(Message);
                                        }
                                    });
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.MQTTStatus)).setText("MQTT:Connected");
                                    ((Button) findViewById(R.id.MQTTConnect)).setText("MQTT Disconnect");
                                    ((Button) findViewById(R.id.MQTTTestMsg)).setEnabled(true);
                                }
                            });
                        }
                    }
                });
            } else {
                MQTTClient.Disconnect();
                MQTTShadowClient.finalize();
                MQTTShadowClient = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.MQTTStatus)).setText("MQTT:Disconnected");
                        ((Button) findViewById(R.id.MQTTConnect)).setText("MQTT Connect");
                        ((Button) findViewById(R.id.MQTTTestMsg)).setEnabled(false);
                    }
                });
            }
        } else if (v.getId() == R.id.WebSocketConnect) {
            if (WebSocketClient.GetStatus() != IotMqttClientStatus.Connected) {
                if (WebSocketShadowClient == null)
                    WebSocketShadowClient = new IOTData_WebSocket(getApplicationContext());
                WebSocketClient.Connect(new ConnectCallBack() {
                    @Override
                    public void call(IotMqttClientStatus status) {
                        if (status == IotMqttClientStatus.Connected) {
                            if (WebSocketClient.GetSubScribeStatus())
                                return;
                            WebSocketClient.SubScribe("TEST", new SubScribeCallback() {
                                @Override
                                public void call(String Topic, final String Message, byte[] bytes) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((EditText) findViewById(R.id.WebSocketMsg)).setText(Message);
                                        }
                                    });
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.WebSocketStatus)).setText("WebSocket:Connected");
                                    ((Button) findViewById(R.id.WebSocketConnect)).setText("WebSocket Disconnect");
                                    ((Button) findViewById(R.id.WebSocketTestMsg)).setEnabled(true);
                                }
                            });
                        }
                    }
                });
            } else {
                WebSocketClient.Disconnect();
                WebSocketShadowClient.finalize();
                WebSocketShadowClient = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.WebSocketStatus)).setText("WebSocket:Disconnected");
                        ((Button) findViewById(R.id.WebSocketConnect)).setText("WebSocket Connect");
                        ((Button) findViewById(R.id.WebSocketTestMsg)).setEnabled(false);
                    }
                });
            }
        } else if (v.getId() == R.id.MQTTTestMsg) {
            MQTTClient.Publish("TEST", "Message form MQTT Client. " + (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
        } else if (v.getId() == R.id.WebSocketTestMsg) {
            WebSocketClient.Publish("TEST", "Message form WebSocket Client. " + (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
        }
    }


    private void BeepAndVibrate() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        if (MQTTShadowClient != null) {
            MQTTShadowClient.phoneMQTT.setBeepReport(true);
            final Timer timer = new Timer();

            final TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    MQTTShadowClient.phoneMQTT.setBeepReport(false);
                    timer.cancel();
                }
            };
            timer.schedule(timerTask, 1000);
        }
    }
}

