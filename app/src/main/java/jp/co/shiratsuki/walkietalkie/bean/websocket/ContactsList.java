package jp.co.shiratsuki.walkietalkie.bean.websocket;

import java.util.ArrayList;

import jp.co.shiratsuki.walkietalkie.bean.Contact;

public class ContactsList {

    private String eventName;
    private Data data;

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public class Data {

        private String socketId;
        private String socketName;
        private ArrayList<Contact> contacts;

        public void setSocketId(String socketId) {
            this.socketId = socketId;
        }

        public String getSocketId() {
            return socketId;
        }

        public void setSocketName(String socketName) {
            this.socketName = socketName;
        }

        public String getSocketName() {
            return socketName;
        }

        public void setContacts(ArrayList<Contact> contacts) {
            this.contacts = contacts;
        }

        public ArrayList<Contact> getContacts() {
            return contacts;
        }

    }

}
