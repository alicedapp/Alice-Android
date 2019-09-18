package com.alice.manager;

import android.os.Environment;

import com.alice.async.MainHandler;
import com.alice.async.WorkThreadHandler;
import com.alice.utils.LogUtil;
import com.orhanobut.hawk.Hawk;

import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.File;

import static com.alice.config.Constants.MEMORIZINGWORDS;

/**
 * create by zhhr on 2019/09/16
 */
public class Web3jManager {

    public final static String psw = "zhhr1122";

    private Web3j web3j;

    private Web3jManager(){
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/da3717f25f824cc1baa32d812386d93f"));
    }

    private static class Web3jManagerHolder{
        private final static Web3jManager instance=new Web3jManager();
    }

    public static Web3jManager getInstance(){
        return Web3jManagerHolder.instance;
    }

    public void createWallet(OnCreateWalletListener listener){
        if(listener == null){
            LogUtil.e("OnCreateWalletListener is null");
            return;
        }
        WorkThreadHandler.getInstance().post(() -> {
            if(web3j == null){
                MainHandler.getInstance().post(() -> {
                    listener.OnFailed(new IllegalAccessException("Web3j build failed"));
                });
                return;
            }
            File path = Environment.getExternalStorageDirectory();
            Bip39Wallet wallet;
            try {
                wallet = WalletUtils.generateBip39Wallet(psw, path);
                String memorizingWords = wallet.getMnemonic();
                Hawk.put(MEMORIZINGWORDS,memorizingWords);
                Credentials credentials = WalletUtils.loadBip39Credentials(psw, wallet.getMnemonic());
                MainHandler.getInstance().post(() -> {
                    listener.OnSuccess(credentials);
                });
            } catch (Exception e) {
                LogUtil.d(e.getMessage());
                MainHandler.getInstance().post(() -> {
                    listener.OnFailed(e);
                });
            }

        });

    }

    public void importWallet() {

    }

    public interface OnCreateWalletListener{
        void OnSuccess(Credentials credentials);
        void OnFailed(Exception e);
    }

}
