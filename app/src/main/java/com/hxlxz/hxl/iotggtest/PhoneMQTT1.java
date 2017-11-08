package com.hxlxz.hxl.iotggtest;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;


public class PhoneMQTT1 extends AWSIotDevice {
    PhoneMQTT1(String thingName) {
        super(thingName);
    }

    @AWSIotDeviceProperty (allowUpdate = false)
    private double light = 0;
    @AWSIotDeviceProperty
    private boolean beep = false;           //Report
    private boolean beepDesired = false;    //Desired

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public boolean getBeep() {          //Report
        return beep;
    }

    public void setBeep(boolean beep) { //Desired
        this.beepDesired = beep;
    }

    void setBeepReport(boolean beep) {
        this.beep = beep;
    }

    boolean getBeepDesired() {
        return beepDesired;
    }
}