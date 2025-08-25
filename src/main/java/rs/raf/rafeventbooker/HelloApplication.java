package rs.raf.rafeventbooker;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import rs.raf.rafeventbooker.config.CORSRequestFilter;
import rs.raf.rafeventbooker.config.CORSResponseFilter;
import rs.raf.rafeventbooker.config.ObjectMapperProvider;
import rs.raf.rafeventbooker.repositories.users.MySQLUsersRepository;
import rs.raf.rafeventbooker.repositories.users.UsersRepository;
import rs.raf.rafeventbooker.services.AuthService;
import rs.raf.rafeventbooker.services.UserService;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class HelloApplication extends ResourceConfig {
    public HelloApplication() {
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                this.bind(MySQLUsersRepository.class).to(UsersRepository.class).in(Singleton.class);

                this.bindAsContract(AuthService.class);
                this.bindAsContract(UserService.class);
            }
        };

        register(binder);
        register(ObjectMapperProvider.class);
        register(CORSRequestFilter.class);
        register(CORSResponseFilter.class);

        packages("rs.raf.rafeventbooker");
    }

}