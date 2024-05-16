/*
 * Copyright ©2024. Jingfeng Wu.
 */

package policy.dispatch;

import entity.Instance;

import java.util.List;
import java.util.Map;

public abstract class RequestDispatchPolicy {
    private List<Instance> instanceList;
    private Map<Integer,Instance> workloadMap;
    int num = 1;
//        for (int i = 0; i < num; i++) { //TODO: 请求的批大小不对
//            Request request = requests.get(i);
//            String API = request.getAPI();
//            // 获取请求映射的源服务
//            Service source = request.getChain().get(0);
//            // 源服务创建属于这个request的cloudlets,数量为服务的端点数
//            int endpoints = source.getApiMap().get(API);
//            List<NativeCloudlet> source_Native_cloudlets = source.createCloudlets(request,endpoints);
//            // 提交cloudlets
//            submitCloudlets(source_Native_cloudlets);
//            // 发送处理任务的事件
//            sendNow(getId(), NativeSimTag.CLOUDLET_PROCESS, source_Native_cloudlets);
//        }
}
