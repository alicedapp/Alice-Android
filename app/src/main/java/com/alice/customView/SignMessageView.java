package com.alice.customView;

import android.content.Context;
import android.widget.TextView;

import com.alice.R;
import com.alice.utils.Hex;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/18
 */
public class SignMessageView extends BaseBottomView<String> {
    private TextView mMessage;

    public SignMessageView(Context context) {
        super(context);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.view_sign_bottom_tap;
    }

    @Override
    protected void initView() {
        mMessage = findViewById(R.id.tv_message);
    }

    @Override
    protected void updateViews(String data) {
        String signString = Hex.hexToUtf8(data);
        mMessage.setText(signString);
    }
}
