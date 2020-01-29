package com.alice.manager;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alice.async.BaseListener;
import com.alice.async.MainHandler;
import com.alice.async.WorkThreadHandler;
import com.alice.model.BaseReponseBody;
import com.alice.model.GasPriceModel;
import com.alice.model.PriceModel;
import com.alice.model.SmartContractMessage;
import com.alice.net.Api;
import com.alice.net.ApiConstants;
import com.alice.source.BaseDataSource;
import com.alice.utils.Hex;
import com.alice.utils.LogUtil;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.orhanobut.hawk.Hawk;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
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
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import wallet.core.jni.CoinType;
import wallet.core.jni.Curve;
import wallet.core.jni.HDWallet;
import wallet.core.jni.Hash;
import wallet.core.jni.PrivateKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;

import static com.alice.config.Constants.KEY_ADDRESS;
import static com.alice.config.Constants.KEY_STORE_PATH;
import static com.alice.config.Constants.MEMORIZINGWORDS;

/**
 * create by zhhr on 2019/09/16
 */
public class Web3jManager {

    public final static String psw = "Alice";
    private CompositeDisposable mCompositeDisposable;

    private Credentials mCredentials;
    private Web3j web3j;
    private BaseDataSource dataSource;

    private int HARDENED_BIT = 0x80000000;

    public  static final String FAILED_SIGNATURE = "00000000000000000000000000000000000000000000000000000000000000000";

    private static final ObjectMapper objectMapper = new ObjectMapper();


    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private Web3jManager() {
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/da3717f25f824cc1baa32d812386d93f"));
        this.mCompositeDisposable = new CompositeDisposable();
        dataSource = new BaseDataSource(mCompositeDisposable);
        System.loadLibrary("TrustWalletCore");
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
            try{

                byte[] initialEntropy = new byte[16];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(initialEntropy);
                String memorizingWords = MnemonicUtils.generateMnemonic(initialEntropy);

                byte[] seed = MnemonicUtils.generateSeed(memorizingWords, "");
                Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(seed);
                final int[] path = {44 | HARDENED_BIT, 60 | HARDENED_BIT, 0 | HARDENED_BIT, 0, 0};
                Bip32ECKeyPair childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path);
                mCredentials = Credentials.create(childKeypair);

                File storagePath = Environment.getExternalStorageDirectory();
                WalletFile walletFile = Wallet.createStandard("", childKeypair);
                File filePath = new File(storagePath, "/keystore.json");

                ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
                writer.writeValue(filePath, walletFile);

                Hawk.put(MEMORIZINGWORDS, memorizingWords);
                Hawk.put(KEY_STORE_PATH, filePath.getAbsolutePath());

                LogUtil.d("create success!memorizingWords = " + memorizingWords + ",save path is" + storagePath.getAbsolutePath());
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


    public void importWallet(String memorizingWords,BaseListener<Credentials> listener) {
        checkNull(listener);
        WorkThreadHandler.getInstance().post(() -> {
            try {
                byte[] seed = MnemonicUtils.generateSeed(memorizingWords, "");
                Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(seed);
                final int[] path = {44 | HARDENED_BIT, 60 | HARDENED_BIT, 0 | HARDENED_BIT, 0, 0};
                Bip32ECKeyPair childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path);
                mCredentials = Credentials.create(childKeypair);

                Hawk.put(KEY_ADDRESS, mCredentials.getAddress());
                Hawk.put(MEMORIZINGWORDS, memorizingWords);

                LogUtil.d("Import success!Address is " + mCredentials.getAddress() + ",memorizingWords:" + memorizingWords);
                MainHandler.getInstance().post(() -> {
                    listener.OnSuccess(mCredentials);
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

    public static byte[] bytesFromSignature(Sign.SignatureData signature)
    {
        byte[] sigBytes = new byte[65];
        Arrays.fill(sigBytes, (byte) 0);

        try
        {
            System.arraycopy(signature.getR(), 0, sigBytes, 0, 32);
            System.arraycopy(signature.getS(), 0, sigBytes, 32, 32);
            sigBytes[64] = signature.getV();
        }
        catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }

        return sigBytes;
    }

    static byte[] getEthereumMessage(byte[] message) {
        byte[] prefix = getEthereumMessagePrefix(message.length);

        byte[] result = new byte[prefix.length + message.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(message, 0, result, prefix.length, message.length);

        return result;
    }
    static byte[] getEthereumMessagePrefix(int messageLength) {
        return MESSAGE_PREFIX.concat(String.valueOf(messageLength)).getBytes();
    }

    private static final String MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";

    /**
     * 签名
     * @param message
     * @param listener
     */
    public void sign(String message, BaseListener<String> listener) {
        checkNull(listener);
        if (TextUtils.isEmpty(message)) {
            listener.OnFailed(new IllegalAccessException("message i"));
            return;
        }
       /* byte[] sigBytes = FAILED_SIGNATURE.getBytes();
        String signString = Hex.hexToUtf8(message);*/
        //listener.OnSuccess(signString);

       /* Sign.SignatureData signatureData = Sign.signMessage(message.getBytes(), mCredentials.getEcKeyPair());
        byte[] sigBytes = bytesFromSignature(signatureData);*/
        try{
            byte[] messageBytes = getEthereumMessage(Numeric.hexStringToByteArray(message));
            String mnemonic = Hawk.get(MEMORIZINGWORDS);
            HDWallet newWallet = new HDWallet(mnemonic, "");
            PrivateKey pk = newWallet.getKeyForCoin(CoinType.ETHEREUM);
            byte[] digest = Hash.keccak256(messageBytes);
            byte[] sigBytes = pk.sign(digest, Curve.SECP256K1);
            String result = Numeric.toHexString(sigBytes);
            listener.OnSuccess(result);
        }catch (Exception e){
            listener.OnFailed(e);
        }
    }


    /**
     * 查询智能合约
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

    /**
     * 简易调用智能合约
     * @param contractAddr
     * @param functionName
     * @param value
     * @param params
     * @param gasPrice
     * @return
     */
    public String setSmartContract(String contractAddr,String functionName,String value,String[] params,BigInteger gasPrice) {
        String fromAddr = Hawk.get(KEY_ADDRESS);
        BigInteger nonce = getNonce(web3j, fromAddr);
        // 构建方法调用信息
        String method = functionName;

        // 构建输入参数
        List<Type> inputArgs = new ArrayList<>();
        for(String param:params){
            inputArgs.add(new Utf8String(param));
        }
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

    /**
     * 获取智能合约信息
     * @param contractAddr
     * @param functionName
     * @param value
     * @param params
     * @param listener
     */
    public void loadSmartContractSet(String contractAddr, String functionName, String value, String[] params,BaseListener<SmartContractMessage> listener) {
        checkNull(listener);
        WorkThreadHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                try{
                    String fromAddr = Hawk.get(KEY_ADDRESS);
                    BigInteger nonce = getNonce(web3j, fromAddr);
                    // 构建方法调用信息
                    String method = functionName;
                    // 构建输入参数
                    List<Type> inputArgs = new ArrayList<>();
                    for(String param:params){
                        inputArgs.add(new Utf8String(param));
                    }
                    // 合约返回值容器
                    List<TypeReference<?>> outputArgs = new ArrayList<>();
                    String funcABI = FunctionEncoder.encode(new Function(method, inputArgs, outputArgs));

                    //调用接口拿到gasPrice信息
                    GasPriceModel gasPriceModel = dataSource.executeSync(dataSource.getService(Api.class).getGasPriceModelSync(ApiConstants.GAS_PRICE));

                    //调用接口拿到美元换算信息
                    BaseReponseBody<PriceModel> baseReponseBody = dataSource.executeSync(dataSource.getService(Api.class).getPriceModelSync(ApiConstants.CONVERT, 2, 1, "USD"));
                    PriceModel priceModel = baseReponseBody.data.get(0);
                    //设置gasPrice
                    BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();
                    if(gasPriceModel!=null){
                        BigDecimal amountGwei = BigDecimal.valueOf(gasPriceModel.average);
                        gasPrice = Convert.toWei(amountGwei, Convert.Unit.GWEI).toBigInteger();
                    }

                    Transaction transaction = Transaction.createFunctionCallTransaction(fromAddr, nonce, gasPrice, null, contractAddr, funcABI);
                    BigInteger gasLimit = getTransactionGasLimit(web3j, transaction);
                    //设置参数
                    SmartContractMessage smartContractMessage = new SmartContractMessage();
                    smartContractMessage.gasLimit = gasLimit;
                    smartContractMessage.gasPrice = gasPrice;
                    smartContractMessage.priceModel = priceModel;
                    smartContractMessage.gasPriceModel = gasPriceModel;
                    smartContractMessage.funcABI = funcABI;
                    smartContractMessage.contractAddr = contractAddr;
                    smartContractMessage.fromAddr = fromAddr;
                    smartContractMessage.nonce = nonce;

                    MainHandler.getInstance().post(() -> listener.OnSuccess(smartContractMessage));
                }catch (Exception e){
                    MainHandler.getInstance().post(() -> listener.OnFailed(e));
                }
            }
        });
    }

    /**
     * 调用智能合约
     * @param smartContractMessage
     * @param listener
     */
    public void setSmartContract(SmartContractMessage smartContractMessage, BaseListener<String> listener) {
        checkNull(listener);
        WorkThreadHandler.getInstance().post(() -> {
            try{
                // 获得余额
                BigDecimal ethBalance = getBalance(web3j, smartContractMessage.fromAddr);
                BigInteger balance = Convert.toWei(ethBalance, Convert.Unit.ETHER).toBigInteger();
                if (balance.compareTo(smartContractMessage.gasLimit) < 0) {
                    throw new RuntimeException("手续费不足，请核实");
                }
                String resultHashCode =  signAndSend(web3j, smartContractMessage.nonce, smartContractMessage.gasPrice, smartContractMessage.gasLimit, smartContractMessage.contractAddr, BigInteger.ZERO, smartContractMessage.funcABI, ChainId.RINKEBY, mCredentials.getEcKeyPair().getPrivateKey().toString(16));
                MainHandler.getInstance().post(() -> listener.OnSuccess(resultHashCode));
            }catch (Exception e){
                MainHandler.getInstance().post(() -> listener.OnFailed(e));
            }
        });
    }

    /**
     * 获取gaslimit
     * @param web3j
     * @param transaction
     * @return
     */
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

    /**
     * 同步查询余额
     * @param web3j
     * @param address
     * @return
     */
    public BigDecimal getBalance(Web3j web3j, String address) {
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            return Convert.fromWei(new BigDecimal(ethGetBalance.getBalance()),Convert.Unit.ETHER);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 签名后发送智能合约请求
     * @param web3j
     * @param nonce
     * @param gasPrice
     * @param gasLimit
     * @param to
     * @param value
     * @param data
     * @param chainId
     * @param privateKey
     * @return
     */
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

    /**
     * 检查空
     * @param listener
     */
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
