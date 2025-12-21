package ca.bazlur.modern.concurrency.c07.spring.configuration;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class TomcatConfig {

  @Bean
  public TomcatProtocolHandlerCustomizer<?> virtualThreadExecutor() {
    return protocolHandler
        -> protocolHandler.setExecutor(
              Executors.newVirtualThreadPerTaskExecutor());
  }
}
