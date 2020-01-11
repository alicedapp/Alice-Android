package com.alice.manager;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alice.async.BaseListener;
import com.alice.async.MainHandler;
import com.alice.async.WorkThreadHandler;
import com.alice.utils.Hex;
import com.alice.utils.LogUtil;
import com.facebook.react.bridge.ReadableArray;
import com.orhanobut.hawk.Hawk;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

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
import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
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
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/da3717f25f824cc1baa32d812386d93f"));
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

    public void callSmartContractFunction(String contractAddr,BaseListener<EthCall> listener){
        checkNull(listener);
        String address = Hawk.get(KEY_ADDRESS);
        String methodName = "getMessage";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {};
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);
        Transaction transaction = createEthCallTransaction(address, contractAddr, encodedFunction);
        Disposable disposable = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)
                .flowable()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ethCall -> {
                    List<Type> someTypes = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                    Log.d("zhhr1122","someTypes = " + someTypes.get(0).getTypeAsString());
                }, throwable -> {
                    Log.d("zhhr1122",throwable.toString());
                });
        mCompositeDisposable.add(disposable);
    }

    /**
     * 查询代币名称
     */
    public String getMessageName(String contractAddr) {
        String address = Hawk.get(KEY_ADDRESS);
        String methodName = "getMessage";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(address, contractAddr, data);

        EthCall ethCall = null;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
        if (null == results || results.size() <= 0) {
            return "";
        }
        return results.get(0).getValue().toString();
    }

    public String setMessage(String contractAddr,String message) {
        String fromAddr = Hawk.get(KEY_ADDRESS);
        BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
        BigInteger nonce = getNonce(web3j, fromAddr);
        // 构建方法调用信息
        String method = "setMessage";

        // 构建输入参数
        List<Type> inputArgs = new ArrayList<>();
        inputArgs.add(new Utf8String(message));

        // 合约返回值容器
        List<TypeReference<?>> outputArgs = new ArrayList<>();

        String funcABI = FunctionEncoder.encode(new Function(method, inputArgs, outputArgs));

        Transaction transaction = Transaction.createFunctionCallTransaction(fromAddr, nonce, gasPrice, null, contractAddr, funcABI);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, null, contractAddr, null, funcABI);

        BigInteger gasLimit = getTransactionGasLimit(web3j, transaction);

        // 获得余额
        BigDecimal ethBalance = getBalance(web3j, fromAddr);
        BigInteger balance = Convert.toWei(ethBalance, Convert.Unit.ETHER).toBigInteger();

        if (balance.compareTo(gasLimit) < 0) {
            throw new RuntimeException("手续费不足，请核实");
        }

        return signAndSend(web3j, nonce, gasPrice, gasLimit, contractAddr, BigInteger.ZERO, funcABI, ChainId.RINKEBY, mCredentials.getEcKeyPair().getPrivateKey().toString(16));
    }

    public BigInteger getTransactionGasLimit(Web3j web3j, Transaction transaction) {
        try {
            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            if (ethEstimateGas.hasError()){
                throw new RuntimeException(ethEstimateGas.getError().getMessage());
            }
            return ethEstimateGas.getAmountUsed();
        } catch (IOException e) {
            throw new RuntimeException("net error");
        }
    }

    public BigDecimal getBalance(Web3j web3j, String address) {
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            return Convert.fromWei(new BigDecimal(ethGetBalance.getBalance()),Convert.Unit.ETHER);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String signAndSend(Web3j web3j, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data, byte chainId, String privateKey) {
        String txHash = "";
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
        if (privateKey.startsWith("0x")){
            privateKey = privateKey.substring(2);
        }

        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);

        byte[] signMessage;
        if (chainId > ChainId.NONE){
            signMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        } else {
            signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        }

        String signData = Numeric.toHexString(signMessage);
        if (!"".equals(signData)) {
            try {
                EthSendTransaction send = web3j.ethSendRawTransaction(signData).send();
                txHash = send.getTransactionHash();
                //System.out.println(JSON.toJSONString(send));
            } catch (IOException e) {
                throw new RuntimeException("交易异常");
            }
        }
        return txHash;
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
