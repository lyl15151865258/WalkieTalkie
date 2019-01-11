package jp.co.shiratsuki.walkietalkie.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.pkmmte.view.CircularImageView;

import org.webrtc.MediaStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.adapter.MalfunctionAdapter;
import jp.co.shiratsuki.walkietalkie.bean.WebSocketData;
import jp.co.shiratsuki.walkietalkie.broadcast.BaseBroadcastReceiver;
import jp.co.shiratsuki.walkietalkie.constant.Constants;
import jp.co.shiratsuki.walkietalkie.service.WebSocketService;
import jp.co.shiratsuki.walkietalkie.utils.LogUtils;
import jp.co.shiratsuki.walkietalkie.utils.NotificationsUtil;
import jp.co.shiratsuki.walkietalkie.utils.PermissionUtil;
import jp.co.shiratsuki.walkietalkie.utils.StatusBarUtil;
import jp.co.shiratsuki.walkietalkie.webrtc.IWebRTCHelper;
import jp.co.shiratsuki.walkietalkie.webrtc.WebRTCHelper;
import jp.co.shiratsuki.walkietalkie.webrtc.WebRTCUtil;
import jp.co.shiratsuki.walkietalkie.widget.xrecyclerview.ProgressStyle;
import jp.co.shiratsuki.walkietalkie.widget.xrecyclerview.XRecyclerView;
import jp.co.shiratsuki.walkietalkie.widget.xrecyclerview.XRecyclerViewDivider;

/**
 * 主页面
 * Created at 2019/1/11 17:16
 *
 * @author Li Yuliang
 * @version 1.0
 */

public class MainActivity extends BaseActivity implements IWebRTCHelper {

    private String TAG = "MainActivity";
    private Context mContext;
    private DrawerLayout drawerLayout;
    private NavigationView navigation;
    private LinearLayout llMain, llNotification;
    private CircularImageView ivIcon, ivUserIcon;
    private ImageView ivRight;
    private XRecyclerView rvMalfunction;
    private List<WebSocketData> malfunctionList;
    private MalfunctionAdapter malfunctionAdapter;
    private MyReceiver myReceiver;
    private boolean sIsScrolling = false;
    private WebSocketService webSocketService;

    private Button btnEnterRoom;

    private WebRTCHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initWebSocket();
        initBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showOrHideNotification();
        Intent intent = new Intent(mContext, WebSocketService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void setStatusBar() {
        int mColor = getResources().getColor(R.color.colorBluePrimary);
        StatusBarUtil.setColorForDrawerLayout(this, findViewById(R.id.drawer_layout), mColor);
    }

    private void initView() {

        navigation = findViewById(R.id.navigation);
        ViewGroup.LayoutParams params = navigation.getLayoutParams();
        params.width = mWidth * 4 / 5;
        navigation.setLayoutParams(params);

        ivRight = findViewById(R.id.iv_right);
        ivRight.setOnClickListener(onClickListener);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.addDrawerListener(drawerListener);
        llMain = findViewById(R.id.ll_main);
        ivIcon = findViewById(R.id.iv_icon);
        ivUserIcon = findViewById(R.id.iv_userIcon);
        ivIcon.setOnClickListener(onClickListener);
        llNotification = findViewById(R.id.ll_notification);
        llNotification.setOnClickListener(onClickListener);
        rvMalfunction = findViewById(R.id.rvMalfunction);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvMalfunction.setLayoutManager(linearLayoutManager);
        rvMalfunction.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        rvMalfunction.setArrowImageView(R.drawable.iconfont_downgrey);
        rvMalfunction.getDefaultRefreshHeaderView().setRefreshTimeVisible(false);
        rvMalfunction.setLoadingMoreEnabled(false);
        rvMalfunction.addItemDecoration(new XRecyclerViewDivider(mContext, LinearLayoutManager.HORIZONTAL, 1, ContextCompat.getColor(mContext, R.color.gray_slight)));

        rvMalfunction.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                rvMalfunction.refreshComplete();
            }

            @Override
            public void onLoadMore() {

            }
        });
        malfunctionList = new ArrayList<>();
        malfunctionAdapter = new MalfunctionAdapter(mContext, malfunctionList);
        malfunctionAdapter.setOnItemClickListener(onItemClickListener);
        rvMalfunction.setAdapter(malfunctionAdapter);
        rvMalfunction.addOnScrollListener(onScrollListener);

        btnEnterRoom = findViewById(R.id.btnEnterRoom);
        btnEnterRoom.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = (view) -> {
        Intent intent;
        switch (view.getId()) {
            case R.id.iv_right:

                break;
            case R.id.iv_icon:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.ll_notification:
                intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
                break;
            case R.id.btnEnterRoom:
                if (!PermissionUtil.isNeedRequestPermission(MainActivity.this)) {
                    helper = new WebRTCHelper(MainActivity.this, MainActivity.this, WebRTCUtil.iceServers);
                    helper.initSocket(WebRTCUtil.signal, "123", false);
                }
                break;
            default:
                break;
        }
    };

    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            llMain.scrollTo(-(int) (navigation.getWidth() * slideOffset), 0);
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    private void initWebSocket() {
        Intent intent = new Intent(mContext, WebSocketService.class);
        intent.putExtra("ServerHost", Constants.WEBSOCKET_IP);
        intent.putExtra("WebSocketPort", String.valueOf(Constants.WEBSOCKET_PORT));
        intent.putExtra("WebSocketName", String.valueOf(Constants.WEBSOCKET_NAME));
        mContext.startService(intent);
    }

    private void initBroadcastReceiver() {
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("RECEIVE_MALFUNCTION");
        intentFilter.addAction("CURRENT_PLAYING");
        mContext.registerReceiver(myReceiver, intentFilter);
    }

    /**
     * 显示或隐藏“打开悬浮窗”的提示
     */
    private void showOrHideNotification() {
        if (NotificationsUtil.isNotificationEnabled(this)) {
            llNotification.setVisibility(View.GONE);
        } else {
            llNotification.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            LogUtils.d(WebRTCHelper.TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                break;
            }
        }
        helper = new WebRTCHelper(MainActivity.this, MainActivity.this, WebRTCUtil.iceServers);
        helper.initSocket(WebRTCUtil.signal, "", false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSetLocalStream(MediaStream stream, String socketId) {

    }

    @Override
    public void onAddRemoteStream(MediaStream stream, String socketId) {

    }

    @Override
    public void onCloseWithId(String socketId) {

    }

    private MalfunctionAdapter.OnItemClickListener onItemClickListener = (position) -> {

    };

    /**
     * onServiceConnected和onServiceDisconnected运行在UI线程中
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WebSocketService.WebSocketServiceBinder binder = (WebSocketService.WebSocketServiceBinder) service;
            webSocketService = binder.getService();
            LogUtils.d(TAG, "绑定Service成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
        }
    };

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // 先判断mContext是否为空，防止Activity已经onDestroy导致的java.lang.IllegalArgumentException: You cannot start a load for a destroyed activity
            if (mContext != null) {
                // 如果快速滑动，停止Glide的加载，停止滑动后恢复加载
                if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    sIsScrolling = true;
                    Glide.with(mContext).pauseRequests();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (sIsScrolling) {
                        Glide.with(mContext).resumeRequests();
                    }
                    sIsScrolling = false;
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    private class MyReceiver extends BaseBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            super.onReceive(context, intent);
            if ("RECEIVE_MALFUNCTION".equals(intent.getAction())) {
                WebSocketData webSocketData = (WebSocketData) intent.getSerializableExtra("data");
                if (webSocketData.isStatus()) {
                    malfunctionList.add(0, webSocketData);
                    malfunctionAdapter.notifyDataSetChanged();
                } else {
                    Iterator<WebSocketData> webSocketMsgIterator = malfunctionList.iterator();
                    while (webSocketMsgIterator.hasNext()) {
                        WebSocketData socketMsg = webSocketMsgIterator.next();
                        if (socketMsg.getListNo() == webSocketData.getListNo()) {
                            webSocketMsgIterator.remove();
                        }
                    }
                    malfunctionAdapter.notifyDataSetChanged();
                }
            }
            if ("CURRENT_PLAYING".equals(intent.getAction())) {
                int listNo = intent.getIntExtra("number", -1);
                if (listNo == -1) {
                    for (int i = 0; i < malfunctionList.size(); i++) {
                        malfunctionList.get(i).setPalying(false);
                    }
                    malfunctionAdapter.notifyDataSetChanged();
                } else {
                    for (int i = 0; i < malfunctionList.size(); i++) {
                        if (listNo == malfunctionList.get(i).getListNo()) {
                            malfunctionList.get(i).setPalying(true);
                        } else {
                            malfunctionList.get(i).setPalying(false);
                        }
                    }
                    malfunctionAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.exitRoom();
        webSocketService.closeWebSocket();
        if (myReceiver != null) {
            mContext.unregisterReceiver(myReceiver);
        }
    }

}
