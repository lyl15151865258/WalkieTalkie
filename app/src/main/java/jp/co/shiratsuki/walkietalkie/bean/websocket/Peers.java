package jp.co.shiratsuki.walkietalkie.bean.websocket;

import java.util.List;

/**
 * 自己进入房间
 * Created at 2019/1/22 9:57
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class Peers {

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

        private String you;
        private List<String> connections;

        public String getYou() {
            return you;
        }

        public void setYou(String you) {
            this.you = you;
        }

        public List<String> getConnections() {
            return connections;
        }

        public void setConnections(List<String> connections) {
            this.connections = connections;
        }
    }
}
