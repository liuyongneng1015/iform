package tech.ascs.icity.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import tech.ascs.icity.common.advice.FeignRequestInterceptor;

@FeignClient(value = "iform", configuration = FeignRequestInterceptor.class)
public interface FileUploadService extends tech.ascs.icity.iform.api.service.FileUploadService {
}
