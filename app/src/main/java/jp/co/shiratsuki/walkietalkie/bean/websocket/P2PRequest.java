package jp.co.shiratsuki.walkietalkie.bean.websocket;

import jp.co.shiratsuki.walkietalkie.bean.User;

public class P2PRequest {

    private Data data;
    private String eventName;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public class Data {

        public User user;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

}
