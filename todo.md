# CloudNativeSim，一个基于微服务架构的云原生应用模拟器。



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
    ```json
    {
      "services": [
        {
        "name": "front-end",
        "labels": ["front-end", "origin"],
        "calls": ["carts", "orders", "catalogue", "user", "payment", "session-db"],
        "endpoints": ["GetHealth", "GetUsers", "GetOrders"]
        }]
    }
    ```
    * [X] requests.json
      ```json
      {
          "requests": [
          {
              "method": "GET",
              "url": "/orders",
              "endpoint": "GetOrders",
              "num": 200
          }]
      }
      ```
   * [X] deployment.yaml
   ```yaml
    pods:
      - name: front-end-pod
        labels:
          - front-end
        replicas: 2
        storage: 10000
        prefix: front-end
        containers:
      - size: 1000
        pes: 1
        mips: 1000
        ram: 64
        bw: 100
   ```
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
* [ ]  服务调度融合NativeDatacenter
* [ ]  实现controller.initialize，对实体进行映射关系的初始化

  * [ ]  融合endpoint&servicegraph
  * [ ]  将各层实体通过部署算法来映射起来
* [ ]  让cloudlet在container中被执行
* [ ]  经典场景建模
* [ ]  重新调整调度间隔
  300s调度会不会限制精度？这个间隔来源于planetLab？在微服务下合理吗？
* [ ]  网络通信建模
* [ ]  服务发现&实例化
* [ ]  思考：请求和cloudlet是如何联系上的（目前是正态分布生成）
* [ ]  完善论文素材

  * [ ]  改进架构详解
  * [ ]  补充图片素材
