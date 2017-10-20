package com.hxlxz.hxl.iottest;


class PhoneJSON {
    public State state = new State();

    public static class State {
        public Document reported = new Document();
        public Document desired = new Document();
        public Document delta = new Document();
        public Long version;
        public Long timestamp;

        public class Document {
            public double distance;
            public boolean beep;
            public int light;
            public boolean flashLight;
        }
    }
}
