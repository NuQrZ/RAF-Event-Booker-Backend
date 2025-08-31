package rs.raf.rafeventbooker;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import rs.raf.rafeventbooker.config.CORSRequestFilter;
import rs.raf.rafeventbooker.config.CORSResponseFilter;
import rs.raf.rafeventbooker.config.ObjectMapperProvider;
import rs.raf.rafeventbooker.repositories.categories.CategoriesRepository;
import rs.raf.rafeventbooker.repositories.categories.MySQLCategoriesRepository;
import rs.raf.rafeventbooker.repositories.comments.CommentRepository;
import rs.raf.rafeventbooker.repositories.comments.MySQLCommentRepository;
import rs.raf.rafeventbooker.repositories.events.EventsRepository;
import rs.raf.rafeventbooker.repositories.events.MySQLEventsRepository;
import rs.raf.rafeventbooker.repositories.rsvp.MySQLRsvpRepository;
import rs.raf.rafeventbooker.repositories.rsvp.RsvpRepository;
import rs.raf.rafeventbooker.repositories.users.MySQLUsersRepository;
import rs.raf.rafeventbooker.repositories.users.UsersRepository;
import rs.raf.rafeventbooker.services.*;

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
                this.bind(MySQLRsvpRepository.class).to(RsvpRepository.class).in(Singleton.class);
                this.bind(MySQLEventsRepository.class).to(EventsRepository.class).in(Singleton.class);
                this.bind(MySQLCommentRepository.class).to(CommentRepository.class).in(Singleton.class);
                this.bind(MySQLCategoriesRepository.class).to(CategoriesRepository.class).in(Singleton.class);

                this.bindAsContract(AuthService.class);
                this.bindAsContract(UserService.class);
                this.bindAsContract(EventService.class);
                this.bindAsContract(CommentService.class);
                this.bindAsContract(CategoryService.class);
            }
        };

        register(binder);
        register(ObjectMapperProvider.class);
        register(CORSRequestFilter.class);
        register(CORSResponseFilter.class);

        packages("rs.raf.rafeventbooker");
    }

}