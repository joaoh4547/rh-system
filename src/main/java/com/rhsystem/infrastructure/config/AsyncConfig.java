package com.rhsystem.infrastructure.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables {@code @Async} and runs every async method on <b>virtual threads</b>.
 *
 * <p>The executor is declared here (not as an {@code Executor} bean) on purpose: an
 * {@code Executor} bean would switch off Spring Boot's auto-configured
 * {@code applicationTaskExecutor}. One virtual thread per task — no pool to size;
 * blocking I/O (SMTP, JDBC) just parks the virtual thread.</p>
 *
 * <p>Shutdown is graceful: tasks already running get up to 30s to finish.</p>
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer, DisposableBean {

    private final SimpleAsyncTaskExecutor executor = createExecutor();

    private static SimpleAsyncTaskExecutor createExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("async-");
        executor.setVirtualThreads(true);
        executor.setTaskTerminationTimeout(30_000);
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return executor;
    }

    /**
     * Logs failures of {@code void} async methods. Parameters are deliberately NOT
     * logged — events carry activation/reset tokens.
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async task {}.{} failed", method.getDeclaringClass().getSimpleName(),
                        method.getName(), ex);
    }

    @Override
    public void destroy() {
        executor.close();
    }
}
