package ServerStuff;

import Constants.Settings;
import Core.Internet.HttpRequest;
import Core.Internet.HttpProperty;
import Core.Internet.HttpResponse;
import Core.SecretManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Botsfordiscord {

    final static Logger LOGGER = LoggerFactory.getLogger(Botsfordiscord.class);

    public static boolean updateServerCount(int serverCount) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("server_count", String.valueOf(serverCount));
            HttpProperty[] properties = new HttpProperty[]{
                    new HttpProperty("Content-Type", "application/json"),
                    new HttpProperty("Authorization", SecretManager.getString("botsfordiscord.token"))
            };
            HttpResponse httpResponse = HttpRequest.getData("https://botsfordiscord.com/api/bot/" + Settings.LAWLIET_ID, jsonObject.toString(), properties).get();
            return httpResponse.getCode() == 200;
        } catch (IOException | InterruptedException | ExecutionException e) {
            LOGGER.error("Could not post Botsfordiscord request", e);
        }
        return false;
    }

}
