package moesif.analytics.reporter;

import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.am.analytics.publisher.reporter.GenericInputValidator;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultResponseMetricEventBuilder;
import org.wso2.am.analytics.publisher.util.EventMapAttributeFilter;

import java.util.HashMap;
import java.util.Map;

public class MoesifDefaultResponseMetricEventBuilder extends DefaultResponseMetricEventBuilder implements MoesifChoreoMetricEventBuilder {

    protected Map<String, Class> requiredAttributesForMoesif;
    private Map<String, Object> moesifEventMap;
    private Boolean isMoesifBuilt = false;

    public MoesifDefaultResponseMetricEventBuilder(){
        super();
        this.requiredAttributesForMoesif = GenericInputValidator.getInstance().getEventProperties(MetricSchema.RESPONSE);
        this.moesifEventMap = new HashMap<>();
    }

    public MoesifDefaultResponseMetricEventBuilder(Map<String,Class> requiredAttributesForMoesif){
        super();
        this.requiredAttributesForMoesif = requiredAttributesForMoesif;
        this.moesifEventMap = new HashMap<>();
    }

    public Map<String, Object> buildForMoesif() throws MetricReportingException {
        if (validateForMoesif()) {
            return buildEventForMoesif();
        }
        throw new MetricReportingException("Validation failure occurred when building the event");
    }


    public Map<String, Object> buildEventForMoesif() throws MetricReportingException {
        if (!isMoesifBuilt) {
            // util function to filter required attributes
            moesifEventMap = EventMapAttributeFilter.getInstance().filter(moesifEventMap,requiredAttributesForMoesif);

            isMoesifBuilt = true;
        }
        return moesifEventMap;
    }


    public boolean validateForMoesif() throws MetricReportingException {
        return true;
    }

    @Override
    public MetricEventBuilder addAttribute(String key, Object value) throws MetricReportingException {
        // TODO: Check for concurrency issues.
        super.eventMap.put(key, value);
        moesifEventMap.put(key, value);
        return this;
    }
}
