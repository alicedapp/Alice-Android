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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alice.R;
import com.alice.model.BaseReponseBody;
import com.alice.model.GasPriceModel;
import com.alice.model.PriceModel;
import com.alice.model.SmartContractMessage;
import com.alice.net.Api;
import com.alice.net.ApiConstants;
import com.alice.net.RequestCallback;
import com.alice.source.BaseDataSource;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/18
 */
public class BottomTapView extends RelativeLayout {

    private LinearLayout mContent;
    private View bg;
    private ViewGroup mParentView;

    private TextView mAddress;
    private TextView mSend;

    private TextView mFunctionName;
    private TextView mValue;
    private TextView mParameters;

    private TextView mTvGasPrice;
    private TextView mArriveInTimes;
    private BaseDataSource dataSource;
    private double mPrice;

    private OnClickSendListener onClickSendListener;

    private String mAddaressText;
    private String mValueText;
    private String mFunctionText;
    private String[] mParamsText;

    private SmartContractMessage mData;


    public void setOnClickSendListener(OnClickSendListener onClickSendListener) {
        this.onClickSendListener = onClickSendListener;
    }

    public BottomTapView(Context context) {
        this(context,null);
    }

    public BottomTapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_tap, this, true);
        dataSource = new BaseDataSource();
        initView();
    }

    private void initView() {
        mContent = findViewById(R.id.rl_content);
        bg = findViewById(R.id.v_bg);
        mAddress = findViewById(R.id.tv_address);
        mFunctionName = findViewById(R.id.et_function_name);
        mParameters = findViewById(R.id.ev_params);
        mValue = findViewById(R.id.ev_params);
        mSend = findViewById(R.id.tv_send);
        mTvGasPrice = findViewById(R.id.tv_gas_price);
        mArriveInTimes = findViewById(R.id.tv_arrive_times);

        bg.setOnClickListener(v -> hideView());
        mSend.setOnClickListener(v -> {
            if(onClickSendListener!=null){
                onClickSendListener.OnClickSend(mData);
            }
        });
    }

    public void showView(Activity activity,String address,String functionName,String value,String[] params){
        if(activity!=null){
            mParentView = (ViewGroup)activity.getWindow().getDecorView();
            if(TextUtils.isEmpty(address)){
                Toast.makeText(getContext(),"address is null",Toast.LENGTH_LONG).show();
                return;
            }
            this.mAddaressText = address;
            this.mFunctionText = functionName;
            this.mValueText = value;
            this.mParamsText = params;
            mAddress.setText(address);
            mValue.setText(value);
            StringBuilder paramsList = new StringBuilder();
            for(String param:params){
                paramsList.append(param);
                paramsList.append(",");
            }
            mParameters.setText(paramsList.toString());
            if(!TextUtils.isEmpty(functionName)){
                mFunctionName.setText(functionName);
            }
            mParentView.addView(this);
            ObjectAnimator translationYAnimation = ObjectAnimator.ofFloat(mContent, "translationY",mContent.getTranslationY(),0);
            translationYAnimation.setDuration(200);
            translationYAnimation.start();
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
                    mParentView.removeView(BottomTapView.this);
                    super.onAnimationEnd(animation);
                }
            });
        }
    }

    public void setData(SmartContractMessage smartContractMessage) {
        mData = smartContractMessage;

        mPrice = mData.priceModel.getQuote().getUSD().getPrice();

        BigInteger result = mData.gasLimit.multiply(mData.gasPrice);
        String amountETH = Convert.fromWei(result.toString(), Convert.Unit.ETHER).toPlainString();
        BigDecimal bg = new BigDecimal(Double.valueOf(amountETH) * mPrice);
        double realPrice = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

        mTvGasPrice.setText(getResources().getString(R.string.USD_price,String.valueOf(realPrice)));
        mArriveInTimes.setText(getResources().getString(R.string.ARRIVE_IN_MINS,String.valueOf(mData.gasPriceModel.fastWait)));

    }

    public interface OnClickSendListener{
        void OnClickSend(SmartContractMessage data);
    }
}
