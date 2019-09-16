package com.alice.manager;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

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

    public void createWallet(){
        if(web3j == null){
            return;
        }

    }

}
