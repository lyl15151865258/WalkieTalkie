package jp.co.shiratsuki.walkietalkie.activity.appmain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import jp.co.shiratsuki.walkietalkie.R;
import jp.co.shiratsuki.walkietalkie.activity.base.SwipeBackActivity;
import jp.co.shiratsuki.walkietalkie.utils.ActivityController;
import jp.co.shiratsuki.walkietalkie.utils.NetworkUtil;
import jp.co.shiratsuki.walkietalkie.widget.MyToolbar;

/**
 * 通用HTML页面
 * Created at 2018/11/20 13:37
 *
 * @author LiYuliang
 * @version 1.0
 */

public class HtmlActivity extends SwipeBackActivity {

    private Context mContext;
    private ProgressBar progressBarWebView;
    private WebView webViewProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html);
        mContext = this;
        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("URL");
        MyToolbar toolbar = findViewById(R.id.myToolbar);
        toolbar.initToolBar(this, toolbar, title, R.drawable.back_white, onClickListener);
        webViewProtocol = findViewById(R.id.webView_protocol);
        progressBarWebView = findViewById(R.id.progress_bar_webView);
        initSettings();
        loadProtocol(url);
    }

    /**
     * 加载页面配置
     */
    private void initSettings() {
        WebSettings settings = webViewProtocol.getSettings();
        settings.setJavaScriptEnabled(true);
        // 设置缓存模式
        if (NetworkUtil.isNetworkAvailable(mContext)) {
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        // 提高渲染的优先级
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 支持多窗口
        settings.setSupportMultipleWindows(true);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启 Application Caches 功能
        settings.setAppCacheEnabled(true);
    }

    private void loadProtocol(String url) {
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webViewProtocol.loadUrl(url);
        webViewProtocol.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http") & !url.startsWith("https")) {
                    return false;

                } else {
                    view.loadUrl(url);
                    return true;
                }
            }

            @SuppressLint("NewApi")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.startsWith("http") || url.startsWith("https")) {
                    return super.shouldInterceptRequest(view, url);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    //view.loadUrl(url);
                    return null;
                }
            }
        });

        //监听网页加载
        webViewProtocol.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 网页加载完成
                    cancelDialog();
                    progressBarWebView.setVisibility(View.GONE);
                } else {
                    // 加载中
                    showLoadingDialog(mContext, true);
                    progressBarWebView.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
    }

    private View.OnClickListener onClickListener = (v) -> {
        switch (v.getId()) {
            case R.id.iv_left:
                ActivityController.finishActivity(this);
                break;
            default:
                break;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webViewProtocol.canGoBack()) {
            webViewProtocol.goBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

}
