package com.github.gridlts.khapi.dto;

import com.github.gridlts.khapi.types.SourceManager;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC,
        builderVisibility = Value.Style.BuilderVisibility.PACKAGE)
public abstract class BaseTaskDto {


    @InjectCsvBindByNameAnnotation
    public abstract UUID getTaskId();

    @InjectCsvBindByNameAnnotation
    public abstract String getTitle();

    @InjectCsvBindByNameAnnotation
    @Nullable
    public abstract String getDescription();

    @InjectCsvCustomBindByNameAnnotation(converter = LocalDateConverter.class)
    public abstract LocalDate getCompleted();

    @InjectCsvBindByNameAnnotation
    public abstract SourceManager getSource();

    public abstract List<String> tags();

    public static class Builder extends com.github.gridlts.khapi.dto.ImmutableBaseTaskDto.Builder {
    }

}
