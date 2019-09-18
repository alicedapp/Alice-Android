package com.alice.manager;

import android.os.Environment;
import android.text.TextUtils;

import com.alice.async.BaseListener;
import com.alice.async.MainHandler;
import com.alice.async.WorkThreadHandler;
import com.alice.utils.LogUtil;
import com.orhanobut.hawk.Hawk;

import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.alice.config.Constants.KEY_ADDRESS;
import static com.alice.config.Constants.KEY_STORE_PATH;
import static com.alice.config.Constants.MEMORIZINGWORDS;

/**
 * create by zhhr on 2019/09/16
 */
public class Web3jManager {

    public final static String psw = "Alice";
    private CompositeDisposable mCompositeDisposable;

    private Web3j web3j;

    private Web3jManager(){
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/da3717f25f824cc1baa32d812386d93f"));
        this.mCompositeDisposable = new CompositeDisposable();
    }

    private static class Web3jManagerHolder{
        private final static Web3jManager instance=new Web3jManager();
    }

    public static Web3jManager getInstance(){
        return Web3jManagerHolder.instance;
    }

    public void createWallet(BaseListener<String> listener){
        checkNull(listener);
        WorkThreadHandler.getInstance().post(() -> {
            File path = Environment.getExternalStorageDirectory();
            Bip39Wallet wallet;
            try {
                wallet = WalletUtils.generateBip39Wallet(psw, path);
                String memorizingWords = wallet.getMnemonic();
                String filePath = path + "/" +wallet.getFilename();
                Hawk.put(MEMORIZINGWORDS,memorizingWords);
                Hawk.put(KEY_STORE_PATH,filePath);
                LogUtil.d("create success!memorizingWords = "+ memorizingWords +",save path is" + path.getAbsolutePath());
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
                String filePath = Hawk.get(KEY_STORE_PATH);
                File file = new File(filePath);
                if(!file.exists()){
                    MainHandler.getInstance().post(() -> {
                        listener.OnFailed(new IllegalAccessException("Please create wallet first"));
                    });
                }
                Credentials credentials = WalletUtils.loadCredentials(psw, file);
                Hawk.put(KEY_ADDRESS,credentials.getAddress());
                LogUtil.d("Import success!Address is " + credentials.getAddress());
                MainHandler.getInstance().post(() -> {
                    listener.OnSuccess(credentials);
                });
            }catch (Exception e) {
                MainHandler.getInstance().post(() -> {
                    listener.OnFailed(e);
                });
            }

        });
    }

    /**
     * check local address balance
     * @param listener
     */
    public void checkBalances(BaseListener<EthGetBalance> listener) {
        checkNull(listener);
        String address = Hawk.get(KEY_ADDRESS);
        LogUtil.d("Get address success! The address is " + address);
        if(TextUtils.isEmpty(address)){
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
     * @param address
     * @param listener
     */
    public void checkBalances(String address,BaseListener<EthGetBalance> listener) {
        checkNull(listener);
        LogUtil.d("Get address success! The address is " + address);
        if(TextUtils.isEmpty(address)){
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

    private void checkNull(BaseListener listener) {
        if(listener == null){
            LogUtil.e("listener is null");
            return;
        }
        if(web3j == null){
            listener.OnFailed(new IllegalAccessException("Web3j build failed"));
            return;
        }
    }

    public void clear() {
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }


}
