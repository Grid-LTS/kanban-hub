package com.github.gridlts.kanbanhub.sources.api.dto;
import com.github.gridlts.kanbanhub.sources.api.TaskResourceType;
import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
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

    public abstract TaskStatus getStatus();

    @InjectCsvCustomBindByNameAnnotation(converter = LocalDateConverter.class)
    public abstract LocalDate getCreationDate();

    @InjectCsvCustomBindByNameAnnotation(converter = LocalDateConverter.class)
    @Nullable
    public abstract LocalDate getCompleted();

    @InjectCsvBindByNameAnnotation
    public abstract TaskResourceType getSource();

    @InjectCsvBindByNameAnnotation
    @Nullable
    public abstract String getProjectCode();

    @InjectCsvBindAndSplitByNameAnnotation(elementType = String.class)
    public abstract List<String> getTags();

    public static class Builder extends ImmutableBaseTaskDto.Builder {
    }

}
