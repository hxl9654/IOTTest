package com.hxlxz.hxl.iottest;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;


public class PhoneMQTT extends AWSIotDevice {
    public PhoneMQTT(String thingName) {
        super(thingName);
    }

    @AWSIotDeviceProperty (allowUpdate = false)
    private double light = 0;
    @AWSIotDeviceProperty
    private boolean beep = false;           //Report
    private boolean beepDesired = false;    //Desired
//    @AWSIotDeviceProperty (enableReport = false)
//    private double distance = 0;
//    @AWSIotDeviceProperty (enableReport = false)
//    private boolean flashLight = false;           //Report

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

    public void setBeepReport(boolean beep) {
        this.beep = beep;
    }

    public boolean getBeepDesired() {
        return beepDesired;
    }

//    public double getDistance() {
//        return distance;
//    }
//
//    public void setDistance(double distance) {
//        this.distance = distance;
//    }
//
//    public boolean getFlashLight() {
//        return flashLight;
//    }
//
//    public void setFlashLight(boolean flashLight) {
//        this.flashLight = flashLight;
//    }
}