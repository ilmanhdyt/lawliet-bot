package events.sync;

import java.time.Duration;
import core.AsyncTimer;
import core.MainLogger;
import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.json.JSONObject;

@Path("")
@Singleton
public class RestService {

    @POST
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String event(@PathParam("name") String name, String json) {
        try (AsyncTimer timer = new AsyncTimer(Duration.ofSeconds(5))) {
            JSONObject requestJson = new JSONObject(json);
            JSONObject responseJson = null;
            try {
                responseJson = EventManager.getEvent(name).apply(requestJson);
            } catch (Throwable e) {
                MainLogger.get().error("Error in event \"{}\"", name, e);
            }
            if (responseJson == null) {
                responseJson = new JSONObject();
            }
            return responseJson.toString();
        } catch (Throwable e) {
            MainLogger.get().error("Error in event \"{}\"", name, e);
            return new JSONObject().toString();
        }
    }

}
