package jp.co.shiratsuki.walkietalkie.bean.websocket;

import java.util.List;

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
        private List<Contact> contacts;

        public void setSocketId(String socketId) {
            this.socketId = socketId;
        }

        public String getSocketId() {
            return socketId;
        }

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }

        public List<Contact> getContacts() {
            return contacts;
        }

    }

}
