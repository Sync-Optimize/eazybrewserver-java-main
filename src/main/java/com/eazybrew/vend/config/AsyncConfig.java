package com.eazybrew.vend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuration class to enable asynchronous processing in the application.
 * This is required for methods annotated with @Async to work properly.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // No additional configuration needed
    @Bean("emailTaskExecutor")
    public TaskExecutor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Set the initial number of threads
        executor.setMaxPoolSize(10); // Set the maximum number of threads
        executor.setQueueCapacity(25); // Set the capacity of the queue
        executor.setThreadNamePrefix("EmailAsync-");
        executor.initialize();
        return executor;
    }
}