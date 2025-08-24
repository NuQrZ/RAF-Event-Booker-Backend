package rs.raf.rafeventbooker.services;

import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.model.User;
import rs.raf.rafeventbooker.model.enums.UserRole;
import rs.raf.rafeventbooker.model.enums.UserStatus;
import rs.raf.rafeventbooker.repositories.user.UserRepository;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.Optional;

public class UserService {
    @Inject
    private UserRepository userRepository;

    public Optional<User> getUserByID(int userID) {
        if (userID <= 0) throw new BadRequestException("Invalid user id.");
        return userRepository.getUserByID(userID);
    }

    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("Email is required.");
        return userRepository.getUserByEmail(email.trim());
    }

    public int createUser(User user) {
        if (user == null) throw new BadRequestException("User cannot be null.");
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("Email is required.");
        }
        if (userRepository.emailExists(user.getEmail().trim())) {
            throw new WebApplicationException("Email already in use.", 409);
        }
        if (user.getUserRole() == null) {
            throw new BadRequestException("User role is required.");
        }
        if (user.getUserStatus() == null) {
            user.setUserStatus(UserStatus.ACTIVE);
        }
        return userRepository.createUser(user);
    }

    public void updateStatus(int userID, UserStatus userStatus) {
        if (userID <= 0) throw new BadRequestException("Invalid user id.");
        if (userStatus == null) throw new BadRequestException("UserStatus cannot be null.");

        User user = userRepository.getUserByID(userID).orElseThrow(NotFoundException::new);
        if (user.getUserRole() == UserRole.ADMIN && userStatus != UserStatus.ACTIVE) {
            throw new ForbiddenException("Admin cannot be deactivated!");
        }

        int affected = userRepository.updateStatus(userID, userStatus);
        if (affected == 0) throw new NotFoundException(); // race condition fallback
    }

    public void updateUser(User user) {
        if (user == null || user.getUserID() == null) {
            throw new BadRequestException("User id is required.");
        }

        User existing = userRepository.getUserByID(user.getUserID())
                .orElseThrow(NotFoundException::new);

        if (user.getEmail() != null &&
                !user.getEmail().trim().equalsIgnoreCase(existing.getEmail()) &&
                userRepository.emailExists(user.getEmail().trim())) {
            throw new WebApplicationException("Email already in use.", 409);
        }

        int affected = userRepository.updateUser(user);
        if (affected == 0) throw new NotFoundException();
    }

    public void deleteUser(int userID) {
        if (userID <= 0) throw new BadRequestException("Invalid user id.");

        User existing = userRepository.getUserByID(userID).orElseThrow(NotFoundException::new);
        if (existing.getUserRole() == UserRole.ADMIN) {
            throw new ForbiddenException("Admin accounts cannot be deleted.");
        }

        boolean deleted = userRepository.deleteUser(userID);
        if (!deleted) throw new NotFoundException();
    }

    public Page<User> getAllUsers(int page, int size) {
        if (page < 0 || size <= 0) throw new BadRequestException("Invalid pagination.");
        return userRepository.getAllUsers(page, size);
    }
}
