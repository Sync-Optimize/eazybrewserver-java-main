package com.eazybrew.vend.config;

import com.eazybrew.vend.filter.RequestResponseLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        registrationBean.addUrlPatterns("/api/*", "/ws/*"); // Log APIs and WebSocket handshake
        registrationBean.setOrder(1); // Set order
        return registrationBean;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        System.out
                .println("==========================================================================================");
        System.out.println("REQUEST LOGGING CONFIG LOADED SUCCESSFULLY - YOU SHOULD SEE LOGS FOR API CALLS NOW");
        System.out
                .println("==========================================================================================");
    }
}
