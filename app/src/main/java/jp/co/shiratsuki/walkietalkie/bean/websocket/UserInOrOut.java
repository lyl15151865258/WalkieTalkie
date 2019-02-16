package jp.co.shiratsuki.walkietalkie.bean.websocket;

import java.util.ArrayList;

import jp.co.shiratsuki.walkietalkie.bean.User;

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

        private String userId;
        private ArrayList<User> contacts;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public ArrayList<User> getContacts() {
            return contacts;
        }

        public void setContacts(ArrayList<User> contacts) {
            this.contacts = contacts;
        }
    }

}
