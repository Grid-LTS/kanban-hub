package com.github.gridlts.kanbanhub.taskw.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.github.gridlts.kanbanhub.taskw.service.DateTimeHelper.TASKW_DATE_PATTERN;


public class CustomDateDeserializer extends StdDeserializer<ZonedDateTime> {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TASKW_DATE_PATTERN).withZone(ZoneId.of("UTC"));

    public CustomDateDeserializer() {
        this(null);
    }

    public CustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ZonedDateTime deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException {
        String date = jsonparser.getText();
        try {
            return ZonedDateTime.parse(date, this.formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException(e);
        }
    }
}