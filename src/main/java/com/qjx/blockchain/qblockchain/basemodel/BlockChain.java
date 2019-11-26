package com.qjx.blockchain.qblockchain.basemodel;

import com.alibaba.fastjson.JSON;
import com.qjx.blockchain.qblockchain.blockprocessing.ProofOfWork;
import com.qjx.blockchain.qblockchain.commonutils.FileUtil;


import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
import com.qjx.blockchain.qblockchain.storedb.DbInitConfig;
import com.qjx.blockchain.qblockchain.storedb.DbStore;
import com.qjx.blockchain.qblockchain.storedb.LevelDbStoreImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.*;

/**
 * Created by caomaoboy 2019-11-04
 **/
public class BlockChain {
    public final static Logger logger = LoggerFactory.getLogger(BlockChain.class);
    public final static DbInitConfig a;
    public  static DbStore b = null;
    static {
        a= new DbInitConfig();
        try {
            b = new LevelDbStoreImpl(a.levelDB());
        } catch (IOException e) {
            e.printStackTrace();
        }
        WALLETFILE = FileUtil.getConTextPath().replace("targ","");

    }
    public void setBlockchain(List<Block> blockchain) {
        this.blockchain = blockchain;
    }

    public List<Block> getBlockchain() {
        return blockchain;
    }

    /**
     * 校验HASH的合法性
     *
     * @param hashcode 待检验的hashcode
     * @return
     */
    public static boolean isHashValid(String hashcode,String nBits){
        String a = ProofOfWork.unDecodeBits(nBits.replace("0x","")).replace("0x","");
        if(new BigInteger(hashcode.replace("0x",""),16).compareTo(new BigInteger(a,16))<=-1)
            return true;
        return false;

    }
    private static String repeat(String str, int repeat) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            buf.append(str);
        }
        return buf.toString();
    }
    //保存区块链的地址
    private static  String WALLETFILE;
    //区块链
    private static List<Block> blockchain;
    public BlockChain(List<Block> blockchain){
       this.blockchain = blockchain;
        Integer validNum =validBlockChain();
        List<Block>_buff = new ArrayList<Block>();
        //如果验证钱包的hash不连续 则丢弃不连续部分
        if(!validNum.equals(-1)){
            for(int i =0;i<validNum;i++){
                logger.info("检测到区块链不一致将丢弃后面" + (this.blockchain.size() - validNum -1) +"部分!");
                _buff.add(blockchain.get(i));
                this.blockchain = _buff;

            }
        }
        List<Block>_buff2 = new ArrayList<Block>();
        //检测区块链计算的工作量证明值是否通过验证
        for(int i =1;i<this.blockchain.size();i++){
            if(!isHashValid(this.blockchain.get(i).gethash(),this.blockchain.get(i).getnBits())){
                for(int m =0;i<i;i++){
                    logger.info("检测到区块链工作证明无效!" + (this.blockchain.size() - i -1) +"部分!");
                    _buff2.add(this.blockchain.get(m));
                    this.blockchain = _buff2;

                }
            }
        }

    }
    public BlockChain(){
        this.blockchain = new ArrayList<Block>();

    }


    /**
     * 将区块链以jspon格式保存到本地  blockChain.json中
     * @param blockChain
     */
    public static void serializeBlockChain(List<Block> blockChain,String file){
//        public LinkedHashMap<String, String> iteratorDb() {
//            DBIterator iterator = db.iterator();
//            LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
//            while (iterator.hasNext()) {
//                Map.Entry<byte[], byte[]> next = iterator.next();
//                String key = Iq80DBFactory.asString(next.getKey());
//                String value = Iq80DBFactory.asString(next.getValue());
//                linkedHashMap.put(key, value);
//            }
//            return linkedHashMap;
//        }


        for(int i=0;i<blockChain.size();i++){
            String content = JSON.toJSONString(blockChain.get(i));
            b.put(blockChain.get(i).gethash(),content);
        }
        b.put("lastblockHash",blockChain.get(blockChain.size()-1).gethash());
//        String content = JSON.toJSONString(blockChain);
//        byte[] byteContent = compress(content.getBytes());
//        try{
//            writeFile(WALLETFILE,file,byteContent);
//        }catch (IOException e){
//            throw  new IllegalArgumentException("写入文件失败");
//        }
//        logger.info("Blockchain saved successfully!");
    }
    public static void serializeBlockChain(List<Block> blockChain){
        serializeBlockChain(blockChain,"blockchain.data");
    }


    /**
     * 用于校验区块链hash是否正确
     */
    public Integer validBlockChain(){
        for(int i = 1;i<blockchain.size();i++){
            //校验区块链数据是否正确 前一块的hash 和后一块 里记录的hash对应
            if(!blockchain.get(i-1).gethash().equals(blockchain.get(i).getPreviousHash())){
                //返回索引表示当前索引区块链出现问题
                return i;
            }
        }
        //-1 表示通过检验
        return -1;
    }

    /**
     * 将本地  blockChain.json 读取成对象
     * @return
     */
    public static BlockChain loadBlockChain(String file)  {
        String lastHash =b.get("lastblockHash");
        if(!ObjectUtils.notEmpty(lastHash)){
            return new BlockChain();
        }
        List<Block> blockchain = new ArrayList<Block>();
        Block block =null;
        int statt =-1;

        while(statt ==-1 || !block.isgenesisBlock()){
            statt=0;
            try{
                block = JSON.parseObject(b.get(lastHash),Block.class);
                lastHash = block.getPreviousHash();
                blockchain.add(block);
            }catch (Exception e){
                logger.error("序列化数据库失败~");
            }
        }
        Collections.sort(blockchain, new Comparator<Block>() {
            @Override
            public int compare(Block b1, Block b2) {
                //从小到大排序
                return b1.getIndex().compareTo(b2.getIndex());
            }
        });
        return new BlockChain(blockchain);
//        String contet ="";
//        if(FileUtil.isFileExist(WALLETFILE +file))
//            try{
//                byte[] filebyte = readtToByte(WALLETFILE +file);
//                if(filebyte.length!=0) {
//                    filebyte = uncompress(filebyte);
//                    contet = new String(filebyte);
//                }
//            }catch (IOException | DataFormatException e){
//               throw  new IllegalArgumentException("读取区块链数据失败!");
//            }

    }
    /**
     * 将本地  blockChain.json 读取成对象
     * @return
     */
    public static BlockChain loadBlockChain(){

        //默认保存blockchain目录
        return loadBlockChain("blockchain.data");
    }


    public static byte[] compress(byte input[]) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Deflater compressor = new Deflater(1);
        try {
            compressor.setInput(input);
            compressor.finish();
            final byte[] buf = new byte[2048];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
        } finally {
            compressor.end();
        }
        return bos.toByteArray();
    }

    public static byte[] uncompress(byte[] input) throws DataFormatException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Inflater decompressor = new Inflater();
        try {
            decompressor.setInput(input);
            final byte[] buf = new byte[2048];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
        } finally {
            decompressor.end();
        }
        return bos.toByteArray();
    }

    /**
     * 将byte数组写入文件
     *
     * @param file
     * @param content
     * @throws IOException
     */
    public static void writeFile(String file,String filename, byte[] content)
            throws IOException {
        try {
            File f = new File(file);
            if (!f.exists()) {
                f.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file + filename);
            fos.write(content);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * the traditional io way
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] readtToByte(String filename) throws IOException {

        File f = new File(filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bos.close();
        }
    }


}
