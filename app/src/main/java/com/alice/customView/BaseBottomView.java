package com.alice.customView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alice.R;
import com.alice.model.SmartContractMessage;
import com.alice.source.BaseDataSource;
import com.alice.utils.ToastUtils;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/18
 */
public abstract class BaseBottomView<T> extends RelativeLayout {

    protected LinearLayout mContent;
    protected View bg;
    protected ViewGroup mParentView;
    protected TextView mSend;
    protected T mData;
    private boolean isShow;

    public boolean isShow() {
        return isShow;
    }

    private OnClickSendListener onClickSendListener;

    public BaseBottomView(Context context) {
        this(context,null);
    }

    public void setOnClickSendListener(OnClickSendListener<T> onClickSendListener) {
        this.onClickSendListener = onClickSendListener;
    }

    public BaseBottomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(getContentLayout(), this, true);
        mContent = findViewById(R.id.rl_content);
        bg = findViewById(R.id.v_bg);
        bg.setOnClickListener(v -> hideView());
        mSend = findViewById(R.id.tv_send);
        bg.setOnClickListener(v -> hideView());
        mSend.setOnClickListener(v -> {
            if(onClickSendListener!=null){
                if(mData == null){
                    ToastUtils.makeText("please wait data to load");
                    return;
                }
                onClickSendListener.OnClickSend(mData);
            }
        });
        initView();
    }

    protected abstract int getContentLayout();

    protected abstract void initView();

    protected abstract void updateViews(T data);

    public void showView(Activity activity){
        if(activity!=null){
            mParentView = (ViewGroup)activity.getWindow().getDecorView();
            mParentView.addView(this);
            ObjectAnimator translationYAnimation = ObjectAnimator.ofFloat(mContent, "translationY",mContent.getTranslationY(),0);
            translationYAnimation.setDuration(200);
            translationYAnimation.start();
            isShow = true;
        }
    }

    public void hideView(){
        if(mParentView!=null){
            ObjectAnimator translationYAnimation = ObjectAnimator.ofFloat(mContent, "translationY",0,getResources().getDimensionPixelOffset(R.dimen.view_dimen_1500));
            translationYAnimation.setDuration(200);
            translationYAnimation.start();
            translationYAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mParentView.removeView(BaseBottomView.this);
                    isShow = false;
                    super.onAnimationEnd(animation);
                }
            });
        }
    }

    public void setData(T data){
        mData = data;
        updateViews(data);
    }

    public interface OnClickSendListener<T>{
        void OnClickSend(T data);
    }
}
