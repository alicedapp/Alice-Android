package com.alice.customView;

import android.content.Context;

import com.alice.R;
import com.alice.model.SmartContractMessage;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/18
 */
public class SendTransactionView extends BaseBottomView<SmartContractMessage> {

    public SendTransactionView(Context context) {
        super(context);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.view_send_transation_tap;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void updateViews(SmartContractMessage data) {

    }
}
