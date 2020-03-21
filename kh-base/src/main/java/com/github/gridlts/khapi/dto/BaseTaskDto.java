package com.github.gridlts.khapi.dto;

import com.github.gridlts.khapi.resources.TaskResourceType;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC,
        builderVisibility = Value.Style.BuilderVisibility.PACKAGE)
public abstract class BaseTaskDto {

    @InjectCsvBindByNameAnnotation
    public abstract String getTaskId();

    @InjectCsvBindByNameAnnotation
    public abstract String getTitle();

    @InjectCsvBindByNameAnnotation
    @Nullable
    public abstract String getDescription();

    @InjectCsvCustomBindByNameAnnotation(converter = LocalDateConverter.class)
    public abstract LocalDate getCompleted();

    @InjectCsvBindByNameAnnotation
    public abstract TaskResourceType getSource();

    @InjectCsvBindByNameAnnotation
    @Nullable
    public abstract String getProjectCode();

    @InjectCsvBindAndSplitByNameAnnotation(elementType = String.class)
    public abstract List<String> getTags();

    public static class Builder extends com.github.gridlts.khapi.dto.ImmutableBaseTaskDto.Builder {
    }

}
