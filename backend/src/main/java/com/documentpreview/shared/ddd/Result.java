package com.documentpreview.shared.ddd;

import java.util.Objects;
import java.util.Optional;

/**
 * A generic result class that represents either a success or a failure.
 * This is used to avoid throwing exceptions for expected error conditions.
 * 
 * @param <T> The type of the value in case of success.
 */
public class Result<T> {
    private final T value;
    private final String errorMessage;
    private final boolean isSuccess;

    private Result(T value, String errorMessage, boolean isSuccess) {
        this.value = value;
        this.errorMessage = errorMessage;
        this.isSuccess = isSuccess;
    }

    /**
     * Creates a success result with the given value.
     * 
     * @param <T> The type of the value.
     * @param value The value to wrap in the result.
     * @return A success result.
     */
    public static <T> Result<T> success(T value) {
        return new Result<>(value, null, true);
    }

    /**
     * Creates a failure result with the given error message.
     * 
     * @param <T> The type of the value (which will be null).
     * @param errorMessage The error message describing the failure.
     * @return A failure result.
     */
    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(null, Objects.requireNonNull(errorMessage, "Error message cannot be null"), false);
    }

    /**
     * Checks if this result is a success.
     * 
     * @return true if this result is a success, false otherwise.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Checks if this result is a failure.
     * 
     * @return true if this result is a failure, false otherwise.
     */
    public boolean isFailure() {
        return !isSuccess;
    }

    /**
     * Gets the value of this result if it's a success.
     * 
     * @return An Optional containing the value if this is a success, or empty otherwise.
     */
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Gets the error message of this result if it's a failure.
     * 
     * @return An Optional containing the error message if this is a failure, or empty otherwise.
     */
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    /**
     * Gets the value of this result if it's a success, otherwise throws an IllegalStateException.
     * 
     * @return The value of this result.
     * @throws IllegalStateException If this result is a failure.
     */
    public T getOrThrow() {
        if (isFailure()) {
            throw new IllegalStateException(errorMessage);
        }
        return value;
    }

    /**
     * Maps the value of this result to another type using the given mapper function.
     * If this result is a failure, the mapping is skipped and a failure result with the same error message is returned.
     * 
     * @param <U> The type of the mapped value.
     * @param mapper The function to map the value.
     * @return A new result with the mapped value if this is a success, or a failure result otherwise.
     */
    public <U> Result<U> map(java.util.function.Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        if (isSuccess()) {
            return Result.success(mapper.apply(value));
        } else {
            return Result.failure(errorMessage);
        }
    }

    /**
     * Flat maps the value of this result to another result using the given mapper function.
     * If this result is a failure, the mapping is skipped and a failure result with the same error message is returned.
     * 
     * @param <U> The type of the value in the mapped result.
     * @param mapper The function to map the value to another result.
     * @return The result of applying the mapper function if this is a success, or a failure result otherwise.
     */
    public <U> Result<U> flatMap(java.util.function.Function<? super T, Result<U>> mapper) {
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        if (isSuccess()) {
            return mapper.apply(value);
        } else {
            return Result.failure(errorMessage);
        }
    }
}