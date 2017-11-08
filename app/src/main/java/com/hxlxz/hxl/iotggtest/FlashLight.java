package com.hxlxz.hxl.iotggtest;


import android.hardware.Camera;

class FlashLight {
    public enum Status {
        ON, OFF
    }

    private Camera m_Camera = Camera.open();
    private Camera.Parameters mParameters = m_Camera.getParameters();
    private Status status = Status.OFF;


    void SetStatus(Status status) {
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
            ex.printStackTrace();
        }
        this.status = status;
    }

    Status GetStatus() {
        return status;
    }
}
