package com.github.gridlts.khapi.taskw.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.gridlts.kanbanhub.dto.CustomDateDeserializer;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Value.Immutable
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        builderVisibility = Value.Style.BuilderVisibility.PUBLIC)
@JsonDeserialize(builder = TaskwDto.Builder.class)
public abstract class TaskwDto {

    public abstract int id();

    public abstract String description();

    @JsonDeserialize(using = CustomDateDeserializer.class)
    public abstract ZonedDateTime end();

    @JsonDeserialize(using = CustomDateDeserializer.class)
    public abstract ZonedDateTime entry();

    @JsonDeserialize(using = CustomDateDeserializer.class)
    public abstract ZonedDateTime modified();

    @Nullable
    public abstract String priority();

    @Nullable
    public abstract String project();

    public abstract String status();

    public abstract List<String> tags();

    public abstract UUID uuid();

    public abstract String urgency();

    public static class Builder extends com.github.gridlts.khapi.taskw.dto.ImmutableTaskwDto.Builder {
    }
}
