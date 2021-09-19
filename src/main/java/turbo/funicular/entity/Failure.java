package turbo.funicular.entity;

import io.vavr.control.Option;
import lombok.Builder;
import lombok.Value;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Value
@Builder
public class Failure {

    public static final String DEFAULT_CODE = "failure.code.default";
    public static final String DEFAULT_I18N_CODE = "failure.i18.default";

    @Nonnull
    @Builder.Default
    String code = DEFAULT_CODE;

    @Nonnull
    String reason;

    @Builder.Default
    List<Detail> details = List.of();
    @Nonnull
    @Builder.Default
    Option<Throwable> cause = Option.none();

    @Builder.Default
    I18nData i18nData = I18nData.instance;

    public enum ErrorType {
        UNEXPECTED, VALIDATION, BUSINESS
    }

    @Value
    @Builder
    public static class I18nData {
        /**
         * The code used for getting the message from MessageSource
         */
        @Nonnull
        @Builder.Default
        String code = DEFAULT_I18N_CODE;
        /**
         * Default message in case not found in MessageSource
         */
        @Nonnull
        @Builder.Default
        String defaultMessage = "";

        @Nonnull
        @Builder.Default
        SortedSet<Object> arguments = new TreeSet<>();

        public static I18nData instance = Failure.I18nData.builder().build();
    }

    @Value
    @Builder
    public static class Detail {
        /**
         * An end user friendly message with the information about the error.
         */
        @Builder.Default
        String localizedMessage = "";
        /**
         * The key to load from the WebApi client the message if desired. It could be empty.
         */
        @Builder.Default
        String codeMessage = "";
        /**
         * The error type just to give more context
         */
        @Builder.Default
        ErrorType type = ErrorType.UNEXPECTED;
        /**
         * Useful to know what field of a given object had an error, useful for displaying in user interfaces if needed. It could be empty
         */
        @Builder.Default
        String path = "";
    }

    /**
     * Create a {@link Failure} with the given parameters
     *
     * @param cause    exception caught
     * @param i18nCode the given {@code i18nCode}
     * @param reason   simple message of the failure
     * @return a fresh instance
     */
    @Nonnull
    public static Failure of(final @Nonnull Throwable cause,
                             final @Nonnull String i18nCode,
                             final @Nonnull String reason) {
        final var i18nData = Failure.I18nData.builder()
            .defaultMessage(reason)
            .code(i18nCode)
            .build();
        return Failure.builder()
            .i18nData(i18nData)
            .code(i18nCode)
            .reason(reason)
            .cause(Option.of(cause))
            .build();
    }

    /**
     * Create a {@link Failure} with the given parameters
     *
     * @param i18nCode the given {@code i18nCode}
     * @param reason   simple message of the failure
     * @return a fresh instance
     */
    @Nonnull
    public static Failure of(final @Nonnull String i18nCode,
                             final @Nonnull String reason) {
        final var i18nData = Failure.I18nData.builder()
            .defaultMessage(reason)
            .code(i18nCode)
            .build();
        return Failure.builder()
            .i18nData(i18nData)
            .code(i18nCode)
            .reason(reason)
            .build();
    }
}
