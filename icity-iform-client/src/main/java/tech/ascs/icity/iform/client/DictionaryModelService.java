package tech.ascs.icity.iform.client;

import org.springframework.cloud.openfeign.FeignClient;
import tech.ascs.icity.common.advice.FeignRequestInterceptor;

@FeignClient(value = "iform", configuration = FeignRequestInterceptor.class)
public interface DictionaryModelService extends tech.ascs.icity.iform.api.service.DictionaryDataService {

}