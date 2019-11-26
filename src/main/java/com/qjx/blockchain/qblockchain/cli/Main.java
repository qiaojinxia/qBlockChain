package com.qjx.blockchain.qblockchain.cli;

import com.qjx.blockchain.qblockchain.basemodel.BlockChain;
import com.qjx.blockchain.qblockchain.blockprocessing.BlockServices;
import com.qjx.blockchain.qblockchain.blockprocessing.TransactionsPool;
import java.util.Scanner;
public class Main {
public static  BlockServices blockServices = null;
    public static  TransactionsPool tp = null;
    public static void main(String[] args) {
        blockServices =new BlockServices(BlockChain.loadBlockChain().getBlockchain());
        tp = new TransactionsPool(blockServices.getBlockChain());
        String ch="";//定义字符串ch
        while(true){
            try{
                System.out.println("Usage:");
                System.out.println("  createwallet -name [Name] - Generates a new key-pair and saves it into the wallet file");
                System.out.println("  printaddresses - print all wallet address");
                System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
                System.out.println("  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
                System.out.println("  printchain - Print all the blocks of the blockchain");
                //send -from zhangsan -to 1whgKUf5XUbzYH6MsbnYkFCiURwV89bm6 -amount 2
                System.out.println("  send -from FROM -to TO -amount AMOUNT of coins from FROM address to TO");
                System.out.println("  p2pserver -listenport port -seedport port");
                Scanner str=new Scanner(System.in);//通过new Scanner(System.in)创建一个Scanner，控制台会一直等待输入，直到敲回车键结束，把所输入的内容传给Scanner
                ch = str.nextLine();//获取输入的内容
                //过去掉首字母的空格
                if(Character.isSpaceChar(ch.charAt(0)))
                    ch = ch.substring(1,ch.length());
                if(Character.isSpaceChar(ch.charAt(ch.length()-1)))
                    ch = ch.substring(0,ch.length()-1);
                String[] chs = ch.split(" ");
                for(int i=0;i<chs.length;i++){
                    chs[i]=chs[i].replace(" ","");
                }
                CLI cli = new CLI(chs);
                cli.parse();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

}
