package jp.co.shiratsuki.walkietalkie.network.statuschange;

import java.util.Observable;

/**
 * 网络变化被观察者
 * Created at 2018/11/28 13:46
 *
 * @author LiYuliang
 * @version 1.0
 */

public class NetworkChange extends Observable {

    private static NetworkChange instance = null;

    public static NetworkChange getInstance() {
        if (null == instance) {
            instance = new NetworkChange();
        }
        return instance;
    }

    //通知观察者数据改变
    public void notifyDataChange(NetworkState net) {
        //被观察者怎么通知观察者数据有改变了呢？？这里的两个方法是关键。
        setChanged();
        notifyObservers(net);
    }

}
