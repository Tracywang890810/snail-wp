package com.seblong.wp.config;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.fasterxml.classmate.TypeResolver;

@Configuration
// 注解开启 swagger2 功能
@EnableSwagger2
public class Swagger2Config {

	// 是否开启swagger，正式环境一般是需要关闭的
	@Value("${swagger.enabled}")
	private boolean enableSwagger;

	@Autowired
	private TypeResolver typeResolver;

	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.host("api.snailsleep.net")
				.apiInfo(apiInfo())
				// 是否开启 (true 开启 false隐藏。生产环境建议隐藏)
				.enable(enableSwagger)
				.select()
				// 扫描的路径包,设置basePackage会将包下的所有被@Api标记类的所有方法作为api
				.apis(RequestHandlerSelectors.basePackage("com.seblong.wp.controllers"))
				// 指定路径处理PathSelectors.any()代表所有的路径
				.paths(PathSelectors.any())
				.build()
				.alternateTypeRules(
						newRule(typeResolver.resolve(ResponseEntity.class, WildcardType.class),
								typeResolver.resolve(WildcardType.class)));
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
		// 设置文档标题(API名称)
				.title("许愿池项目接口文档")
				// 文档描述
				.description("项目地址：https://api.snailsleep.net/snail-wp-v3")
				// 服务条款URL
				.termsOfServiceUrl("http://127.0.0.1:8080/")
				// 版本号
				.version("1.0.0").build();
	}

}
