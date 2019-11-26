package com.qjx.blockchain.qblockchain.cli;

import com.qjx.blockchain.qblockchain.basemodel.BlockChain;
import com.qjx.blockchain.qblockchain.blockprocessing.BlockServices;
import com.qjx.blockchain.qblockchain.blockprocessing.TransactionsPool;

public class Main {
public static  BlockServices blockServices = null;
    public static  TransactionsPool tp = null;
    public static void main(String[] args) {
        blockServices =new BlockServices(BlockChain.loadBlockChain().getBlockchain());
        tp = new TransactionsPool(blockServices.getBlockChain());
        CLI cli = new CLI(args);
        cli.parse();
        BlockChain.serializeBlockChain(blockServices.getBlockChain());
    }

}
