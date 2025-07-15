package com.drk.SpringAIDemo;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 确保 @SpringBootApplication 注解是干净的，没有 exclude
@SpringBootApplication
// 确保 MyBatis-Plus 知道去哪里扫描你的 Mapper 接口
@MapperScan("com.drk.SpringAIDemo.mapper")
public class SpringAiDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiDemoApplication.class, args);
	}

}
