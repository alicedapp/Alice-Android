package com.alice.customView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alice.R;


/**
 * 自定义dialog
 * create by zhanghaoran3 2019/6/27
 */
public class BaseDialog extends Dialog {

    private TextView tvTitle;
    private TextView tvMsg;
    private TextView btnPositive;
    private TextView btnNegative;
    private View.OnClickListener onPositiveListener;
    private View.OnClickListener onNegativeListener;
    private String mTitle;
    private String mMessage;
    private String positiveText;
    private String negativeText;
    private int paddingOffset;
    private FrameLayout mCustomViewWrapper;
    private View mCustomView;

    private BaseDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base);
        tvTitle = findViewById(R.id.title);
        tvMsg = findViewById(R.id.desc);
        btnPositive = findViewById(R.id.ok);
        btnNegative = findViewById(R.id.cancel);
        mCustomViewWrapper = findViewById(R.id.custom_view);
        paddingOffset = getContext().getResources().getDimensionPixelOffset(R.dimen.view_dimen_90);

        Window dialogWindow = this.getWindow();
        dialogWindow.getDecorView().setPadding(paddingOffset, 0, paddingOffset, 0);
        WindowManager.LayoutParams wlp = dialogWindow.getAttributes();
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(wlp);

    }

    /**
     * 调用完Builder类的create()方法后显示该对话框的方法
     */
    @Override
    public void show() {
        super.show();
        show(this);
    }

    private void show(BaseDialog mDialog) {
        if (!TextUtils.isEmpty(mDialog.mTitle)) {
            mDialog.tvTitle.setVisibility(View.VISIBLE);
            mDialog.tvTitle.setText(mDialog.mTitle);
        }
        if (!TextUtils.isEmpty(mDialog.mMessage)) {
            mDialog.tvMsg.setText(mDialog.mMessage);
        }
        if(!TextUtils.isEmpty(negativeText)){
            btnNegative.setText(negativeText);
        }
        if(!TextUtils.isEmpty(positiveText)){
            btnPositive.setText(positiveText);
        }
        if (onNegativeListener!=null) {
            btnNegative.setVisibility(View.VISIBLE);
            btnNegative.setOnClickListener(onNegativeListener);
        }
        if (onPositiveListener!=null) {
            btnPositive.setOnClickListener(onPositiveListener);
        }
        if(mCustomView!=null){
            mCustomViewWrapper.addView(mCustomView);
        }
    }

    public static class Builder {

        private BaseDialog mDialog;

        public Builder(Context context) {
            mDialog = new BaseDialog(context);
        }

        /**
         * 设置对话框标题
         *
         * @param title
         */
        public Builder setTitle(String title) {
            mDialog.mTitle = title;
            return this;
        }

        /**
         * 设置对话框文本内容,如果调用了setView()方法，该项失效
         *
         * @param msg
         */
        public Builder setMessage(String msg) {
            mDialog.mMessage = msg;
            return this;
        }

        /**
         * 设置确认按钮的回调
         *
         * @param onClickListener
         */
        public Builder setPositiveButton(View.OnClickListener onClickListener) {
            mDialog.onPositiveListener = onClickListener;
            return this;
        }

        /**
         * 设置确认按钮的回调
         *
         * @param btnText,onClickListener
         */
        public Builder setPositiveButton(String btnText, View.OnClickListener onClickListener) {
            mDialog.positiveText = btnText;
            mDialog.onPositiveListener = onClickListener;
            return this;
        }

        /**
         * 设置取消按钮的回掉
         *
         * @param onClickListener
         */
        public Builder setNegativeButton(View.OnClickListener onClickListener) {
            mDialog.onNegativeListener = onClickListener;
            return this;
        }

        /**
         * 设置取消按钮的回调
         *
         * @param btnText,onClickListener
         */
        public Builder setNegativeButton(String btnText, View.OnClickListener onClickListener) {
            mDialog.negativeText = btnText;
            mDialog.onNegativeListener = onClickListener;
            return this;
        }

        /**
         * 设置该对话框能否被Cancel掉，默认可以
         *
         * @param cancelable
         */
        public Builder setCancelable(boolean cancelable) {
            mDialog.setCancelable(cancelable);
            return this;
        }

        /**
         * 设置对话框被cancel对应的回调接口，cancel()方法被调用时才会回调该接口
         *
         * @param onCancelListener
         */
        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            mDialog.setOnCancelListener(onCancelListener);
            return this;
        }

        /**
         * 设置对话框消失对应的回调接口，一切对话框消失都会回调该接口
         *
         * @param onDismissListener
         */
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            mDialog.setOnDismissListener(onDismissListener);
            return this;
        }


        public Builder setCustomView(View view){
            mDialog.mCustomView = view;
            return this;
        }

        /**
         * 通过Builder类设置完属性后构造对话框的方法
         */
        public BaseDialog create() {
            return mDialog;
        }
    }
}
