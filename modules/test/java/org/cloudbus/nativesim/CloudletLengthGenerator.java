package org.cloudbus.nativesim;/*
 * Copyright ©2024. Jingfeng Wu.
 */

import java.util.Random;

public class CloudletLengthGenerator {
    public static void main(String[] args) {
        long meanCloudletLength = 10000; // 可根据实际情况调整

        // 标准差
        long stdDev = 2000; // 可根据实际情况调整

        // 创建 Random 实例
        Random random = new Random();

        // 生成正态分布的 Cloudlet 长度
        long cloudletLength = (long) (meanCloudletLength + random.nextGaussian() * stdDev);

        // 输出生成的 Cloudlet 长度
        System.out.println("Generated Cloudlet Length: " + cloudletLength);

    }
}
