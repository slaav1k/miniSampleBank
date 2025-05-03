package com.authservice.observability;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.StreamSupport;

@Component
public class CustomObservationHandler implements ObservationHandler<Observation.Context> {
    private static final Logger logger = LoggerFactory.getLogger(CustomObservationHandler.class);

    @Override
    public void onStart(Observation.Context context) {
        logger.info("Начало наблюдения для контекста [{}], операция [{}]",
                context.getName(), getOperationFromContext(context));
    }

    @Override
    public void onStop(Observation.Context context) {
        logger.info("Конец наблюдения для контекста [{}], операция [{}]",
                context.getName(), getOperationFromContext(context));
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }

    private String getOperationFromContext(Observation.Context context) {
        return StreamSupport.stream(context.getLowCardinalityKeyValues().spliterator(), false)
                .filter(keyValue -> "operation".equals(keyValue.getKey()))
                .map(KeyValue::getValue)
                .findFirst()
                .orElse("UNKNOWN");
    }
}