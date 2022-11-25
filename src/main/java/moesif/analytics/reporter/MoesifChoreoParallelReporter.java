package moesif.analytics.reporter;

import com.moesif.api.MoesifAPIClient;
import com.moesif.api.controllers.APIController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.reporter.*;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultAnalyticsMetricReporter;

import java.util.Map;

public class MoesifChoreoParallelReporter extends DefaultAnalyticsMetricReporter {
    private static final Logger log = LoggerFactory.getLogger(MoesifChoreoParallelReporter.class);
    protected MoesifAPIClient client;
    protected APIController api;

    public MoesifChoreoParallelReporter(Map<String, String> properties) throws MetricCreationException {

        super(properties);

        String ID = "eyJhcHAiOiIxMDUxOjI1NSIsInZlciI6IjIuMCIsIm9yZyI6IjM1MToyNzciLCJpYXQiOjE2NjcyNjA4MDB9.rPbXw_lU6E5-5Ws-DG2uhiIEIecTMBtkQpJpSBATt5o";
        client = new MoesifAPIClient(ID);
        api = APIController.getInstance();

        log.info("Successfully initialized");
    }
    @Override
    public CounterMetric createCounter(String name, MetricSchema metricSchema) throws MetricCreationException {
        MoesifChoreoParallelLogCounter logCounterMetric = new MoesifChoreoParallelLogCounter(name, metricSchema,eventQueue,api);

        return logCounterMetric;
    }


}