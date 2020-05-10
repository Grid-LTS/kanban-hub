package com.github.gridlts.kanbanhub.sources.api.dto;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvCustomBindByName;
import org.immutables.annotate.InjectAnnotation;

@InjectAnnotation(type = CsvCustomBindByName.class, target = InjectAnnotation.Where.FIELD, code = "([[*]])")
public @interface InjectCsvCustomBindByNameAnnotation {
    Class<? extends AbstractBeanField> converter();
}
