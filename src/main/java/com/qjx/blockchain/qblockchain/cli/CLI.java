package com.qjx.blockchain.qblockchain.cli;

import com.qjx.blockchain.qblockchain.basemodel.*;
import com.qjx.blockchain.qblockchain.blockprocessing.BlockServices;
import com.qjx.blockchain.qblockchain.blockprocessing.WalletServices;
import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.HexUtil;
import com.qjx.blockchain.qblockchain.p2pnetwork.BlockController;
import com.qjx.blockchain.qblockchain.p2pnetwork.Inital;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import sun.misc.BASE64Decoder;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 * Created by caomaoboy 2019-11-19
 **/
public class CLI {
    private String[] args;
    private Options options = new Options();
    public CLI(String[] args) {
        this.args = args;

        Option helpCmd = Option.builder("h").desc("show help").build();
        options.addOption(helpCmd);
        Option address = Option.builder("address").hasArg(true).desc("Source wallet address").build();
        Option name = Option.builder("name").hasArg(true).desc("name wallet address").build();
        Option sendFrom = Option.builder("from").hasArg(true).desc("Source wallet address").build();
        Option sendTo = Option.builder("to").hasArg(true).desc("Destination wallet address").build();
        Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();
        options.addOption(address);
        options.addOption(name);
        options.addOption(sendFrom);
        options.addOption(sendTo);
        options.addOption(sendAmount);
    }

    /**
     * 验证入参
     *
     * @param args
     */
    private void validateArgs(String[] args) {
        if (args == null || args.length < 1) {
            help();
        }
    }


    /**
     * 命令行解析入口
     */
    public void parse() {
        this.validateArgs(args);
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            switch (args[0]) {
                case "createblockchain":
                    String createblockchainAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(createblockchainAddress)) {
                        help();
                    }
                    this.createBlockchain(createblockchainAddress);
                    break;
                case "getbalance":
                    String getBalanceAddress = cmd.getOptionValue("address");
                    if (StringUtils.isBlank(getBalanceAddress)) {
                        help();
                    }
                    this.getBalance(getBalanceAddress);
                    break;
                case "send":
                    String sendFrom = cmd.getOptionValue("from");
                    String sendTo = cmd.getOptionValue("to");
                    String sendAmount = cmd.getOptionValue("amount");
                    if (StringUtils.isBlank(sendFrom) ||
                            StringUtils.isBlank(sendTo) ||
                            !NumberUtils.isDigits(sendAmount)) {
                        help();
                    }
                    this.send(sendFrom, sendTo, BigDecimal.valueOf(Long.parseLong(sendAmount)));
                    break;
                case "createwallet":
                    String name = cmd.getOptionValue("name");
                    this.createWallet(name);
                    break;
                case "printaddresses":
                    this.printAddresses();
                    break;
                case "printchain":
                    this.printChain();
                    break;
                case "h":
                    this.help();
                    break;
                default:
                    this.help();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getBalance(String walletname)  {
        Wallet wallet = Wallet.loadMyWallet();
        String balance = null;
        try {
            balance = Main.blockServices.getBalcnce(wallet.getByWalletName(walletname).getAddress()).toString();
        } catch (Exception e) {
            System.out.println("Failed to reach any valid utxo!");
            e.printStackTrace();
            balance ="0.00";
        }
        System.out.println("\033[31;4m" + "当前余额:"+balance + "\033[0m");

    }

    private void send(String sendFrom, String sendTo, BigDecimal valueOf) {
        Wallet wallet = Wallet.loadMyWallet();
        System.out.println(sendFrom+"  "+sendTo);
        if(null ==wallet.getByWalletName(sendFrom)){
            System.out.println("\033[31;4m" + "当前钱包地址未找到,请先创建!" + "\033[0m");
            return;
        }
        try {
            Transaction cc =Main.blockServices.newTransaction(wallet.getByWalletName(sendFrom).getPublicKey(),sendTo.getBytes(),valueOf);
            Transaction.SignTransaction(cc,new String(wallet.getByWalletName(sendFrom).getPrivateKey()),Main.blockServices.getBlockChain());
            Main.tp.addTransPlool(cc);
        } catch (Exception e) {
            System.out.println("\033[31;4m" + "转账失败,没有可用余额!" + "\033[0m");
            e.printStackTrace();
            return;
        }
        System.out.println("转账已加入交易池 会在下一次区块产生时完成!"+ valueOf.toString());
    }

    private void printAddresses() {

    }

    /**
     * 开启p2p客户端
     * @param listenPort
     * @param conPort
     */
    private void p2pwWork(String listenPort,String conPort)  {
        Inital inital = new Inital(listenPort,conPort);
        Thread p2pThread = new Thread(inital);
        p2pThread.start();
    }

    private void createWallet(String name) throws Exception {
        Wallet wallet = Wallet.loadMyWallet();
        if(null !=wallet.getByWalletName(name))
        System.out.println("\033[35;4m" + "当前钱包名已存在!" + "\033[0m");
        else{
            WalletStruts wallets = wallet.newAccount(name);
            Wallet.saveMyWallet(wallet);
        }
        System.out.println("\033[35;4m" + "钱包名:" +name+ "\033[0m");
        System.out.println("\033[35;4m" + "钱包地址:" + new String(wallet.getByWalletName(name).getAddress())+ "\033[0m");

    }
    private void createBlockchain(String createblockchainAddress) {
        Wallet wallet = Wallet.loadMyWallet();
        if(null == wallet.getByWalletName(createblockchainAddress))
            throw new IllegalArgumentException("钱包地址不存,请先去创建钱包~");
        try {
            List<Transaction> trans =Main.tp.getallTrans();
            Block packageblock = Main.blockServices.PackageBlock(trans);
            Block gen= Main.blockServices.Mineral(wallet.getByWalletName(createblockchainAddress).getAddress(),"system award",
                    packageblock);
            Main.blockServices.addBlock(gen);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("挖矿成功!");
    }

    /**
     * 打印帮助信息
     */
    private void help() {
        System.out.println("Usage:");
        System.out.println("  createwallet -name [Name] - Generates a new key-pair and saves it into the wallet file");
        System.out.println("  printaddresses - print all wallet address");
        System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
        System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
        System.out.println("  printchain - Print all the blocks of the blockchain");
        System.out.println("  send -from FROM -to TO -amount AMOUNT - Send AMOUNT of coins from FROM address to TO");
        System.exit(0);
    }
    /**
     * 打印出区块链中的所有区块
     */
    private void printChain() throws Exception {
        BlockChain blockchain = BlockChain.loadBlockChain();
        for (Iterator<Block> iterator = blockchain.getBlockchain().iterator(); iterator.hasNext(); ) {
            Block block = iterator.next();
            if (block != null) {
                boolean validate = Block.isProofValid(block);
                System.out.println("================================================================================");
                System.out.println("\033[32;4m"+"   ** --------------------区块索引号:" + block.getIndex()+ ", 区块有效性:" + validate +" --------------------- **"+"\033[0m");
                System.out.println(" **" + " 区块hash:"+ block.gethash()+ "   **");
                System.out.println("**" + " 前一区块hash:"+block.getPreviousHash()+ " **");
                System.err.println("  **" + "                         区块包含交易数:"+ block.getData().size()+ "区块工作量证明:" + block.getNonce()+"                  **");
            }
        }
    }
}
