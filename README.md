一个简单的区块链demo 能实现 存储 区块链 交易 Merkle树 交易签名 UTXO模型 。

可以

<img src="/Users/qiao/Library/Application Support/typora-user-images/image-20191116170008612.png" alt="image-20191116170008612" style="zoom:50%;" />



第一次写只是实现了 没有用Spring 方式去去管理 bean  等有空了可以重构下。

<img src="/Users/qiao/Library/Application Support/typora-user-images/image-20191116170547538.png" alt="image-20191116170547538" style="zoom:50%;" />

localhost:8080/blockChain 只写了一个借口查区块链的





这个是获取钱包余额接口

```java
/**
 * @param address 钱包地址
 * @return 返回账户的余额
 * @throws Exception
 */
public BigDecimal getBalcnce(byte[] address) throws Exception {
    if (!WalletStruts.isValid(address))
        throw new IllegalArgumentException("wallet address is  invalid");
    //获取账户余额
    UtxoInfo balance = getUnSpendUtxos(address);
    return balance.getBalance();
}
```





挖矿接口

<img src="/Users/qiao/Library/Application Support/typora-user-images/image-20191116170917961.png" alt="image-20191116170917961" style="zoom:50%;" />



新建交易

<img src="/Users/qiao/Library/Application Support/typora-user-images/image-20191116170955246.png" alt="image-20191116170955246" style="zoom:50%;" />

