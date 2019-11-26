### 基于Java实现的比特币系统

<img src="/Users/qiao/Downloads/4465704-bc5265fac9dadfb8.jpeg" alt="4465704-bc5265fac9dadfb8" style="zoom:51%;" />

实现比特币 的 go语言版本的比较多 java 并不多 所以就边研究边想实现一遍用java,但为什么大多数人用go来实现 其一就是比较快吧 用java感觉有些笨重,运行编译速度 也比不上go.

目前实现了

- 数据层√ 
  - 数据区块 √
  - 哈希函数 √
  - 链式结构√
  - Merklet树√
  - 时间戳√
  - 非对称加密√
  - 交易签名√
  - 交易转账utxo模型√
- 网络层
  - p2p网络 √
  - 传播机制√
  - 验证机制√
- 共识层
  - pow √

数据层基本上该有的都有了 

```
//比特币衰减周期
private final static int decaycycle = 10 * 6 * 24 *365 * 4;
//初始奖励数 每 decaycycle块减半
private final static float basewaard = (float) 50.0;
//Q特币初始奖励数
public final static int Qitcoin = 21000000;
```

设定了 每块自动调节时间 为10分钟 刚开始挖矿会比较快 挖着挖着 就接近10分钟了

奖励也是 设定的 21000000万枚 表面上高仿比特币的机制。

网络层 测试了 开2个进程 可以互相通信传输区块 当挖到区块是自动同步 当添加交易时自动同步到交易池

交易池 暂时还是比较 简单的 只是简单的存储维护一个交易list

关于网络层 比特币 使用的是UPNP协议进行Nat穿透 暂时还没添加上 所以只能有公网ip 或者内网之间测试 。

数据存储用的leveldb



使用方式:



```bash

p2pserver -listenport 52111 //运行一个监听一个 服务端  

p2pserver -listenport 52112 -seedport ws://localhost:52111  //监听端
 
首先创建一个钱包 createwallet -name [Name] 账户名

getbalance -address 1986nN76A38CnBSvj9kmkTnVfu4sCiF28B  使用钱包地址获取零钱

send -from zhangsan -to 18vCZaoBvu14v7QdT8L4xVztPTsr7vWMSk -amount 2 //转账 只有在湾矿后才能生效

createblockchain -address zhangsan 创建区块


```

# yblockchain
