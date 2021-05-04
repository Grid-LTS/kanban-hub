package com.github.gridlts.kanbanhub.error;

import org.immutables.value.Value;

import javax.annotation.Nullable;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC,
        builderVisibility = Value.Style.BuilderVisibility.PUBLIC, newBuilder = "builder")
public abstract class ResponseErrorDto {

    public abstract String getMessage();

    @Nullable
    public abstract String getReason();

    public static class Builder extends ImmutableResponseErrorDto.Builder {

    }
}
