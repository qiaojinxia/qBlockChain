package com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;

/**
 * Created by caomaoboy 2019-10-29
 **/
public class WalletUtils {

    // RIPEMD160 ( SHA256 (publicKey) )
    public static String generateAddress(byte [] publicKey) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte [] pubKeySha256 = digest.digest(publicKey);
        //Log.i(TAG, "==> sha256: 0x" + HexUtils.encodeHexString(pubKeySha256));
        Security.addProvider(new BouncyCastleProvider());
        digest = MessageDigest.getInstance("RIPEMD160");
        byte [] bytesAddr = digest.digest(pubKeySha256);
        //Log.i(TAG, "==> sha256: 0x" + HexUtils.encodeHexString(bytesAddr));

        return HexUtil.encode(bytesAddr);
    }

    public static String generateAddress(String publicKeyHex) throws  NoSuchAlgorithmException {
        return generateAddress(HexUtil.decode(publicKeyHex));
    }


    public static byte[] generateBase58CheckSum(byte[] data) throws NoSuchAlgorithmException {
        //加载sha256加密器
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        //对数据进行一次sha256
        byte [] dataOneHash = digest.digest(data);
        //对数据进行第二次sha256
        byte [] dataDoubleHash = digest.digest(dataOneHash);
        //取前四个字节
        byte [] checkSum = Arrays.copyOf(dataDoubleHash, 4);
        //Log.i(TAG, "==> base58check sum: " + HexUtils.encodeHexString(checkSum));
        return checkSum;
    }

    // Base58Check(RIPEMD160(SHA256(publicKey))
    public static String generateAddressWithBase58Check(byte [] publicKey) throws NoSuchAlgorithmException {
        String addrHex = generateAddress(publicKey);
        byte [] addrBytes = HexUtil.decode(addrHex);
        byte [] checksum = generateBase58CheckSum(addrBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0);  // address version prefix
        baos.write(addrBytes, 0, addrBytes.length);
        baos.write(checksum, 0, checksum.length);
        String addressWithBase58Check = Base58.encode(baos.toByteArray());
        //Log.i(TAG, "==> address with base58check format: " + addressWithBase58Check);
        return addressWithBase58Check;
    }

}
