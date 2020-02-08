package com.alice.customView;

import android.content.Context;
import android.widget.TextView;

import com.alice.R;
import com.alice.model.SmartContractMessage;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @Description:
 * @Author: zhanghaoran3
 * @CreateDate: 2020/1/18
 */
public class SendTransactionView extends BaseBottomView<SmartContractMessage> {
    private TextView mTvGasPrice;
    private TextView mArriveInTimes;
    private TextView mValue;
    private TextView mValuePrice;
    private TextView mAddress;

    public SendTransactionView(Context context) {
        super(context);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.view_send_transation_tap;
    }

    @Override
    protected void initView() {
        mTvGasPrice = findViewById(R.id.tv_gas_price);
        mArriveInTimes = findViewById(R.id.tv_arrive_times);
        mValue = findViewById(R.id.tv_eth_price);
        mValuePrice = findViewById(R.id.tv_real_price);
        mAddress = findViewById(R.id.tv_address);
    }

    @Override
    protected void updateViews(SmartContractMessage data) {
        double mPrice = data.priceModel.getQuote().getUSD().getPrice();
        BigInteger result = data.gasLimit.multiply(data.gasPrice);
        String amountETH = Convert.fromWei(result.toString(), Convert.Unit.ETHER).toPlainString();
        BigDecimal bg = new BigDecimal(Double.valueOf(amountETH) * mPrice);
        double realPrice = bg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

        double ethPrice = Double.parseDouble(data.value);
        BigDecimal ethPriceBg = new BigDecimal(ethPrice *mPrice);
        //保留小数点后3位
        double realEthPrice = ethPriceBg.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

        mTvGasPrice.setText(getResources().getString(R.string.USD_price,String.valueOf(realPrice)));
        mArriveInTimes.setText(getResources().getString(R.string.ARRIVE_IN_MINS,String.valueOf(mData.gasPriceModel.fastWait)));
        mValue.setText(data.value);
        mValuePrice.setText(getResources().getString(R.string.USD_price,String.valueOf(realEthPrice)));
        mAddress.setText(data.contractAddr);
    }
}
