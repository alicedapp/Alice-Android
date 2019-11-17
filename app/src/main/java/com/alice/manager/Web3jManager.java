package com.alice.manager;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alice.async.BaseListener;
import com.alice.async.MainHandler;
import com.alice.async.WorkThreadHandler;
import com.alice.utils.LogUtil;
import com.facebook.react.bridge.ReadableArray;
import com.orhanobut.hawk.Hawk;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.alice.config.Constants.KEY_ADDRESS;
import static com.alice.config.Constants.KEY_STORE_PATH;
import static com.alice.config.Constants.MEMORIZINGWORDS;
import static org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction;

/**
 * create by zhhr on 2019/09/16
 */
public class Web3jManager {

    public final static String psw = "Alice";
    private CompositeDisposable mCompositeDisposable;

    private Credentials mCredentials;
    private Web3j web3j;

    private Web3jManager() {
        web3j = Web3j.build(new HttpService("https://ropsten.infura.io/da3717f25f824cc1baa32d812386d93f"));
        this.mCompositeDisposable = new CompositeDisposable();
    }

    private static class Web3jManagerHolder {
        private final static Web3jManager instance = new Web3jManager();
    }

    public static Web3jManager getInstance() {
        return Web3jManagerHolder.instance;
    }

    public void createWallet(BaseListener<String> listener) {
        checkNull(listener);
        WorkThreadHandler.getInstance().post(() -> {
            File path = Environment.getExternalStorageDirectory();
            Bip39Wallet wallet;
            try {
                wallet = WalletUtils.generateBip39Wallet(psw, path);
                String memorizingWords = wallet.getMnemonic();
                String filePath = path + "/" + wallet.getFilename();
                Hawk.put(MEMORIZINGWORDS, memorizingWords);
                Hawk.put(KEY_STORE_PATH, filePath);
                LogUtil.d("create success!memorizingWords = " + memorizingWords + ",save path is" + path.getAbsolutePath());
                MainHandler.getInstance().post(() -> {
                    listener.OnSuccess(memorizingWords);
                });
            } catch (Exception e) {
                LogUtil.d(e.getMessage());
                MainHandler.getInstance().post(() -> {
                    listener.OnFailed(e);
                });
            }
        });

    }

    public void importWallet(BaseListener<Credentials> listener) {
        checkNull(listener);
        WorkThreadHandler.getInstance().post(() -> {
            try {
                String filePath = Hawk.get(KEY_STORE_PATH, "");
                String memorizingWords = Hawk.get(MEMORIZINGWORDS);

                File file = new File(filePath);
                mCredentials = WalletUtils.loadCredentials(psw, file);
                Hawk.put(KEY_ADDRESS, mCredentials.getAddress());

                LogUtil.d("Import success!Address is " + mCredentials.getAddress() + ",memorizingWords:" + memorizingWords);
                MainHandler.getInstance().post(() -> {
                    listener.OnSuccess(mCredentials);
                });
            } catch (FileNotFoundException e) {
                MainHandler.getInstance().post(() -> {
                    listener.OnFailed(new IllegalAccessException("Please create wallet first"));
                });
            } catch (Exception e) {
                MainHandler.getInstance().post(() -> {
                    listener.OnFailed(e);
                });
            }

        });
    }

    /**
     * check local address balance
     *
     * @param listener
     */
    public void checkBalances(BaseListener<EthGetBalance> listener) {
        checkNull(listener);
        String address = Hawk.get(KEY_ADDRESS);
        LogUtil.d("Get address success! The address is " + address);
        if (TextUtils.isEmpty(address)) {
            listener.OnFailed(new IllegalAccessException("There is no address!Please Import keystore!"));
            return;
        }
        Disposable disposable = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest"))
                .flowable()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::OnSuccess, listener::OnFailed);
        mCompositeDisposable.add(disposable);
    }

    /**
     * check the address
     *
     * @param address
     * @param listener
     */
    public void checkBalances(String address, BaseListener<EthGetBalance> listener) {
        checkNull(listener);
        LogUtil.d("Get address success! The address is " + address);
        if (TextUtils.isEmpty(address)) {
            listener.OnFailed(new IllegalAccessException("There is no address!Please input address!"));
            return;
        }
        Disposable disposable = web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest"))
                .flowable()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::OnSuccess, listener::OnFailed);
        mCompositeDisposable.add(disposable);
    }

    public void transfer(String address, String value, BaseListener<TransactionReceipt> listener) {
        checkNull(listener);
        if (mCredentials == null) {
            listener.OnFailed(new IllegalArgumentException("please import key first!"));
        }
        try {
            BigDecimal valueWei = Convert.toWei(value, Convert.Unit.ETHER);
            Disposable disposable = Transfer.sendFunds(web3j, mCredentials, address, valueWei, Convert.Unit.WEI)
                    .flowable()
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(listener::OnSuccess, listener::OnFailed);
            mCompositeDisposable.add(disposable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sign(String message, BaseListener<String> listener) {
        checkNull(listener);
        if (TextUtils.isEmpty(message)) {
            listener.OnFailed(new IllegalAccessException("message i"));
            return;
        }
        if (mCredentials == null) {
            listener.OnFailed(new IllegalArgumentException("please import key first!"));
        }
       /* WorkThreadHandler.getInstance().post(() -> {
            try {
                BigInteger value = Convert.toWei("5", Convert.Unit.ETHER).toBigInteger();
                EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount("0x02be5ade098ed915b163f92bd76194877a66783a", DefaultBlockParameterName.LATEST).send();
                BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                        nonce, GAS_PRICE, GAS_LIMIT,  "0x02be5ade098ed915b163f92bd76194877a66783a", value);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, mCredentials);
                String hexValue = Numeric.toHexString(signedMessage);
                System.out.println("hexValue = " + hexValue);
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
                System.out.println("ethSendTransaction.getTransactionHash() = "+ethSendTransaction.getTransactionHash());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });*/
        String plainMessage = "Hello world";
        byte[] hexMessage = Hash.sha3(plainMessage.getBytes());
        Sign.SignatureData signMessage = Sign.signMessage(hexMessage, mCredentials.getEcKeyPair());
        try {
            String pubKey = Sign.signedMessageToKey(hexMessage, signMessage).toString(16);
            Log.d("zhhr1122","pubKey = " + pubKey);
        } catch (SignatureException e) {
            e.printStackTrace();
        }

    }


    public void callSmartContractFunction(String contractAddr, String funcABI, String functionName, ReadableArray params, String value, String data,BaseListener<EthCall> listener){
        checkNull(listener);
        String address = Hawk.get(KEY_ADDRESS);
        List<Type> paramList = new ArrayList<>();
        Function function = new Function("setOrder", Arrays.<Type>asList(new Utf8String("Mark"), new Utf8String("HotDog")), Collections.<TypeReference<?>>emptyList());
        String encodedFunction = FunctionEncoder.encode(function);
        Transaction transaction = createEthCallTransaction(address, "0x68F7202dcb25360FA6042F6739B7F6526AfcA66E", data);
        Disposable disposable = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::OnSuccess, listener::OnFailed);
        mCompositeDisposable.add(disposable);
    }

    //获取账户的Nonce
    public static BigInteger getNonce(Web3j web3j, String addr) {
        try {
            EthGetTransactionCount getNonce = web3j.ethGetTransactionCount(addr, DefaultBlockParameterName.PENDING).send();
            if (getNonce == null){
                throw new RuntimeException("net error");
            }
            return getNonce.getTransactionCount();
        } catch (IOException e) {
            throw new RuntimeException("net error");
        }
    }

    private void checkNull(BaseListener listener) {
        if (listener == null) {
            LogUtil.e("listener is null");
            return;
        }
        if (web3j == null) {
            listener.OnFailed(new IllegalArgumentException("Web3j build failed"));
            return;
        }
    }

    public void clear() {
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }


}
