package com.qjx.blockchain.qblockchain.blockprocessing;

import com.qjx.blockchain.qblockchain.basemodel.*;
import org.omg.CORBA.INTERNAL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

public class ProofOfWork {
    //比特币衰减周期
    private final static int decaycycle = 10 * 6 * 24 *365 * 4;
    //初始奖励数 每 decaycycle块减半
        private final static float basewaard = (float) 50.0;
        //Q特币初始奖励数
        public final static int Qitcoin = 21000000;
        //初始挖矿难度
        public final static String nInitbits ="0x200ffff";
        public final static Integer nTargetTimespan = 10 * 60 * 1; //过去两周时间
        public final static  Integer nTargetSpacing = 10 * 60 ; //期望挖矿时间
        public final static Integer nInterval = nTargetTimespan / nTargetSpacing; //过去2周期望达到的区块数 2016个
        public final static String nInitHard =new String("0x00ffff0000000000000000000000000000000000000000000000000000000000");

        /**
         * 获取交易奖励数
         * @param blocksize
         * @return
         */
        public static float getAward(int blocksize){
            //区块链个数 除衰减周期
            int cycle = blocksize / decaycycle;
            //余数除以 乘以 2
            int factor = (int) Math.pow(2,cycle);
            //再去除以基础奖励
            return basewaard / factor;

        }
        /**
         * 校验HASH的合法性
         *
         * @param hashcode 待检验的hashcode
         * @return
         */
        public static boolean isHashValid(String hashcode,String calc){
            calc = ProofOfWork.unDecodeBits(calc).replace("0x","");
            BigInteger hash = new BigInteger(hashcode,16);
            BigInteger calcs = new BigInteger(calc,16);
            if(hash.compareTo(calcs) < 1)
                return true;
            return  false;

        }
        //public static long hard =0X00000000FFFF0000000000000000000000000000000000000000000000000000;
        private static String repeat(String str, int repeat) {
            final StringBuilder buf = new StringBuilder();
            for (int i = 0; i < repeat; i++) {
                buf.append(str);
            }
            return buf.toString();
        }

        /**
         * 计算挖矿难度按照当前
         */

        private final static Integer Counts =  2100;//币的总数
        private final static Integer unit = 10000;//单位

        private static  double reward =50.0; //最初个数
        private final Integer mintime = 10;//每隔10 分钟 单位分钟
        private final Double decayFrac =  0.5; //衰减比例
        private final Double decayYears =  4.0; //衰减年数

        private Double Calcdecay() {
            Double cc = 1.0;
            Double costs = 0.0;
            while (reward > 1e-7) {
                costs += (60.0 / mintime * 24 * 365   * reward  * decayYears );
                System.out.println(costs);
                reward = reward * 0.5 ;
            }
            System.out.println(costs);
            return costs;

        }
        /**
         * 返回特殊编码后的难度值
         * @return
         */
        public static String unDecodeBits(String nbits){
            String ex ="";
            String co ="";
            if(nbits.startsWith("0x")) {
                ex = nbits.substring(2, 4);
                co = nbits.substring(4, nbits.length());
            }
            else {
                ex = nbits.substring(0, 2);
                co = nbits.substring(2, nbits.length());
            }

            BigInteger exponent = new BigInteger(ex,16);
            BigInteger coefficient = new BigInteger(co,16);
            Double xx = 8 * (exponent.doubleValue() -0x03); //8 * (exponent – 3)
            Double ax = Math.pow(2,xx);// coefficient * 2^(8 * (exponent – 3))
            BigDecimal res = new BigDecimal(coefficient).multiply(new BigDecimal(ax));
            String toHex = String.format("%#x.",res.toBigInteger()).replace(".","");
            return autoformat(toHex);
        }
        public static String decodeBits(String nbits){
            if(nbits.toLowerCase().startsWith("0x"))
                nbits = nbits.substring(2,nbits.length());
            int end= 0;
            int begin = -1;
            for(int i=0;i<nbits.length()-1;i++){
                char ch =nbits.charAt(i);
                if(ch !='0' && begin == -1){
                    begin= i;//开始标记
                }
                //如果 当前字符是 0且 前几次都是零
                if(ch =='0' && begin ==0)
                    continue;
                if(ch !='0')
                    //记录最后一个非0 char的index
                    end = i;
            }
            if(end - begin > 5 ){//这里是判断 如果 像0x1acdfabc 这样的在进位转换的时候会出现小数 因为定义了3个字节为有效位
                //所以需要 舍去多余的 0x1acdfa00000后面填充成0
                nbits = autoformat(nbits).replace("0x","");
                end = begin + 5;
            }
            String co = "0" + nbits.substring(begin,end+1);
            BigDecimal traget =new BigDecimal(new BigInteger(nbits,16));
            BigDecimal coefficient =new BigDecimal(new BigInteger(co,16));
            BigInteger xx = traget.divide(coefficient,0).toBigInteger();
            Double mm =Math.log(xx.doubleValue())/Math.log(2) / 0x08  + 0x03 ;//这里定义了有效位 0x03相当于3个字节 0xffffff 当然你可以换成四个字节
            String res = "0x" + Integer.toHexString(mm.intValue()) + co;
            return res;
        }

        /**
         * 用来将字符串转换成 指定格式 比如 传过来 12345**** 是62位的字符串 则补足前缀 0x0012345****
         * 还有一个作用 如果计算后的难度 出现 0x00028ccccbd8000000 难度有效位 28ccccbd8 超出6位的时候自动
         * 截取成 0x00028ccc000000000.... 后面填充0补足成64位 为什么是6位呢 使用为 上面↑那条公式定义了
         * @param toHex
         * @return
         */
        public static String autoformat(String toHex){
            StringBuilder zeroHeader = new StringBuilder("0x");
            /**
             * 下面代码自动 补足长度为64位 如 0x00028ccccbd80000000000000000000000000000000000000000000000000000
             */
            toHex= toHex.replace("0x","");
            for(int i =0;i< 64 - toHex.length() ;i++){
                zeroHeader.append("0");
            }
            //截取 6位  Math.log(xx.doubleValue())/Math.log(2) / 0x08  + 0x03 ; 因为公式里设定了6位
            int begin=-1;
            int end = 0;
            for(int i=0;i<toHex.length()-1;i++){
                char ch =toHex.charAt(i);
                if(ch !='0' && begin == -1){
                    begin= i;//开始标记
                }
                //如果 当前字符是 0且 前几次都是零
                if(ch =='0' && begin ==0)
                    continue;
                if(ch !='0')
                    //记录最后一个非0 char的index
                    end = i;
            }
            String hardBit =null;
            StringBuilder zeroTail =new StringBuilder();
            if (end - begin > 5){
                hardBit = toHex.substring(begin,begin+6);
                for(int mx=0;mx<(toHex.length() -hardBit.length());mx++){
                    zeroTail.append("0");
                }
            }
            else{
                hardBit = toHex.substring(begin,end +1);
                zeroTail = zeroTail.append(toHex.substring(end + 1,toHex.length()));
            }
            zeroHeader.append(hardBit).append(zeroTail.toString());
            if(zeroHeader.length() != 66)
                throw  new IllegalArgumentException("auto format error!");
            return zeroHeader.toString();

        }

        public static String GetNextWorkRequired(List<Block> blockChain){
            //区块链高度
            Integer height = blockChain.size();
            //至少2个区块 算间隔一个区块的时间差 创世区块 不算在内
            if(height <3){
                return ProofOfWork.decodeBits(ProofOfWork.nInitHard);
            }
            //如果不满足 2016区块的倍数 就返回过去的难度
            System.out.println(nInterval.toString());
            System.out.println(blockChain.get(blockChain.size() - 1).getIndex());
            System.out.println(blockChain.get(blockChain.size() - 1).getIndex().remainder(new BigInteger(nInterval.toString())).intValue()==0);
            if(blockChain.get(height - 1).getIndex().add(new BigInteger("1")) .remainder(new BigInteger(nInterval.toString())).intValue()!=0)
                //如果不满足一定数量的区块就 给他最后一个区块的难度
                return blockChain.get(height-1).getnBits();
            //前 nInterval 个 的 难度
            String preNbit = unDecodeBits(blockChain.get(height - nInterval -1 ).getnBits());
            //计算前2016个区块所用时间 秒
            System.out.println( blockChain.get(height - nInterval -1 ).getTimestamp());
            System.out.println(blockChain.get(height-1).getTimestamp());
            float nActualTimespan =  blockChain.get(height-1).getTimestamp()/1000 - blockChain.get(height - nInterval -1 ).getTimestamp()/1000 ;
            /**
             * 以下是控制调整难度的速率 不让比特币的难度产生大幅的波动
             */
            if(nActualTimespan < nTargetTimespan/4)
                nActualTimespan = nTargetTimespan/4;
            if(nActualTimespan > nTargetTimespan*4)
                nActualTimespan = nTargetTimespan*4;
            float scale = (float) ((nActualTimespan/nTargetTimespan));
            System.out.println("缩放比例:"+scale+"如果当前 的算力越大 花费时间越小 和 预期时间不变并成反比 缩放比例也就越小 ");
            System.out.println(preNbit.substring(2,preNbit.length()));
            BigInteger  toTen = new BigInteger(preNbit.replace("0x",""),16);
            BigDecimal NEWb = new BigDecimal(toTen.toString()) .multiply(new BigDecimal(scale)).setScale(0,BigDecimal.ROUND_HALF_DOWN);
            String toHex = autoformat(String.format("%#x.",NEWb.toBigInteger()).replace(".",""));
            //如果难度值 太小了 就设为 初始难度值
            if(new BigInteger(toHex.replace("0x",""),16).compareTo(new BigInteger(nInitHard.replace("0x",""),16)) ==1)
                return nInitbits;
            System.out.println("升级后的比特币挖矿难度" +toHex + "\n" +"长度"+toHex.length());
            return ProofOfWork.decodeBits(toHex);
        }
        public static void main(String[] args) throws Exception {

        System.out.println( ProofOfWork.getAward(20));
        String a = unDecodeBits(decodeBits(nInitHard));
        boolean m= a .equals(nInitHard);
        System.out.println(m);
        //System.out.println((unDecodeBits(decodeBits(nInitHard)).equals(nInitHard)));
        BlockServices blockServices =new BlockServices(BlockChain.loadBlockChain().getBlockchain());
        TransactionsPool tp = new TransactionsPool(blockServices.getBlockChain());
        //GetNextWorkRequired(blockServices.getBlockChain());
        Wallet wallet = Wallet.loadMyWallet();
        wallet.newAccount("zhangsan");
        if(null == wallet.getByWalletName("zhangsan"))
            throw new IllegalArgumentException("钱包地址不存,请先去创建钱包~");
        try {
            List<Transaction> trans = tp.getallTrans();
            Block packageblock = blockServices.PackageBlock(trans);
            Block gen= blockServices.Mineral(wallet.getByWalletName("zhangsan").getAddress(),"system award",
                    packageblock);
            blockServices.addBlock(gen);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println(blockServices.getBlockChainjson());
        blockServices.getBalcnce(wallet.getByWalletName("zhangsan").getAddress());
        Wallet.saveMyWallet(wallet);
//        System.out.println(m.toString());
//        System.out.println(String.valueOf(scale));
//        System.out.println((  Math.pow(16,32)/(Math.pow(16,31)) /R));
//        System.out.println( (( S * ( (Math.pow(16,32)/(Math.pow(16,30))) /S) )));
//        System.out.println(((Math.pow(16,32)/(Math.pow(16,30)) *  Math.pow(16,32))/R/1));
        //System.out.println(mathUtils.chufa(num2,String.valueOf(scale)));
    }
}
