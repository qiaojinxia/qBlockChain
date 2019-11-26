package com.qjx.blockchain.qblockchain.commonutils.CryptoSecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Coder {

    private static final Logger logger = LoggerFactory.getLogger(Md5Coder.class);

    public static String encode(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String md5Result = null;
        if(null == data){
            return md5Result;
        }

        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes("UTF-8"));
            byte b[] = md.digest();
            int i;
            StringBuffer sb = new StringBuffer("");
            for(int offset = 0; offset < b.length; offset ++){
                i = b[offset];
                if(i< 0){
                    i += 256;
                }
                if(i < 16){
                    sb.append("0");
                }
                sb.append(Integer.toHexString(i));
            }
            md5Result = sb.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return md5Result;
    }

}

