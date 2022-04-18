package com.example.simpleui;

public class Recording {
        private String date;
        private String time;
        private String avgHR;
        private Long sortnumber;

        public Recording(String date, String time,  String avgHR, Long sortnumber) {
            this.date = date;
            this.time = time;
            this.avgHR = avgHR;
            this.sortnumber = sortnumber;
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

        public Long getSortNumber() {
        return sortnumber;
    }

        public void setSortNumber(String sortNumber) {
        this.sortnumber = sortnumber;
    }


}
