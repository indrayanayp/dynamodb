package com.example.simpleui;

public class Recording {
        private String date;
        private String time;
        private String avgHR;

        public Recording(String date, String time,  String avgHR) {
            this.date = date;
            this.time = time;
            this.avgHR = avgHR;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getAvgHR() {
            return avgHR;
        }

        public void setAvgHR(String avgHR) {
            this.avgHR = avgHR;
        }

}
