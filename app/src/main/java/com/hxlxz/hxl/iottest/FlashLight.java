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

    public void SetStatus(Status status,IOTData_WebSocket WebSocketShadowClient) {
        try {
            boolean t = false;
            switch (status) {
                case ON:
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    m_Camera.setParameters(mParameters);
                    t = true;
                    break;
                case OFF:
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    m_Camera.setParameters(mParameters);
                    t = false;
                    break;
            }
            if (WebSocketShadowClient != null) {
                WebSocketShadowClient.UpdateShadow_Start("flashLight", Boolean.toString(t), new UpdateShadowCallback() {
                    @Override
                    public void call(final String key, final String value, final String result) {

                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.status = status;
    }

    public Status GetStatus() {
        return status;
    }
}
