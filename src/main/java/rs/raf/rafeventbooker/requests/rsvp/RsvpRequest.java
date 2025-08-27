package rs.raf.rafeventbooker.requests.rsvp;

import javax.validation.constraints.*;

public class RsvpRequest {
    @NotBlank
    @Email public String userEmail; // guest or registered user email
}