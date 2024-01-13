## 待办清单

* [X]  Vm层及以下扩展了许多原有方法来支持上层功能，都以“Native”为前缀，可以向下兼容cloudsim。
* [X]  简化容器类
* [X]  代码开源和版本控制
* [X]  重构Provisioners
* [X]  由于Pe只有Vm相关的方法，把Pe全换成NativePe
* [X]  重构NativeVm、NativeDatacenter、NativeDatacenterBroker
* [X]  重构NativeCloudlet，它的提交应该在进入执行队列以后
* [X]  关掉Vm中的cloudletScheduler，重构NativeCloudletScheduler
* [X]  构建Request、Dispatcher、EndPoint、LoadBalancer
* [X]  保留Controller，删掉Events
* [X]  重构输入

    * [X] dependency.json
    * [X] requests.json
    * [X] deployment.yaml
   
* [x]  既能支持Pod，也能支持单个容器
    *[x] 将Pod透明化，pod只是container的集合操作
    *[x] 像mpis、ram、bw这些直接分配给pod不合理，因为pod只是容器的集合.所以像allocateXXXForPod这样的方法需要注释掉

* [ ]  Service 部署在 Datacenter
    * [ ] ServiceAllocation，运行在Datacenter上，根据系统负载和业务需求去创建实例
    * [ ] ContainerAllocation,运行在VM上，部署单个Container或单个Pod，即服务的实例

* [ ]  将各层实体通过部署算法来映射起来实现
   
    *[ ] vm、container、pod是先置的，实现ServiceAllocationPolicy
    *[x] requests、endpoints是先置的，根据文件定义
    *[ ] network是后置的

* [ ]  NativeVm继承SimEntity，让cloudlet在container中被执行

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
