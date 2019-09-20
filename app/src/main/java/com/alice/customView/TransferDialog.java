package com.alice.customView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alice.R;
import com.alice.model.PriceModel;
import com.alice.net.Api;
import com.alice.net.ApiConstants;
import com.alice.net.RequestCallback;
import com.alice.source.BaseDataSource;
import com.alice.utils.LogUtil;
import com.alice.utils.UnitUtlis;

import org.web3j.utils.Convert;

import java.math.BigDecimal;


public class TransferDialog extends Dialog {
    private TextView btnPositive;
    private TextView btnNegative;
    private EditText etAddress;
    private EditText etValue;
    private ImageView ivClearAddress;
    private ImageView ivClearValue;
    private TextView tvPrice;
    private int paddingOffset;
    private OnClickConfirmListener onClickConfirmListener;
    private BaseDataSource dataSource;

    private double mPrice = 0.0;

    private String mAddress = "";
    private String mValue = "";

    public TransferDialog(Context context) {
        super(context, R.style.MyDialog);
        dataSource = new BaseDataSource();
    }

    public TransferDialog(Context context,String address,String value) {
        super(context, R.style.MyDialog);
        this.mAddress = address;
        dataSource = new BaseDataSource();
        String realValue = UnitUtlis.hex2decimal(value.substring(2));
        BigDecimal bigDecimal = Convert.fromWei(realValue,Convert.Unit.ETHER);
        this.mValue = bigDecimal.toString();
    }

    public void setOnClickConfirmListener(OnClickConfirmListener onClickConfirmListener) {
        this.onClickConfirmListener = onClickConfirmListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_transfer);
        btnPositive = findViewById(R.id.ok);
        btnNegative = findViewById(R.id.cancel);
        etAddress = findViewById(R.id.et_address);
        etValue = findViewById(R.id.et_value);
        tvPrice = findViewById(R.id.tv_price);
        etAddress.setText(mAddress);
        etValue.setText(mValue);
        ivClearAddress  = findViewById(R.id.iv_clear_address);
        ivClearValue = findViewById(R.id.iv_clear_value);
        paddingOffset = getContext().getResources().getDimensionPixelOffset(R.dimen.view_dimen_90);

        Window dialogWindow = this.getWindow();
        dialogWindow.getDecorView().setPadding(paddingOffset, 0, paddingOffset, 0);
        WindowManager.LayoutParams wlp = dialogWindow.getAttributes();
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(wlp);

        ivClearValue.setOnClickListener(v -> etValue.setText(""));
        ivClearAddress.setOnClickListener(v -> etAddress.setText(""));
        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!TextUtils.isEmpty(etValue.getText().toString())){
                    try{
                        double price = Double.parseDouble(etValue.getText().toString());
                        BigDecimal bg = new BigDecimal(price *mPrice);
                        //保留小数点后3位
                        double realPrice = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                        tvPrice.setText(getContext().getString(R.string.CNY_price,realPrice +""));
                    }catch (Exception e){
                        LogUtil.e(e.toString());
                    }
                }else{
                    tvPrice.setText(getContext().getString(R.string.CNY_price,"0.00"));
                }
            }
        });

    }

    /**
     * 调用完Builder类的create()方法后显示该对话框的方法
     */
    @Override
    public void show() {
        super.show();
        show(this);
    }

    private void show(TransferDialog mDialog) {
        btnNegative.setOnClickListener(v -> dismiss());
        if (onClickConfirmListener!=null) {
            btnPositive.setOnClickListener(v -> {
                String address = etAddress.getText().toString();
                String value = etValue.getText().toString();
                if(TextUtils.isEmpty(address)){
                    onClickConfirmListener.onAddressError("address is null");
                    return;
                }
                if(TextUtils.isEmpty(value)){
                    onClickConfirmListener.onValueError("value is null");
                    return;
                }
                onClickConfirmListener.onClickConfirm(address,value);
                dismiss();
            });
        }
        dataSource.execute(dataSource.getService(Api.class).getPriceModel(ApiConstants.CONVERT, 2, 1, "CNY"), new RequestCallback<PriceModel>() {

            @Override
            public void onSuccess(PriceModel priceModel) {
                mPrice = priceModel.getQuote().getCNY().getPrice();
            }

            @Override
            public void OnFailed(Throwable throwable) {
                LogUtil.d(throwable.toString());
            }
        });
    }

    public interface OnClickConfirmListener{
        void onClickConfirm(String address,String value);
        void onAddressError(String message);
        void onValueError(String message);
    }
}
