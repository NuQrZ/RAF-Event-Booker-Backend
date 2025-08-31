package rs.raf.rafeventbooker.requests.events;

import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateEventRequest (
        @NotBlank String eventName,
        @NotBlank String eventDescription,
        @NotNull  LocalDateTime startAt,
        @NotBlank String eventLocation,
        @Positive int categoryID,
        @PositiveOrZero Integer maxCapacity,
        List<String> tags
) {}
