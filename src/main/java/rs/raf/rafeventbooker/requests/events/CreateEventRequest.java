package rs.raf.rafeventbooker.requests.events;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record CreateEventRequest (
        @NotBlank String eventName,
        @NotBlank String eventDescription,
        @NotNull LocalDateTime startAt,
        @NotBlank String eventLocation,
        @Positive int categoryID,
        @Positive int authorID,
        @PositiveOrZero Integer maxCapacity,
        List<String> tags
) {}