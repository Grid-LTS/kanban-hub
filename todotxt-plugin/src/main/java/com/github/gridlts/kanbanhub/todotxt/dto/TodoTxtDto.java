package com.github.gridlts.kanbanhub.todotxt.dto;

import com.github.gridlts.kanbanhub.sources.api.TaskStatus;
import org.immutables.value.Value;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.List;

@Value.Immutable
@Value.Style(
        visibility = Value.Style.ImplementationVisibility.PUBLIC,
        builderVisibility = Value.Style.BuilderVisibility.PUBLIC)
public abstract class TodoTxtDto {

    public abstract String id();

    public abstract String title();
    public abstract String description();

    @Nullable
    public abstract ZonedDateTime end();

    public abstract ZonedDateTime entry();

    public abstract ZonedDateTime modified();

    @Nullable
    public abstract String priority();

    @Nullable
    public abstract String project();

    public abstract TaskStatus status();

    public abstract List<String> tags();

    public static class Builder extends com.github.gridlts.kanbanhub.todotxt.dto.ImmutableTodoTxtDto.Builder {
    }
}
