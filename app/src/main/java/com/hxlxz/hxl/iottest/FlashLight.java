package com.hxlxz.hxl.iottest;


import android.hardware.Camera;

class FlashLight {
    public enum Status {
        ON, OFF
    }

    Camera m_Camera;
    Camera.Parameters mParameters;
    private Status status = Status.OFF;

    FlashLight() {
        m_Camera = Camera.open();
        mParameters = m_Camera.getParameters();
    }

    public void SetStatus(Status status) {
        try {
            switch (status) {
                case ON:
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    m_Camera.setParameters(mParameters);
                    break;
                case OFF:
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    m_Camera.setParameters(mParameters);
                    break;
            }
        } catch (Exception ex) {
        }
        this.status = status;
    }

    public Status GetStatus() {
        return status;
    }
}
