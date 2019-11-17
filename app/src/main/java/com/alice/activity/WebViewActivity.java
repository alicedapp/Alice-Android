package com.alice.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alice.R;
import com.alice.web3.OnSignPersonalMessageListener;
import com.alice.web3.OnSignTransactionListener;
import com.alice.web3.Web3View;
import com.alice.web3.entity.Message;
import com.alice.web3.entity.Web3Transaction;
import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

import static com.alice.config.Constants.KEY_ADDRESS;

public class WebViewActivity extends Activity implements OnSignTransactionListener, OnSignPersonalMessageListener {
     private Web3View mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView =  findViewById(R.id.wv_content);
        mWebView.setActivity(this);
        mWebView.setWalletAddress(Hawk.get(KEY_ADDRESS));
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webview, int newProgress) {
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        mWebView.loadUrl("http://www.cryptokitties.co/", getWeb3Headers());
        mWebView.setOnSignPersonalMessageListener(this);
    }

    /* Required for CORS requests */
    private Map<String, String> getWeb3Headers()
    {
        //headers
        return new HashMap<String, String>() {{
            put("Connection", "close");
            put("Content-Type", "text/plain");
            put("Access-Control-Allow-Origin", "*");
            put("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
            put("Access-Control-Max-Age", "600");
            put("Access-Control-Allow-Credentials", "true");
            put("Access-Control-Allow-Headers", "accept, authorization, Content-Type");
        }};
    }

    @Override
    public void onSignPersonalMessage(Message<String> message) {
        Toast.makeText(this,message.value,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSignTransaction(Web3Transaction transaction, String url) {

    }
}
