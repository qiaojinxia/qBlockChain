package com.qjx.blockchain.qblockchain.basemodel;

import com.alibaba.fastjson.JSON;
import com.qjx.blockchain.qblockchain.commonutils.FileUtil;
import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by caomaoboy 2019-11-03
 **/
@Component
public class Wallet {
    public final static Logger logger = LoggerFactory.getLogger(Wallet.class);
    /**
     * 保存钱包的地址 自己改
     */
    static {
        WALLETFILE = FileUtil.getConTextPath()+"wallet.json";

    }
    private static  String WALLETFILE ;

    private Wallet(){
        this.wallet =new HashMap<String, WalletStruts>();
        this.size = 0 ;

    }
    private Integer size;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Map<String, WalletStruts> getWallet() {
        return wallet;
    }

    public void setWallet(Map<String, WalletStruts> wallet) {
        this.wallet = wallet;
    }

    @Autowired
    private static Map<String,WalletStruts> wallet;
    /**
     * 从文件中读取脚在钱包内容
     * @return
     */
    @Bean
    public static Wallet loadMyWallet(){
        String contet ="";
        if(FileUtil.isFileExist(WALLETFILE))
            contet = FileUtil.ReadFile(WALLETFILE);
        //如果文件没有则初始化一个新的钱包
        Wallet walletcoent=null;
        if(StringUtils.isEmpty(contet)){
            walletcoent = new Wallet();
            walletcoent.size = 0;
        }else{
            walletcoent =  JSON.parseObject(contet,Wallet.class);
        }

        if(!ObjectUtils.notEmpty(walletcoent)){
            walletcoent = new Wallet();
            walletcoent.size = 0;
        }

        return walletcoent;
    }

    /**
     * 保存我的钱包 到 wallet.json中
     * @param wallet
     */
    public static void saveMyWallet(Wallet wallet){
        String content = JSON.toJSONString(wallet);
        FileUtil.fileLinesWrite(WALLETFILE,content,false);
        System.out.println("Wallet saved successfully!" +content);
    }

    /**
     * 从钱包集合中按照名字遍历获得钱包
     * @param waName
     * @return
     */
    public WalletStruts getByWalletName(String waName ){
        Iterator<Map.Entry<String, WalletStruts>> waIter = this.wallet.entrySet().iterator();
        while(waIter.hasNext()){
           Map.Entry<String, WalletStruts> waNext = waIter.next();
            if(waNext.getKey().equals(waName))
                return waNext.getValue();
        }
        return null;
    }
    public WalletStruts getByWalletName(String waName,boolean isCreate){
        WalletStruts res = getByWalletName(waName);
        if(null == res && isCreate ==true){
            return newAccount(waName);
        }return res;
    }
    /**
     * 生成钱包地址放入map 然后返回 钱包
     * @param waName
     * @return
     */
    public  WalletStruts newAccount(String waName){
        if(!ObjectUtils.notEmpty(this.wallet))
            throw new IllegalArgumentException("Wallet must be init first");
        WalletStruts newwa = getByWalletName(waName);
        if(null != newwa)
            return newwa;
        try{
            newwa = WalletStruts.generateWallet();
        } catch (Exception e) {
            System.out.println("Failed to generate Wallet !");
            e.printStackTrace();
        }
        this.wallet.put(waName,newwa);
        size ++;
        return newwa;

    }


}
