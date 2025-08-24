package rs.raf.rafeventbooker.model;

import rs.raf.rafeventbooker.model.enums.UserRole;
import rs.raf.rafeventbooker.model.enums.UserStatus;

import java.time.LocalDateTime;

public class User {
    private Integer userID;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private UserRole userRole;
    private UserStatus userStatus;
    private LocalDateTime createdAt;

    public User() {

    }

    public User(Integer userID, String email, String firstName, String lastName, String password, UserRole userRole, UserStatus userStatus, LocalDateTime createdAt) {
        this.userID = userID;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userRole = userRole;
        this.userStatus = userStatus;
        this.createdAt = createdAt;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
