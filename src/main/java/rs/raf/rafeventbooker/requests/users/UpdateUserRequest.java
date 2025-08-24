package rs.raf.rafeventbooker.requests.users;

import rs.raf.rafeventbooker.model.enums.UserRole;
import rs.raf.rafeventbooker.model.enums.UserStatus;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record UpdateUserRequest(
        @Email @NotBlank String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Size(min = 8, max = 128) String password,
        @NotNull UserRole userRole,
        @NotNull UserStatus userStatus
) {}
