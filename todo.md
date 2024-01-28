## 待办清单

* [X]  Vm层及以下扩展了许多原有方法来支持上层功能，都以“Native”为前缀，可以向下兼容cloudsim。
* [X]  简化容器类
* [X]  代码开源和版本控制
* [X]  重构Provisioners
* [X]  由于Pe只有Vm相关的方法，把Pe全换成NativePe
* [X]  重构NativeVm、NativeDatacenter、NativeDatacenterBroker
* [X]  重构NativeCloudlet，它的提交应该在进入执行队列以后
* [X]  关掉Vm中的cloudletScheduler，重构NativeCloudletScheduler
* [x]  PodServiceAllocationSimple、PodXXXProvisionerSimple
* [X]  构建Request、Dispatcher、EndPoint、LoadBalancer
* [X]  保留Controller，删掉Events
* [X]  重构输入

    * [X] dependency.json
    * [X] requests.json
    * [X] deployment.yaml


* [ ]  Service调度
    * [x] ServiceAllocation，部署Container或Pod，即服务的实例
    * [ ] ServiceMigration
    * [ ] ServiceScaling

* [ ]  重写submit的逻辑，让cloudlet在container中被执行

* [X]  重构输出格式
* [ ]  计算输出指标
  * [ ]  响应时间
    pod的执行时间+网络的传输时间\*2
    95分位、99分位
  * [ ]  slo违规率
    依赖于设定的slo，计算响应时间超过slo的请求条数/请求数
  * [ ]  请求的先后顺序、总数、每秒请求数QPS
    先后顺序即输入顺序，数量即累和（此处存在调度）
  * [ ]  pod&service对VM的利用率

* [ ]  网络通信建模 （参考SOFIA？）
* [ ]  让controller在通信中更有作用（参考SDN？）

* [ ]  经典场景建模
* [ ]  重新调整调度间隔
  300s调度会不会限制精度？这个间隔来源于planetLab？在微服务下合理吗？

* [ ]  实例化,不被调用的服务（endpoint为null）不会进行进行实例化和部署（资源为0）
* [ ]  思考：请求和cloudlet是如何联系上的（目前是正态分布生成）
* [ ]  完善论文素材

  * [ ]  改进架构详解
  * [ ]  补充图片素材
