package rs.raf.rafeventbooker.repositories.users;

import rs.raf.rafeventbooker.model.Page;
import rs.raf.rafeventbooker.model.User;
import rs.raf.rafeventbooker.model.enums.UserStatus;

import java.util.Optional;

public interface UsersRepository {
    Optional<User> getUserByID(int userID);
    Optional<User> getUserByEmail(String email);
    boolean emailExists(String email);
    int createUser(User user);
    int updateUser(User user);
    int updateStatus(int userID, UserStatus status);
    boolean deleteUser(int userID);
    Page<User> getAllUsers(int page, int size);
}
