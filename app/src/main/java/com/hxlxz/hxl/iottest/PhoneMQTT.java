package com.hxlxz.hxl.iottest;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;


public class PhoneMQTT extends AWSIotDevice {
    public PhoneMQTT(String thingName) {
        super(thingName);
    }

    @AWSIotDeviceProperty
    private double light = 10;
    @AWSIotDeviceProperty
    private boolean beep = false;

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public boolean getBeep() {
        return beep;
    }

    public void setBeep(boolean beep) {
        this.beep = beep;
    }
}