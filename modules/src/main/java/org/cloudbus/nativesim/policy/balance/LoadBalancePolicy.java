///*
// * Copyright ©2024. Jingfeng Wu.
// */
//
//package org.cloudbus.nativesim.policy.balance;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.cloudbus.nativesim.extend.NativeCloudlet;
//import org.cloudbus.nativesim.policy.migration.InstanceMigrationPolicy;
//import org.cloudbus.nativesim.policy.scaling.ServiceScalingPolicy;
//import org.cloudbus.nativesim.service.Instance;
//import org.cloudbus.nativesim.service.Service;
//import org.cloudbus.nativesim.service.ServiceGraph;
//
//import java.util.List;
//
//@Getter
//@Setter
//public abstract class LoadBalancePolicy {
//
//    private List<Instance> instanceList;
//
//    private ServiceScalingPolicy scalingPolicy;
//
//    private InstanceMigrationPolicy migrationPolicy;
//
//    // 将cloudlets分布到合适的instance中
//    public abstract boolean distributeCloudlets(List<NativeCloudlet> cloudlets);
//
//}
