package moesif.analytics.reporter;

import com.google.gson.Gson;
import com.moesif.api.controllers.APIController;
import com.moesif.api.models.*;
import moesif.analytics.reporter.utils.MoesifConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.am.analytics.publisher.exception.MetricCreationException;
import org.wso2.am.analytics.publisher.exception.MetricReportingException;
import org.wso2.am.analytics.publisher.reporter.GenericInputValidator;
import org.wso2.am.analytics.publisher.reporter.MetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.MetricSchema;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultChoreoFaultMetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultChoreoResponseMetricEventBuilder;
import org.wso2.am.analytics.publisher.reporter.cloud.DefaultCounterMetric;
import org.wso2.am.analytics.publisher.reporter.cloud.EventQueue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MoesifChoreoParallelLogCounter extends DefaultCounterMetric {

    private static final Logger log = LoggerFactory.getLogger(MoesifChoreoParallelLogCounter.class);
    private String name;
    private MetricSchema schema;
    private final Gson gson;
    private APIController api;

    // uses to create a UUID (userID) from the userIP
    static final String UUIDNameSpace = MoesifConstants.NAMESPACE_URL;


    public MoesifChoreoParallelLogCounter(String name, MetricSchema schema,EventQueue queue, APIController  api) throws MetricCreationException {
        super(name,queue,schema);
        this.name=name;
        this.schema=schema;
        this.api = api;
        this.gson = new Gson();
    }

    @Override
    public int incrementCount(MetricEventBuilder metricEventBuilder) {
        super.incrementCount(metricEventBuilder);
        MoesifChoreoMetricEventBuilder builder = (MoesifChoreoMetricEventBuilder) metricEventBuilder;
        try {
            Map<String, Object> event = builder.buildForMoesif();
            publish(event);
        } catch (Throwable e) {
            log.error("Moesif: Not publishing event");
        }
        return 0;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public MetricSchema getSchema() {
        return this.schema;
    }

    @Override
    public MetricEventBuilder getEventBuilder() {
        switch (schema) {
            case RESPONSE:
                return new MoesifDefaultResponseMetricEventBuilder();
            case ERROR:
                return new MoesifDefaultFaultMetricEventBuilder();
            case CHOREO_RESPONSE:
                return new MoesifChoreoResponseMetricEventBuilder();
            case CHOREO_ERROR:
                return new MoesifChoreoFaultMetricEventBuilder();
            default:
                // will not happen
                return null;
        }
    }

    public void publish(Map<String,Object> event) throws Throwable {

        switch(schema) {
            case RESPONSE:
                // Async
                 ArrayList<EventModel> events = new ArrayList<>();
                 events.add(buildEventResponse(event));
                 api.createEventsBatchAsync(events,null);
                 // Sync
                // api.createEvent(buildEventResponse(event));
                break;
            case ERROR:
                api.createEvent(buildEventFault(event));
                break;
        }
    }

    public EventModel buildEventResponse(Map<String , Object>  data) throws IOException, MetricReportingException {
        String jsonString = gson.toJson(data);
        String reqBody =  jsonString.replaceAll("[\r\n]", "");

        // Preprocessing data
        final URL uri = new URL((String) data.get(MoesifConstants.DESTINATION));
        final String hostName = uri.getHost();

        final String userIP = (String) data.get(MoesifConstants.USER_IP);

        Map<String, String> reqHeaders = new HashMap<String, String>();

        reqHeaders.put("User-Agent", (String) data.getOrDefault(MoesifConstants.USER_AGENT_HEADER,MoesifConstants.UNKNOWN_VALUE));
        reqHeaders.put("Content-Type", MoesifConstants.MOESIF_CONTENT_TYPE_HEADER);
        reqHeaders.put("Host",hostName);

        Map<String, String> rspHeaders = new HashMap<String, String>();

        rspHeaders.put("Vary", "Accept-Encoding");
        rspHeaders.put("Pragma", "no-cache");
        rspHeaders.put("Expires", "-1");
        rspHeaders.put("Content-Type", "application/json; charset=utf-8");
        rspHeaders.put("Cache-Control","no-cache");


        EventRequestModel eventReq = new EventRequestBuilder()
                .time(new Date()) // See if you can parse request time stamp to date obj
                .uri(uri.toString())
                .verb((String) data.get(MoesifConstants.API_METHOD))
                .apiVersion((String) data.get(MoesifConstants.API_VERSION))
                .ipAddress(userIP)
                .headers(reqHeaders)
                .body(reqBody)
                .build();


        EventResponseModel eventRsp = new EventResponseBuilder()
                .time(new Date(System.currentTimeMillis() + 1000))
                .status((int) data.get(MoesifConstants.TARGET_RESPONSE_CODE))
                .headers(rspHeaders)
                .build();

        EventModel eventModel = new EventBuilder()
                .request(eventReq)
                .response(eventRsp)
                .userId((String) data.get("userName"))
                .companyId((String) data.get(MoesifConstants.ORGANIZATION_ID))
                .build();

        return eventModel;
    }
    public EventModel buildEventFault(Map<String , Object>  data) throws IOException, MetricReportingException {
        // Generate the event
        String jsonString = gson.toJson(data);
        String reqBody =  jsonString.replaceAll("[\r\n]", "");

        Map<String, String> reqHeaders = new HashMap<String, String>();

        reqHeaders.put("User-Agent", (String) data.get(MoesifConstants.USER_AGENT_HEADER) + " fault");

        Map<String, String> rspHeaders = new HashMap<String, String>();

        final String userIP = (String) data.get(MoesifConstants.USER_IP);

        EventRequestModel eventReq = new EventRequestBuilder()
                .time(new Date()) // See if you can parse request time stamp to date obj
                .uri((String) data.get(MoesifConstants.DESTINATION))
                .verb((String) data.get(MoesifConstants.API_METHOD))
                .apiVersion((String) data.get(MoesifConstants.API_VERSION))
                .ipAddress(userIP)
                .headers(reqHeaders)
                .body(reqBody)
                .build();


        EventResponseModel eventRsp = new EventResponseBuilder()
                .time(new Date(System.currentTimeMillis() + 1000))
                .status((int) data.get(MoesifConstants.TARGET_RESPONSE_CODE))
                .headers(rspHeaders)
                .build();

        EventModel eventModel = new EventBuilder()
                .request(eventReq)
                .response(eventRsp)
                .companyId((String) data.get(MoesifConstants.ORGANIZATION_ID))
                .build();

        return eventModel;
    }
}