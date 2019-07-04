package tech.ascs.icity.iform.utils;

import tech.ascs.icity.iform.api.model.FormSubmitCheckModel;
import tech.ascs.icity.iform.model.FormSubmitCheckInfo;

/**
 * Dto转换工具, 用于由model转换为dto
 * @author renjie
 * @since 0.7.3
 **/
public class DtoUtils {

    public static FormSubmitCheckModel toFormSubmitCheckModel(FormSubmitCheckInfo info) {
        FormSubmitCheckModel model = new FormSubmitCheckModel();
        model.setCueExpression(info.getCueExpression());
        model.setCueWords(info.getCueWords());
        model.setOrderNo(info.getOrderNo());
        model.setId(info.getId());
        model.setName(info.getName());
        return model;
    }
}
