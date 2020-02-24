package jm.apidemos.fridge.appmetrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for {@link ratpack.dropwizard.metrics.DropwizardMetricsModule}.
 */
@ConfigurationProperties(prefix = "dwzmetrics")
public class MetricsConf {

    private boolean jmx;
    private Slf4Config slf4j = new Slf4Config();

    public boolean isJmx() {
        return jmx;
    }

    public void setJmx(final boolean jmx) {
        this.jmx = jmx;
    }

    public Slf4Config getSlf4j() {
        return slf4j;
    }

    public static class Slf4Config {
        private boolean enabled;
        private long interval = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(final long interval) {
            this.interval = interval;
        }
    }
}
