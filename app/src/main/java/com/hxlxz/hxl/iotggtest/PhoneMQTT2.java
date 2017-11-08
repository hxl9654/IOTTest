package com.hxlxz.hxl.iotggtest;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;


public class PhoneMQTT2 extends AWSIotDevice {
    PhoneMQTT2(String thingName) {
        super(thingName);
    }

    @AWSIotDeviceProperty(allowUpdate = false)
    private double distance = 0;
    @AWSIotDeviceProperty
    private boolean flashLight = false;           //Report
    private boolean flashLightDesired = false;    //Desired

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean getFlashLight() {
        return flashLight;
    }

    public void setFlashLight(boolean flashLight) {
        this.flashLightDesired = flashLight;
    }

    void setFlashLightpReport(boolean flashLight) {
        this.flashLight = flashLight;
    }

    boolean getFlashLightDesired() {
        return flashLightDesired;
    }
}
