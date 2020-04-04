package com.apple.sdk.utils;

import java.security.SecureRandom;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lipeishen on 2020/02/20.
 */
public class GenerateRandomNumber {
    /**
     * java生成随机数字和字母组合
     *
     * @param length[生成随机数的长度]
     *
     * @return
     */
    public static String getCharAndNumr(int length) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            // 输出字母还是数字
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            // 字符串
            if ("char".equalsIgnoreCase(charOrNum)) {
                // 取得大写字母还是小写字母
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (choice + random.nextInt(26));
            } else if ("num".equalsIgnoreCase(charOrNum)) { // 数字
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String getRandNum(int num) {
        return StringUtils.leftPad(Integer.toString(RANDOM.nextInt((int) Math
                .round(Math.pow(10, num)))), num, '0');
    }

}
