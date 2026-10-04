package io.mastercoding.vssmart.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Generic wrapper class for LiveData emissions to communicate UI state.
 *
 * @param <T> Data payload type
 */
public class Resource<T> {

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING,
        CODE_SENT
    }

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(@NonNull String msg, @Nullable T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    public static <T> Resource<T> error(@NonNull String msg) {
        return new Resource<>(Status.ERROR, null, msg);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public static <T> Resource<T> codeSent(@NonNull String verificationId) {
        return new Resource<>(Status.CODE_SENT, null, verificationId);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
