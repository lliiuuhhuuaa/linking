package com.lh.linking.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecureUtils {
    /**
     * MD5 32为加密
     *
     * @param str 明文
     *            字符集编码
     * @return 32位密文
     */
    public static String md5(String str) {
        byte[] unencodedStr;
        if (str == null) {
            return "";
        }
        unencodedStr = str.getBytes(StandardCharsets.UTF_8);
        return md5Encrypt(unencodedStr);
    }

    /**
     * @param str
     * @return
     * @time 2019年1月22日
     * @desc MD5 32为加密
     */
    public static String md5Encrypt(byte[] str) {
        StringBuffer buf = new StringBuffer();
        try {
            if (str == null) {
                return "";
            }
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.reset();
            md.update(str);
            byte[] encodedPassword = md.digest();

            for (int i = 0; i < encodedPassword.length; i++) {
                if ((encodedPassword[i] & 0xff) < 0x10) {
                    buf.append("0");
                }
                buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("MD5加密出错", e);
        }
        return buf.toString();
    }
}
