package com.hxlxz.hxl.iotggtest;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    double lightSenserValue, distanceSenserValue;
    FlashLight flashLight = new FlashLight();
    Sensors sensors;
    TextView Light;
    TextView Distance;
    private final Timer sensorTimer = new Timer();
    private TimerTask sensorTimerTask;
    IOT_MQTT MQTTClient1, MQTTClient2;
    IOTData_MQTT MQTTShadowClient1, MQTTShadowClient2;
    PhoneMQTT1 phoneMQTT1 = new PhoneMQTT1("Phone1");
    PhoneMQTT2 phoneMQTT2 = new PhoneMQTT2("Phone2");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("IOT", "MainActivity.onCreate-start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorsInit();

        TimerInit();
        MQTTClient1 = new IOT_MQTT(getApplicationContext());
        MQTTClient2 = new IOT_MQTT(getApplicationContext());

        Log.d("IOT", "MainActivity.onCreate-end");
    }

    private void TimerInit() {
        sensorTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (MQTTShadowClient1 != null) {
                    //final PhoneJSON1 phoneJSON1 = PhoneJSON1.GetPhoneJSON(MQTTShadowClient1.getJSON());
                    if (phoneMQTT1 != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (phoneMQTT1.getBeepDesired())
                                    BeepAndVibrate();

                                if (phoneMQTT1.getBeep())
                                    ((TextView) findViewById(R.id.MQTT1BeepStatus)).setText("Beep:T");
                                else
                                    ((TextView) findViewById(R.id.MQTT1BeepStatus)).setText("Beep:F");

                                ((TextView) findViewById(R.id.MQTT1LightStatus)).setText("Light:" + phoneMQTT1.getLight());
                            }
                        });
                    }
                }
                if (MQTTShadowClient2 != null) {
                    //final PhoneJSON2 phoneJSON2 = PhoneJSON2.GetPhoneJSON(MQTTShadowClient2.getJSON());
                    if (phoneMQTT2 != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (phoneMQTT2.getFlashLightDesired())
                                    flashLight.SetStatus(FlashLight.Status.ON);
                                else flashLight.SetStatus(FlashLight.Status.OFF);
                                phoneMQTT2.setFlashLightpReport(phoneMQTT2.getFlashLightDesired());

                                if (phoneMQTT2.getFlashLight())
                                    ((TextView) findViewById(R.id.MQTT2FlashStatus)).setText("Flash:T");
                                else
                                    ((TextView) findViewById(R.id.MQTT2FlashStatus)).setText("Flash:F");

                                ((TextView) findViewById(R.id.MQTT2DistanceStatus)).setText(("Distance:" + phoneMQTT2.getDistance()).substring(0, 10));
                            }
                        });

                    }
                }
            }
        };
        sensorTimer.schedule(sensorTimerTask, 1000, 1000);
    }

    private void SensorsInit() {
        sensors = new Sensors(getApplicationContext(), new SensorCallback() {
            @Override
            public void call(float value) {
                Light = (TextView) findViewById(R.id.Light);
                Light.setText("Light:" + value);
                lightSenserValue = value;
                phoneMQTT1.setLight(value);
            }
        }, new SensorCallback() {
            @Override
            public void call(float value) {
                Distance = (TextView) findViewById(R.id.Distance);
                Distance.setText(("Distance:" + value).substring(0, 10));
                distanceSenserValue = value;
                phoneMQTT2.setDistance(value);
            }
        });
    }

    public void onClickListener(View v) {
        if (v.getId() == R.id.LightONButtom) {
            PhoneJSON2 phoneJSON2 = new PhoneJSON2(true);
            phoneJSON2.state.desired.flashLight = true;
            MQTTShadowClient2.setJSON(PhoneJSON2.GetPhoneJSONString(phoneJSON2));
        } else if (v.getId() == R.id.LightOFFButtom) {
            PhoneJSON2 phoneJSON2 = new PhoneJSON2(true);
            phoneJSON2.state.desired.flashLight = false;
            MQTTShadowClient2.setJSON(PhoneJSON2.GetPhoneJSONString(phoneJSON2));
        } else if (v.getId() == R.id.BeepONButtom) {
            PhoneJSON1 phoneJSON1 = new PhoneJSON1(true);
            phoneJSON1.state.desired.beep = true;
            MQTTShadowClient1.setJSON(PhoneJSON1.GetPhoneJSONString(phoneJSON1));
        } else if (v.getId() == R.id.BeepOFFButtom) {
            PhoneJSON1 phoneJSON1 = new PhoneJSON1(true);
            phoneJSON1.state.desired.beep = false;
            MQTTShadowClient1.setJSON(PhoneJSON1.GetPhoneJSONString(phoneJSON1));
        } else if (v.getId() == R.id.MQTT1Connect) {
            if (MQTTClient1.GetStatus() != IOT_MQTT.IotMqttClientStatus.Connected) {
                if (MQTTShadowClient1 == null)
                    MQTTShadowClient1 = new IOTData_MQTT(getApplicationContext(), phoneMQTT1, "Phone1");
                MQTTClient1.Connect(new ConnectCallBack() {
                    @Override
                    public void call(IOT_MQTT.IotMqttClientStatus status) {
                        if (status == IOT_MQTT.IotMqttClientStatus.Connected) {
                            if (MQTTClient1.GetSubScribeStatus())
                                return;
                            MQTTClient1.SubScribe("TEST", new SubScribeCallback() {
                                @Override
                                public void call(String Topic, final String Message, byte[] bytes) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((EditText) findViewById(R.id.MQTT1Msg)).setText(Message);
                                        }
                                    });
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.MQTT1Status)).setText("MQTT1:Connected");
                                    ((Button) findViewById(R.id.MQTT1Connect)).setText("MQTT1 Disconnect");
                                    ((Button) findViewById(R.id.MQTT1TestMsg)).setEnabled(true);
                                    ((Button) findViewById(R.id.BeepONButtom)).setEnabled(true);
                                    ((Button) findViewById(R.id.BeepOFFButtom)).setEnabled(true);
                                }
                            });
                        }
                    }
                });
            } else {
                MQTTClient1.Disconnect();
                MQTTShadowClient1.finalize();
                MQTTShadowClient1 = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.MQTT1Status)).setText("MQTT1:Disconnected");
                        ((Button) findViewById(R.id.MQTT1Connect)).setText("MQTT1 Connect");
                        ((TextView) findViewById(R.id.MQTT1BeepStatus)).setText(" ");
                        ((TextView) findViewById(R.id.MQTT1FlashStatus)).setText(" ");
                        ((TextView) findViewById(R.id.MQTT1LightStatus)).setText(" ");
                        ((TextView) findViewById(R.id.MQTT1DistanceStatus)).setText(" ");
                        ((Button) findViewById(R.id.MQTT1TestMsg)).setEnabled(false);
                        ((Button) findViewById(R.id.BeepONButtom)).setEnabled(false);
                        ((Button) findViewById(R.id.BeepOFFButtom)).setEnabled(false);
                    }
                });
            }
        } else if (v.getId() == R.id.MQTT2Connect) {
            if (MQTTClient2.GetStatus() != IOT_MQTT.IotMqttClientStatus.Connected) {
                if (MQTTShadowClient2 == null)
                    MQTTShadowClient2 = new IOTData_MQTT(getApplicationContext(), phoneMQTT2, "Phone2");
                MQTTClient2.Connect(new ConnectCallBack() {
                    @Override
                    public void call(IOT_MQTT.IotMqttClientStatus status) {
                        if (status == IOT_MQTT.IotMqttClientStatus.Connected) {
                            if (MQTTClient2.GetSubScribeStatus())
                                return;
                            MQTTClient2.SubScribe("TEST", new SubScribeCallback() {
                                @Override
                                public void call(String Topic, final String Message, byte[] bytes) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((EditText) findViewById(R.id.MQTT2Msg)).setText(Message);
                                        }
                                    });
                                }
                            });
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView) findViewById(R.id.MQTT2Status)).setText("MQTT2:Connected");
                                    ((Button) findViewById(R.id.MQTT2Connect)).setText("MQTT2 Disconnect");
                                    ((Button) findViewById(R.id.MQTT2TestMsg)).setEnabled(true);
                                    ((Button) findViewById(R.id.LightONButtom)).setEnabled(true);
                                    ((Button) findViewById(R.id.LightOFFButtom)).setEnabled(true);
                                }
                            });
                        }
                    }
                });
            } else {
                MQTTClient2.Disconnect();
                MQTTShadowClient2.finalize();
                MQTTShadowClient2 = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.MQTT2Status)).setText("MQTT2:Disconnected");
                        ((Button) findViewById(R.id.MQTT2Connect)).setText("MQTT2 Connect");
                        ((TextView) findViewById(R.id.MQTT2BeepStatus)).setText(" ");
                        ((TextView) findViewById(R.id.MQTT2FlashStatus)).setText(" ");
                        ((TextView) findViewById(R.id.MQTT2LightStatus)).setText(" ");
                        ((TextView) findViewById(R.id.MQTT2DistanceStatus)).setText(" ");
                        ((Button) findViewById(R.id.MQTT2TestMsg)).setEnabled(false);
                        ((Button) findViewById(R.id.LightONButtom)).setEnabled(false);
                        ((Button) findViewById(R.id.LightOFFButtom)).setEnabled(false);
                    }
                });
            }
        } else if (v.getId() == R.id.MQTT1TestMsg) {
            MQTTClient1.Publish("TEST", "Message form MQTT Client1. " + (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
        } else if (v.getId() == R.id.MQTT2TestMsg) {
            MQTTClient2.Publish("TEST", "Message form MQTT Client2. " + (new SimpleDateFormat("HH:mm:ss")).format(new Date()));
        }
    }


    private void BeepAndVibrate() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        if (MQTTShadowClient1 != null) {
            phoneMQTT1.setBeepReport(true);

            final Timer timer = new Timer();
            final TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    phoneMQTT1.setBeepReport(false);
                    timer.cancel();
                }
            };
            timer.schedule(timerTask, 1000);
        }
    }
}
