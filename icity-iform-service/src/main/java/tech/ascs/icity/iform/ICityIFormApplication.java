package tech.ascs.icity.iform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import tech.ascs.icity.jpa.service.config.EnableJPAServices;

@EnableCaching
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients("tech.ascs.icity")
@ComponentScan("tech.ascs.icity")
@EnableJPAServices("tech.ascs.icity")
public class ICityIFormApplication {

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setAllowCredentials(true);//这两句不加不能跨域上传文件，
        corsConfiguration.setMaxAge(6000l);//加上去就可以了
        return corsConfiguration;
    }

    /**
     * 跨域过滤器
     *
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig()); // 4
        return new CorsFilter(source);
    }

	public static void main(String[] args) {
		SpringApplication.run(ICityIFormApplication.class, args);
	}
}
