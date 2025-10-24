package cloud.apposs.webx.management.api;

import cloud.apposs.react.React;
import cloud.apposs.rest.annotation.Request;
import cloud.apposs.rest.annotation.RestAction;
import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.StringWriter;
import java.io.Writer;

/**
 * Prometheus API，用于提供 Prometheus 监控指标，参考
 * <pre>
 *     https://micrometer.io/docs
 *     https://grafana.com/grafana/dashboards/4701
 *     https://github.com/mweirauch/micrometer-jvm-extras
 * </pre>
 */
@RestAction
public class PrometheusApi {
    private final PrometheusMeterRegistry prometheusRegistry;

    public PrometheusApi(PrometheusMeterRegistry prometheusRegistry) {
        this.prometheusRegistry = prometheusRegistry;
        // 绑定系统指标
        new ProcessMemoryMetrics().bindTo(prometheusRegistry);
        new ProcessThreadMetrics().bindTo(prometheusRegistry);
        new ClassLoaderMetrics().bindTo(prometheusRegistry);
        new JvmMemoryMetrics().bindTo(prometheusRegistry);
        new JvmGcMetrics().bindTo(prometheusRegistry);
        new ProcessorMetrics().bindTo(prometheusRegistry);
        new JvmThreadMetrics().bindTo(prometheusRegistry);
        new JvmCompilationMetrics().bindTo(prometheusRegistry);
        new JvmInfoMetrics().bindTo(prometheusRegistry);
        new UptimeMetrics().bindTo(prometheusRegistry);
    }

    @Request.Read(value = "/prometheus", produces = TextFormat.CONTENT_TYPE_004)
    public React<String> prometheus() {
        return React.emitter(() -> {
            Writer writer = new StringWriter();
            TextFormat.write004(writer, prometheusRegistry.getPrometheusRegistry().metricFamilySamples());
            return writer.toString();
        });
    }
}
