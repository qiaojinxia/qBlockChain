package com.qjx.blockchain.qblockchain.blockprocessing;

import com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils.Md5Coder;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class WalletServices {
    public String getAccount(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return Md5Coder.encode(str);
    }
}

