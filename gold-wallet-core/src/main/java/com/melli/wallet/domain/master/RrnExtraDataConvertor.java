package com.melli.wallet.domain.master;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Converter
@Log4j2
@Service
public class RrnExtraDataConvertor implements AttributeConverter<RrnExtraData, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(RrnExtraData stockBrokerExtraData) {
        try {
            return objectMapper.writeValueAsString(stockBrokerExtraData);
        } catch (JsonProcessingException jpe) {
            log.warn("Cannot convert RrnExtraData into JSON");
            return null;
        }
    }

    @Override
    public RrnExtraData convertToEntityAttribute(String value) {
        try {
            if(value == null){
                return null;
            }
            return objectMapper.readValue(value, RrnExtraData.class);
        } catch (JsonProcessingException e) {
            log.warn("Cannot convert JSON into RrnExtraData");
            return null;
        }
    }
}
