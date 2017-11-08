package com.hxlxz.hxl.iotggtest;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

class PhoneJSON2 {
    PhoneJSON2(boolean OnlyDesired) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        state = new State();
        state.desired = new State.Document();
        if(OnlyDesired)
            return;
        state.reported = new State.Document();
        state.delta = new State.Document();
    }
    private static ObjectMapper objectMapper = new ObjectMapper();
    public State state;

    public static class State {
        public Document reported = null;
        public Document desired = null;
        public Document delta = null;
        public Long version;
        public Long timestamp;

        public static class Document {
            public Double distance = null;
            public Boolean flashLight = null;
        }
    }

    public static PhoneJSON2 GetPhoneJSON(String JSONString) {
        try {
            return objectMapper.readValue(JSONString, PhoneJSON2.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String GetPhoneJSONString(PhoneJSON2 phoneJSON) {
        try {
            return objectMapper.writeValueAsString(phoneJSON);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
