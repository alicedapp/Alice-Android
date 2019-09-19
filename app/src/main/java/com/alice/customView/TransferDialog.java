package com.alice.customView;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alice.R;




public class TransferDialog extends Dialog {
    private TextView btnPositive;
    private TextView btnNegative;
    private EditText etAddress;
    private EditText etValue;
    private ImageView ivClearAddress;
    private ImageView ivClearValue;
    private int paddingOffset;
    private OnClickConfirmListener onClickConfirmListener;

    private String mAddress = "";
    private String mValue = "";

    public TransferDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    public TransferDialog(Context context,String address,String value) {
        super(context, R.style.MyDialog);
        this.mAddress = address;
        this.mValue = value;
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
    }

    public interface OnClickConfirmListener{
        void onClickConfirm(String address,String value);
        void onAddressError(String message);
        void onValueError(String message);
    }
}
