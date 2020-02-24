package jm.apidemos.fridge.main;

import jm.apidemos.fridge.appmetrics.MetricsConf;
import ratpack.session.SessionModule;
import ratpack.spring.config.EnableRatpack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ratpack.spring.config.RatpackProperties;
import ratpack.spring.config.RatpackServerCustomizer;

import jm.apidemos.fridge.ratpackserver.RatPackServer;

@EnableRatpack
@EnableConfigurationProperties
@SpringBootApplication
public class FridgeRatPackApp {

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(FridgeRatPackApp.class, args);
    }

    /**
     * Extra Ratpack server configuration.
     *
     * @param ratPackProps Properties for Ratpack server configuration
     * @param metricsConf Properties for DropwizardMetricsModule.
     * @return Bean with Ratpack server configuration.
     */
    @Bean
    RatpackServerCustomizer ratpackServerSpec(final RatpackProperties ratPackProps, final MetricsConf metricsConf) {
        return new RatPackServer(ratPackProps, metricsConf);
    }

   /**
    *
    * @return MetricsConf for DropwizardMetricsModule
    */
    @Bean
    MetricsConf metricsConf() { return new MetricsConf(); }


}
