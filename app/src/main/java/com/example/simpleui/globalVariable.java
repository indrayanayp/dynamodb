package com.example.simpleui;

public class globalVariable {
        String username_global;
        String date_global;
        String time_global;

        private static final globalVariable ourInstance = new globalVariable();
        public static globalVariable getInstance() {
            return ourInstance;
        }
        private globalVariable() {
        }
        public void setUsername_global(String username_global) {
            this.username_global = username_global;
        }
        public String getUsername_global() {
            return username_global;
        }
        public void setDate_global(String date_global) {
            this.date_global = date_global;
        }
        public String getDate_global() {
            return date_global;
        }
        public void setTime_global(String time_global) {
            this.time_global = time_global;
        }
        public String getTime_global() {
            return time_global;
        }
}
