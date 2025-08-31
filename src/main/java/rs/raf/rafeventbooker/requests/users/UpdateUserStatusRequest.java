package rs.raf.rafeventbooker.requests.users;

import rs.raf.rafeventbooker.model.enums.UserStatus;

import javax.validation.constraints.NotBlank;

public record UpdateUserStatusRequest(
        @NotBlank UserStatus userStatus
) {}
