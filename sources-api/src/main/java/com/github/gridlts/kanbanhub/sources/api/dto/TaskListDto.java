package com.github.gridlts.kanbanhub.sources.api.dto;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC,
        builderVisibility = Value.Style.BuilderVisibility.PACKAGE)
public abstract class TaskListDto {

    public abstract String getId();

    public abstract String getTitle();

    public static class Builder extends ImmutableTaskListDto.Builder {
    }

}
