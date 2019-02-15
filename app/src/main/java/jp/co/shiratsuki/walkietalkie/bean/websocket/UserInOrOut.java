package jp.co.shiratsuki.walkietalkie.bean.websocket;

import java.util.ArrayList;

import jp.co.shiratsuki.walkietalkie.bean.Contact;

public class UserInOrOut {

    private Data data;
    private String eventName;

    public void setData(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public class Data {

        private String socketId;
        private ArrayList<Contact> contacts;

        public void setSocketId(String socketId) {
            this.socketId = socketId;
        }

        public String getSocketId() {
            return socketId;
        }

        public void setContacts(ArrayList<Contact> contacts) {
            this.contacts = contacts;
        }

        public ArrayList<Contact> getContacts() {
            return contacts;
        }

    }

}
