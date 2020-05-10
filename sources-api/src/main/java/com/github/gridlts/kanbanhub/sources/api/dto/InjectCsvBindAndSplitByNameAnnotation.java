package com.github.gridlts.kanbanhub.sources.api.dto;

import com.opencsv.bean.CsvBindAndSplitByName;
import org.immutables.annotate.InjectAnnotation;

@InjectAnnotation(type = CsvBindAndSplitByName.class, target = InjectAnnotation.Where.FIELD,  code = "([[*]])")
public @interface InjectCsvBindAndSplitByNameAnnotation {
    Class<?> elementType();
}
