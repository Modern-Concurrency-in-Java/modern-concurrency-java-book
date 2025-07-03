package ca.bazlur.modern.concurrency.c07.spring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class TomcatConfig {

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler
                -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
