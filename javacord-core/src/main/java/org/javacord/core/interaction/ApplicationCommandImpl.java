package org.javacord.core.interaction;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.ApplicationCommand;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class ApplicationCommandImpl implements ApplicationCommand {

    private final DiscordApiImpl api;
    private final long id;
    private final long applicationId;
    private final String name;
    private final boolean defaultPermission;
    private final String description;

    private final long server;

    /**
     * Class constructor.
     *
     * @param api  The api instance.
     * @param data The json data of the application command.
     */
    protected ApplicationCommandImpl(DiscordApiImpl api, JsonNode data) {
        this.api = api;
        id = data.get("id").asLong();
        applicationId = data.get("application_id").asLong();
        name = data.get("name").asText();
        description = data.get("description").asText();
        defaultPermission = !data.hasNonNull("default_permission") || data.get("default_permission").asBoolean();
        server = data.has("guild_id")
                ? data.get("guild_id").asLong()
                : 0L;
    }

    @Override
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getApplicationId() {
        return applicationId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean getDefaultPermission() {
        return defaultPermission;
    }

    @Override
    public Optional<Long> getServerId() {
        if (!isServerApplicationCommand()) {
            return Optional.empty();
        }

        return Optional.of(server);
    }

    @Override
    public Optional<Server> getServer() {
        if (!isServerApplicationCommand()) {
            return Optional.empty();
        }

        return api.getPossiblyUnreadyServerById(server);
    }

    @Override
    public boolean isGlobalApplicationCommand() {
        return server == 0L;
    }

    @Override
    public boolean isServerApplicationCommand() {
        return server != 0L;
    }

    @Override
    public CompletableFuture<Void> deleteGlobal() {
        return new RestRequest<Void>(getApi(), RestMethod.DELETE, RestEndpoint.APPLICATION_COMMANDS)
                .setUrlParameters(String.valueOf(getApplicationId()), getIdAsString())
                .execute(result -> null);
    }

    @Override
    public CompletableFuture<Void> deleteForServer(Server server) {
        return deleteForServer(server.getId());
    }

    @Override
    public CompletableFuture<Void> deleteForServer(long server) {
        return new RestRequest<Void>(getApi(), RestMethod.DELETE, RestEndpoint.SERVER_APPLICATION_COMMANDS)
                .setUrlParameters(String.valueOf(getApplicationId()), String.valueOf(server), getIdAsString())
                .execute(result -> null);
    }
}
