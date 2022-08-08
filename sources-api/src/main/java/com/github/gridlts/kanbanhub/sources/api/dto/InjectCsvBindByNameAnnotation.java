package com.github.gridlts.kanbanhub.sources.api.dto;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import org.immutables.annotate.InjectAnnotation;

@InjectAnnotation(type = CsvBindByName.class, target = InjectAnnotation.Where.FIELD, code = "([[*]])")
public @interface InjectCsvBindByNameAnnotation {
    String column() default "";
}
