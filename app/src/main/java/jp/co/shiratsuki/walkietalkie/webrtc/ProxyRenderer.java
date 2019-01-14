package jp.co.shiratsuki.walkietalkie.webrtc;

import org.webrtc.VideoRenderer;

/**
 *
 * Created at 2019/1/15 2:40
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class ProxyRenderer implements VideoRenderer.Callbacks {
    private VideoRenderer.Callbacks target;

    synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
        if (target == null) {
            VideoRenderer.renderFrameDone(frame);
            return;
        }
        target.renderFrame(frame);
    }

    synchronized public void setTarget(VideoRenderer.Callbacks target) {
        this.target = target;
    }
}
