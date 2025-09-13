package com.drk.SpringAIDemo.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

/**
 * @Author glk
 * @Date 2025/8/5 16:24
 * @Version 1.0
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        // 1. 创建CorsConfiguration对象
        CorsConfiguration config = new CorsConfiguration();

        // 2. 设置允许的来源
        // 已根据您的 vite.config.js 文件更新为正确的地址
        config.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // 3. 设置允许的请求头，"*" 表示允许所有
        config.setAllowedHeaders(Collections.singletonList("*"));

        // 4. 设置允许的请求方法，"*" 表示允许所有
        config.setAllowedMethods(Collections.singletonList("*"));

        // 5. 设置是否允许发送凭证（如Cookie）
        config.setAllowCredentials(true);

        // 6. 设置预检请求的有效期，单位为秒。
        config.setMaxAge(3600L);

        // 7. 创建UrlBasedCorsConfigurationSource对象，并为所有路径应用CORS配置
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 8. 创建FilterRegistrationBean并注册CorsFilter
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));

        // 9. 设置过滤器的顺序为最高优先级，确保它在其他安全过滤器之前执行
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return bean;
    }
}
