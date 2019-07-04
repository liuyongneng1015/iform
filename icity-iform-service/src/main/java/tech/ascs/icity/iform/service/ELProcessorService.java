package tech.ascs.icity.iform.service;

import tech.ascs.icity.iform.api.model.ItemInstance;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;

import java.util.List;
import java.util.Map;

/**
 * 用于处理EL表达式的服务
 *
 * @author renjie
 * @since 0.7.3
 **/
public interface ELProcessorService {


    boolean checkExpressionState(String expression);

    /**
     * 检查提交内容是否符合校验规则
     *
     * @param itemMapping id 和 itemInstance的映射, 不能为null
     * @param checkInfos  表单提交校验信息
     * @return 返回校验结果, 如果 为empty则表示校验通过, 否则里面的为错误内容
     */
    List<String> checkSubmitProcessor(Map<String, ItemInstance> itemMapping, List<FormSubmitCheckInfo> checkInfos);
}