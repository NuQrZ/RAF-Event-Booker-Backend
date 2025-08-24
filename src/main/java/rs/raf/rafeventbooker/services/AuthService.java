package rs.raf.rafeventbooker.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.mindrot.jbcrypt.BCrypt;
import rs.raf.rafeventbooker.model.User;
import rs.raf.rafeventbooker.model.enums.UserStatus;
import rs.raf.rafeventbooker.repositories.user.UserRepository;
import rs.raf.rafeventbooker.requests.login.LoginRequest;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class AuthService {
    @Inject
    private UserRepository userRepository;

    private static final String JWT_SECRET = Optional.ofNullable(System.getenv("JWT_SECRET"))
            .orElse("CHANGE_ME_SUPER_SECRET_AND_LONG");
    private static final long JWT_TTL_MILLIS = 1000L * 60 * 60 * 12; // 12h
    private static final String ISSUER = Optional.ofNullable(System.getenv("JWT_ISSUER")).orElse("raf-event-booker");

    private Algorithm algorithm() {
        return Algorithm.HMAC256(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public Response login(LoginRequest loginRequest) {
        if (loginRequest == null || loginRequest.email() == null || loginRequest.password() == null) {
            throw new BadRequestException("Invalid credentials!");
        }
        Optional<User> optionalUser = userRepository.getUserByEmail(loginRequest.email());

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Invalid email!");
        }

        User user = optionalUser.get();

        boolean checkPassword = BCrypt.checkpw(loginRequest.password(), user.getPassword());
        if (!checkPassword) {
            throw new ForbiddenException("Invalid password!");
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("User is not active!");
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + JWT_TTL_MILLIS);

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withSubject(String.valueOf(user.getUserID()))
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getUserRole().name())
                .sign(algorithm());

        Map<String, Object> body = Map.of(
                "token", token,
                "role", user.getUserRole().name(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName()
        );

        return Response.ok(body).build();
    }
}
