package org.cloudbus.nativesim.entity;

import lombok.Getter;
import lombok.Setter;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import java.util.Random;
import java.util.List;
import java.util.UUID;
@Getter @Setter
public class NativeCloudlet extends Cloudlet {
    private String uid;
    public int id;
    protected int instanceId;

    public NativeCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw);
        uid = UUID.randomUUID().toString();
    }

    public NativeCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, boolean record, List<String> fileList) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw, record, fileList);
        uid = UUID.randomUUID().toString();
    }

    public NativeCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, List<String> fileList) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw, fileList);
        uid = UUID.randomUUID().toString();
    }

    public NativeCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw, boolean record) {
        super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam, utilizationModelBw, record);
        uid = UUID.randomUUID().toString();
    }

    public long generateCloudletLength() {
        // 可根据实际情况调整
        long meanCloudletLength = 10000; // 平均 Cloudlet 长度
        long stdDev = 2000; // 标准差

        // 创建 Random 实例
        Random random = new Random();

        // 生成正态分布的 Cloudlet 长度
        long cloudletLength = (long) (meanCloudletLength + random.nextGaussian() * stdDev);

        // 输出生成的 Cloudlet 长度
        System.out.println("Generated Cloudlet Length: " + cloudletLength);

        return cloudletLength;
    }

}
