package com.alice.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alice.R;
import com.orhanobut.hawk.Hawk;

import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.alice.config.Constants.MEMORIZINGWORDS;

public class MainActivity extends Activity {
    private Web3j web3j;
    private Credentials credentials;
    public final static String psw = "zhhr1122";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Hawk.init(this).build();
        web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/da3717f25f824cc1baa32d812386d93f"));//
    }

    public void toRn(View view) {
        startActivity(new Intent(MainActivity.this,RnActivity.class));
    }

    public void Import(View view) {
        try {
            credentials = WalletUtils.loadBip39Credentials(psw, "head sphere silent quality note cargo fruit soap knife slide tell repair");
            Log.d("zhhr1122","credentials.getAddress()= " + credentials.getAddress());
            credentials.getAddress();
            String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);
            String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
            Toast.makeText(MainActivity.this,"publicKey ="+ publicKey + ",privateKey = "+ privateKey,Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.d("zhhr1122","创建钱包失败");
        }
    }

    public void Get(View view) {
        File  path = Environment.getExternalStorageDirectory();
        Bip39Wallet wallet;
        try {
            wallet = WalletUtils.generateBip39Wallet(psw, path);
            String keyStoreKey = wallet.getFilename();
            String memorizingWords = wallet.getMnemonic();
            Hawk.put(MEMORIZINGWORDS,memorizingWords);
            Credentials credentials = WalletUtils.loadBip39Credentials(psw, wallet.getMnemonic());
            credentials.getAddress();
            String publicKey = credentials.getEcKeyPair().getPublicKey().toString(16);
            String privateKey = credentials.getEcKeyPair().getPrivateKey().toString(16);
            Toast.makeText(MainActivity.this,"memorizingWords ="+ memorizingWords,Toast.LENGTH_LONG).show();
            Log.d("zhhr1122","credentials.getAddress()= " + credentials.getAddress());
            Log.d("zhhr1122","keyStoreKey = "+ keyStoreKey + ",memorizingWords = "+ memorizingWords + ",publicKey ="+ publicKey + ",privateKey = "+ privateKey);
        } catch (Exception e) {
            Log.d("zhhr1122","创建钱包失败" + e.toString());
        }
    }

    public void getBalance(View view) {
        conectETHclient();
    }


    private void conectETHclient() {
        try{
            String address = "0x45be6de2459438ab35ffd415ac2d04d27a08e61b";//等待查询余额的地址
            web3j.ethGetBalance(address, DefaultBlockParameter.valueOf("latest"))
                    .flowable()
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(balance -> {
                        String blanceETH = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER).toPlainString().concat("ether");
                        Toast.makeText(MainActivity.this, "blanceETH =" + blanceETH, Toast.LENGTH_LONG).show();
                    }, throwable -> Toast.makeText(MainActivity.this, throwable.toString() ,Toast.LENGTH_LONG).show());
        }catch (Exception e){
            Toast.makeText(MainActivity.this,"失败" + e.toString(),Toast.LENGTH_LONG).show();
        }

    }

    public void Transfer(View view) {
        try{
            String address_to  = "0xA60f8a3E6586aA590a4AD9EE0F264A1473Bab7cB";
          /*  if(web3j == null){
                Toast.makeText(MainActivity.this,"请先加载钱包",Toast.LENGTH_LONG).show();
                return;
            }
            if(credentials == null){
                Toast.makeText(MainActivity.this,"请先加载钱包",Toast.LENGTH_LONG).show();
                return;
            }*/
            String memorizingWords = Hawk.get(MEMORIZINGWORDS);
            if(memorizingWords == null){
                Toast.makeText(MainActivity.this,"请先加载钱包",Toast.LENGTH_LONG).show();
                return;
            }
            Web3j web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/da3717f25f824cc1baa32d812386d93f"));
            Credentials credentials = WalletUtils.loadBip39Credentials(psw, memorizingWords);
            Toast.makeText(MainActivity.this,"memorizingWords ="+ memorizingWords,Toast.LENGTH_LONG).show();
            Log.d("zhhr1122","credentials.getAddress()= " + credentials.getAddress());
            Transfer.sendFunds(web3, credentials, address_to, Convert.toWei("0.1", Convert.Unit.ETHER), Convert.Unit.WEI)
                    .flowable()
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(send -> {
                        String text = "Transaction complete:" + "trans hash=" + send.getTransactionHash() + "from :" + send.getFrom() + "to:" + send.getTo() + "gas used=" + send.getGasUsed() + "status: " + send.getStatus();
                        Log.d("zhhr1122", text);
                        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
                    }, throwable -> Toast.makeText(MainActivity.this, throwable.toString(), Toast.LENGTH_LONG).show());
        }catch (Exception e){
            Toast.makeText(MainActivity.this,"交易失败" + e.toString()+",credentials.getAddress()= " + credentials.getAddress(),Toast.LENGTH_LONG).show();
        }
    }
}
