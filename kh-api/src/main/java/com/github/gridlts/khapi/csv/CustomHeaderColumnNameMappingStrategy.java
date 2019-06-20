package com.github.gridlts.khapi.csv;

import com.opencsv.ICSVParser;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.util.ResourceBundle;

public class CustomHeaderColumnNameMappingStrategy<T> extends HeaderColumnNameMappingStrategy<T> {

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        if(type == null) {
            throw new IllegalStateException(ResourceBundle
                    .getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, errorLocale)
                    .getString("type.before.header"));
        }

        if(headerIndex.isEmpty()) {
            String[] header = getFieldMap().generateHeader(bean);
            headerIndex.initializeHeaderIndex(header);
        }

        return new String[0];
    }
}
