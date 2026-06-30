package com.gltu.labreservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI labReservationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("高校实验室预约申请与设备管理系统接口文档")
                        .version("1.0.0")
                        .description("用于课程答辩和前后端联调的 OpenAPI/Knife4j 接口文档"));
    }
}
