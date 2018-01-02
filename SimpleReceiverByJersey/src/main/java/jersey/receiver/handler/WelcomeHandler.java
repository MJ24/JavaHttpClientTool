package jersey.receiver.handler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/welcome")
public class WelcomeHandler {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayWelcome() {
        return "Welcome to Jersey world";
    }
}