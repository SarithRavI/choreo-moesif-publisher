package moesif.analytics.reporter;

import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;

import java.util.Map;

public interface MoesifChoreoMetricEventBuilder extends MetricEventBuilder {

    public Map<String, Object> buildForMoesif() throws MetricReportingException;

    public boolean validateForMoesif() throws MetricReportingException;

    public Map<String, Object> buildEventForMoesif() throws MetricReportingException;
}
