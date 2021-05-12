package com.mhy.socket;

/**
 * Created By Mahongyin
 * Date    2021/5/11 16:57
 */
    public class Client {
        private String ip;
        private int port;

        public Client(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {

            return ip == null ? "" : ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }