package ca.bazlur.modern.concurrency.c07.spring.configuration;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.initialize();
        return executor;
    }
}
