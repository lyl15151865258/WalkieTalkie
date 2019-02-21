package jp.co.shiratsuki.walkietalkie.bean.websocket;

import jp.co.shiratsuki.walkietalkie.bean.User;

public class P2PResult {

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

        private String result;
        private String message;
        private User user;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

}
