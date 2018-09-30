package tech.ascs.icity.iform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import tech.ascs.icity.jpa.service.config.EnableJPAServices;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients("tech.ascs.icity")
@ComponentScan("tech.ascs.icity")
@EnableJPAServices("tech.ascs.icity")
public class ICityIFormApplication {

	public static void main(String[] args) {
		SpringApplication.run(ICityIFormApplication.class, args);
	}
}
