package com.github.gridlts.khapi.dto;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import org.immutables.annotate.InjectAnnotation;

@InjectAnnotation(type = CsvBindByName.class, target = InjectAnnotation.Where.FIELD)
public @interface InjectCsvBindByNameAnnotation {
}
