package tech.ascs.icity.iform;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tech.ascs.icity.iform.api.model.*;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("tech.ascs.icity.iform"))
                .paths(PathSelectors.any())
                .build()
                .additionalModels(typeResolver.resolve(FileItemModel.class), typeResolver.resolve(ReferenceItemModel.class), typeResolver.resolve(RowItemModel.class),
                        typeResolver.resolve(SelectItemModel.class),typeResolver.resolve(SerialNumberItemModel.class),typeResolver.resolve(SubFormItemModel.class),
                        typeResolver.resolve(SubFormRowItemModel.class),typeResolver.resolve(TimeItemModel.class)).useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("iCity 业务支撑平台iform API V1.0")
                .description("iCity 业务支撑平台iform API，包括数据表建模,列表建模,表单建模等功能")
                .version("1.0")
                .build();
    }
}
