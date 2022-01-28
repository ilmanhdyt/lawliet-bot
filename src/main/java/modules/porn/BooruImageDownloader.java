package modules.porn;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import constants.Settings;
import core.MainLogger;
import core.restclient.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class BooruImageDownloader {

    public CompletableFuture<Optional<BooruImage>> getPicture(long guildId, String domain, String searchTerm,
                                                              boolean animatedOnly, boolean explicit,
                                                              Set<String> filters, List<String> skippedResults,
                                                              boolean test
    ) throws ExecutionException {
        JSONArray filtersJson = new JSONArray();
        filters = new HashSet<>(filters);
        filters.addAll(Arrays.asList(Settings.NSFW_FILTERS));
        filters.forEach(filtersJson::put);

        JSONArray skippedResultsJson = new JSONArray();
        skippedResults.forEach(skippedResultsJson::put);

        JSONObject json = new JSONObject();
        json.put("guildId", guildId);
        json.put("domain", domain);
        json.put("searchTerm", searchTerm);
        json.put("animatedOnly", animatedOnly);
        json.put("explicit", explicit);
        json.put("filters", filtersJson);
        json.put("skippedResults", skippedResultsJson);
        json.put("test", test);

        return RestClient.WEBCACHE.post("booru", "application/json", json.toString())
                .thenApply(response -> {
                    String content = response.getBody();
                    if (content.startsWith("{")) {
                        if (test) {
                            return Optional.of(new BooruImage());
                        }

                        ObjectMapper mapper = new ObjectMapper();
                        mapper.registerModule(new JavaTimeModule());
                        try {
                            BooruImage booruImage = mapper.readValue(content, BooruImage.class);
                            return Optional.of(booruImage);
                        } catch (JsonProcessingException e) {
                            MainLogger.get().error("Booru image parsing error", e);
                            return Optional.empty();
                        }
                    } else {
                        return Optional.empty();
                    }
                });
    }

}
