package com.github.gridlts.kanbanhub.sources.api.dto;

import com.github.gridlts.kanbanhub.helper.DateUtilities;
import com.opencsv.bean.AbstractBeanField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends AbstractBeanField {

    @Override
    protected Object convert(String s) {
        return DateUtilities.convert(s);
    }
}

