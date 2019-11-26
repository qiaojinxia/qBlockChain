package com.qjx.blockchain.qblockchain;

import com.alibaba.fastjson.JSON;
import com.qjx.blockchain.qblockchain.basemodel.*;
import com.qjx.blockchain.qblockchain.blockprocessing.BlockServices;
import com.qjx.blockchain.qblockchain.blockprocessing.TransactionsPool;
import com.qjx.blockchain.qblockchain.p2pnetwork.P2PController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 这里的@RestController   相当于@ResponseBody + @Controller
 */
@RestController
public class BlockChainController {
    @RequestMapping(value = "/blockChain",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    public String helloSpringBoot() throws Exception {
        Wallet wallet = Wallet.loadMyWallet();
        BlockServices blockServices =new BlockServices(BlockChain.loadBlockChain().getBlockchain());
        TransactionsPool tp = new TransactionsPool(blockServices.getBlockChain());
        //新建一笔交易
//        Transaction cc = blockServices.newTransaction(wallet.getByWalletName("zhangsan").getPublicKey(),wallet.getByWalletName("lisi").getAddress(),new BigDecimal("5"));
//        Transaction.SignTransaction(cc,new String(wallet.getByWalletName("zhangsan").getPrivateKey()),blockServices.getBlockChain());
//        //cc.getVout().get(1).setN(87);
//        Transaction.VerifyTransaction(cc,blockServices.getBlockChain());
//        tp.addTransPlool(cc);
        //挖矿生成一笔交易
        List<Transaction> a =tp.getallTrans();
        Block packageblock = blockServices.PackageBlock(a);
        System.out.println(JSON.toJSONString(packageblock));
        wallet.newAccount("zhangsan");
        Block gen= blockServices.Mineral(wallet.getByWalletName("zhangsan").getAddress(),"caomaoboy",packageblock);
        P2PController bc = new P2PController(blockServices,wallet,tp);
        bc.addBlock(gen);
        return blockServices.getBlockChainjson();
    }
}