package com.qjx.blockchain.qblockchain.basemodel;


import com.qjx.blockchain.qblockchain.commonutils.ObjectUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;


/**
 * Created by caomaoboy 2019-11-02
 **/
public class UtxoInfo {
    public Map<String, UtfoStruts> getUtxoInfo() {
        return utxoInfo;
    }

    public void setUtxoInfo(Map<String, UtfoStruts> utxoInfo) {
        this.utxoInfo = utxoInfo;
    }

    private Map<String,UtfoStruts> utxoInfo;


    public UtxoInfo(){
        super();
    }
    public Integer getSize(){
        return utxoInfo.size();
    }
    public UtxoInfo(Map<String,UtxoInfo> uxtoinfo){
        this.utxoInfo = utxoInfo;
    }
    public void add(UtfoStruts us){
        if(StringUtils.isEmpty(us))
            throw new IllegalArgumentException("null error!");
        if(StringUtils.isEmpty(utxoInfo))
            utxoInfo =new HashMap<String,UtfoStruts>();
        //如果要添加的key 已经存在 则调用添加
        if(isContains(new String(us.getTxid()))){
            for(int i = 0; i< us.getSize();i++)
                utxoInfo.get(new String(us.getTxid())).add(us.getVoi()[i],us.getVout()[i]);
        }else{
            utxoInfo.put(new String(us.getTxid()),us);
        }


    }

    /**
     * 判断是否包含 key
     * @param key
     * @return
     */
    public boolean isContains(String key){
        Iterator<Map.Entry<String, UtfoStruts>> utxoIter = utxoInfo.entrySet().iterator();
        while(utxoIter.hasNext()){
            Map.Entry<String, UtfoStruts> utxoNext = utxoIter.next();
            if(utxoNext.getKey().equals(key))
                return true;

        }
        return false;
    }

    public UtfoStruts findMaxValue(){
        if(StringUtils.isEmpty(utxoInfo)){
            throw new IllegalArgumentException("null error!");
        }
        BigDecimal count = new BigDecimal("0");
        Iterator<Map.Entry<String, UtfoStruts>> uslist = utxoInfo.entrySet().iterator();
        UtfoStruts usRes =null;
        while(uslist.hasNext()){
            Map.Entry<String, UtfoStruts> usNext = uslist.next();
            BigDecimal val = usNext.getValue().findMaxValue().getValue();
                if(val.compareTo(count) ==1){
                    count = new BigDecimal(val.toString());
                    usRes = usNext.getValue();
                }

            }
            return usRes;
        }


    /**
     * 获得 所有交易输出里面最小的一笔
     * @return
     */
    public UtfoStruts findMinValue(){
        if(StringUtils.isEmpty(utxoInfo)){
            throw new IllegalArgumentException("null error!");
        }
        Iterator<Map.Entry<String, UtfoStruts>> uslist = utxoInfo.entrySet().iterator();
        UtfoStruts usRes =null;
        //获取第一笔当做最小
        BigDecimal count = new BigDecimal(utxoInfo.entrySet().iterator().next().getValue().getVout()[0].getValue().toString());
        while(uslist.hasNext()){
            Map.Entry<String, UtfoStruts> usNext = uslist.next();
            BigDecimal val = usNext.getValue().findMaxValue().getValue();
            if(val.compareTo(count) == -1){
                count = new BigDecimal(val.toString());
                usRes = usNext.getValue();
            }
        }
        return usRes;
    }

    /**
     * 随机获取零钱  未来可以添加更多找零钱的算法
     * @param amount
     * @return
     */
    public UtxoInfo getsmallExchange(BigDecimal amount){
        if(StringUtils.isEmpty(utxoInfo)){
            throw new IllegalArgumentException("null error!");
        }
        Iterator<Map.Entry<String, UtfoStruts>> uslist = utxoInfo.entrySet().iterator();
        //用来 和 amount 作比较
        BigDecimal count = new BigDecimal("0");
        UtxoInfo exChange =  new UtxoInfo();
        while(uslist.hasNext()){
            Map.Entry<String, UtfoStruts> usEntry = uslist.next();
            if(count.compareTo(amount) == - 1 ){
                //如果零钱 还少的话 就继续 添加零钱
                count = count.add(usEntry.getValue().getTotalAmount());
                exChange.add(usEntry.getValue());
            }else{
                break;
            }

        }
        return exChange;

    }

    /**
     * 将utxoout的 交易输入 转换成 交易输出
     * @param utxo
     * @return
     */
    public static List<TXInput> tansToTxInPut(byte[] publicKey, UtxoInfo utxo){
        List<TXInput> txins = new ArrayList<TXInput>();
        Iterator<Map.Entry<String, UtfoStruts>> uslist = utxo.getUtxoInfo().entrySet().iterator();
        while(uslist.hasNext()){
            Map.Entry<String, UtfoStruts> usIter = uslist.next();
            //一个 交易id 可能有多笔订单指向 同一个锁定脚本 所以需要遍历
            for(int i=0;i<usIter.getValue().getSize();i++){
                //创建交易输入
                TXInput newTxIn =new TXInput(usIter.getKey().getBytes(),usIter.getValue().getVoi()[i],publicKey);
                txins.add(newTxIn);
            }

        }
        return txins;
    }

    public BigDecimal getBalance(){
        if(!ObjectUtils.notEmpty(this.utxoInfo))
            throw new IllegalArgumentException("Failed to reach any valid utxo!");
        Iterator<Map.Entry<String, UtfoStruts>> uxoIter = this.utxoInfo.entrySet().iterator();
        BigDecimal allAmount = new BigDecimal("0");
        while(uxoIter.hasNext()){
            //计算自己的utxo的所有金钱
            allAmount =  allAmount.add(uxoIter.next().getValue().getTotalAmount());
        }
        return allAmount;

    }


    /**
     * 将utxo交易输出内部的金额 按照特殊格式输出 主要是便于 排序算零钱
     */
    private Map<String, BigDecimal> findBalance(){
        if(StringUtils.isEmpty(this.utxoInfo))
            throw new IllegalArgumentException("Could not find any valid utxo!");
        Map<String, BigDecimal> val = new HashMap<String, BigDecimal>();
        Iterator<Map.Entry<String, UtfoStruts>> utfoIter = this.utxoInfo.entrySet().iterator();
        while(utfoIter.hasNext()){
            Map.Entry<String, UtfoStruts> utfoNext = utfoIter.next();
            for(int i=0;i<utfoNext.getValue().getSize();i++){
                val.put(utfoNext.getKey()+","+utfoNext.getValue().getVoi()[i],utfoNext.getValue().getVout()[i].getValue());
            }
        }
        Map<String, BigDecimal> sortedMap = new LinkedHashMap<String, BigDecimal>();
        List<Map.Entry<String, BigDecimal>> entryList = new ArrayList<Map.Entry<String, BigDecimal>>(
                val.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, BigDecimal>>() {
            @Override
            public int compare(Map.Entry<String, BigDecimal> o1, Map.Entry<String, BigDecimal> o2) {
                //按照从小到大返回
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Iterator<Map.Entry<String, BigDecimal>> iter = entryList.iterator();
        Map.Entry<String, BigDecimal> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;

    }

    /**
     *  从小到大排序零钱 把最小的加在一起 返回 TODO 也可以返回面值最大的
     * @param amount
     * @return
     * @throws Exception
     */
    public Map<String,BigDecimal> getMinExchange( BigDecimal amount) throws Exception {
        //返回所有零钱
        Map<String, BigDecimal> sortedMap = findBalance();
        BigDecimal recoder = new BigDecimal("0");
        Map<String, BigDecimal> copyMap = new HashMap<String, BigDecimal>();
        for (Map.Entry<String, BigDecimal> entry : sortedMap.entrySet()) {
            //如果拼起来的零钱小于 amount 则再增加零钱
            if(recoder.compareTo(amount) == -1 ){
                //钱不够就加钱
                recoder = recoder.add(new BigDecimal(entry.getValue().toString()));
                copyMap.put(entry.getKey(),entry.getValue());
                continue;
            }
        }
        return copyMap;
    }

    /**
     *  将map形式转换成UtxoInfo
     * @param mapChange
     * @return
     */
    public static UtxoInfo  formatMap(Map<String,BigDecimal> mapChange){

        if(StringUtils.isEmpty(mapChange))
            throw new IllegalArgumentException("null error!");
        UtxoInfo utoxs = new UtxoInfo();
        UtfoStruts ufo = null;
        Iterator<Map.Entry<String, BigDecimal>> mapexIter = mapChange.entrySet().iterator();
        while(mapexIter.hasNext()){
            Map.Entry<String, BigDecimal> mapNext =  mapexIter.next();
           //key 是包含 txid,vio的 文本
            String txid = mapNext.getKey().split(",")[0];
            //索引
            Integer val = new Integer(mapNext.getKey().split(",")[1]);
            ufo = new UtfoStruts((txid));
            TXOutput txOut = TXOutput.newTxOutPut(mapNext.getValue());
            ufo.add(val,txOut);
            utoxs.add(ufo);

        }
        return utoxs;

    }

    /**
     * 获取结构体李所有金钱总和
     * @return
     */
    public BigDecimal getTotalAmount(){
        if(StringUtils.isEmpty(this.utxoInfo))
            throw  new IllegalArgumentException("null error!");
        BigDecimal total = new BigDecimal("0");
        Iterator<Map.Entry<String, UtfoStruts>> utxoIter = utxoInfo.entrySet().iterator();
        while(utxoIter.hasNext()){
            Map.Entry<String, UtfoStruts> utxoNext = utxoIter.next();
            total = total.add(utxoNext.getValue().getTotalAmount());

        }
        return total;

    }

}



