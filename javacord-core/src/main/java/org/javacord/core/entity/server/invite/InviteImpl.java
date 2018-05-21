package org.javacord.core.entity.server.invite;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.server.invite.RichInvite;
import org.javacord.api.entity.user.User;
import org.javacord.core.DiscordApiImpl;
import org.javacord.core.entity.IconImpl;
import org.javacord.core.util.logging.LoggerUtil;
import org.javacord.core.util.rest.RestEndpoint;
import org.javacord.core.util.rest.RestMethod;
import org.javacord.core.util.rest.RestRequest;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The implementation of {@link Invite}.
 */
public class InviteImpl implements RichInvite {

    /**
     * The logger of this class.
     */
    private static final Logger logger = LoggerUtil.getLogger(IconImpl.class);

    /**
     * The discord api instance.
     */
    private final DiscordApi api;

    /**
     * The code of the invite.
     */
    private final String code;

    /**
     * The id of the server.
     */
    private final long serverId;

    /**
     * The name of the server.
     */
    private final String serverName;

    /**
     * The icon hash of the server.
     */
    private final String serverIcon;

    /**
     * The splash of the server.
     */
    private final String serverSplash;

    /**
     * The id of the channel.
     */
    private final long channelId;

    /**
     * The name of the channel.
     */
    private final String channelName;

    /**
     * The type of the channel.
     */
    private final ChannelType channelType;

    /**
     * The creator of the invite. May be <code>null</code>.
     */
    private final User inviter;

    /**
     * The number of times this invite has been used.
     */
    private final int uses;

    /**
     * The max number of times this invite can be used.
     */
    private final int maxUses;

    /**
     * The duration (in seconds) after which the invite expires.
     */
    private final int maxAge;

    /**
     * Whether the invite only grants temporary membership or not.
     */
    private final boolean temporary;

    /**
     * The creation date of the invite.
     */
    private final Instant creationTimestamp;

    /**
     * Whether the invite is revoked or not.
     */
    private final boolean revoked;

    /**
     * Creates a new invite.
     *
     * @param api The discord api instance.
     * @param data The json data of the invite.
     */
    public InviteImpl(DiscordApi api, JsonNode data) {
        this.api = api;
        this.code = data.get("code").asText();
        this.serverId = Long.parseLong(data.get("guild").get("id").asText());
        this.serverName = data.get("guild").get("name").asText();
        this.serverIcon = data.get("guild").has("icon") && !data.get("guild").get("icon").isNull()
                ? data.get("guild").get("icon").asText()
                : null;
        this.serverSplash = data.get("guild").has("splash") && !data.get("guild").get("splash").isNull()
                ? data.get("guild").get("splash").asText()
                : null;
        this.channelId = Long.parseLong(data.get("channel").get("id").asText());
        this.channelName = data.get("channel").get("name").asText();
        this.channelType = ChannelType.fromId(data.get("channel").get("type").asInt());

        // Rich data (may not be present)
        this.inviter = data.has("inviter")
                ? ((DiscordApiImpl) api).getOrCreateUser(data.get("inviter"))
                : null;
        this.uses = data.has("uses") ? data.get("uses").asInt() : -1;
        this.maxUses = data.has("max_uses") ? data.get("max_uses").asInt() : -1;
        this.maxAge = data.has("max_age") ? data.get("max_age").asInt() : -1;
        this.temporary = data.has("temporary") && data.get("temporary").asBoolean();
        this.creationTimestamp = data.has("created_at")
                ? OffsetDateTime.parse(data.get("created_at").asText()).toInstant()
                : null;
        this.revoked = data.has("revoked") && data.get("revoked").asBoolean();
    }

    /**
     * Gets the discord api instance.
     *
     * @return The discord api instance.
     */
    public DiscordApi getApi() {
        return api;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public Optional<Server> getServer() {
        return api.getServerById(serverId);
    }

    @Override
    public long getServerId() {
        return serverId;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public Optional<Icon> getServerIcon() {
        if (serverIcon == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(
                    api, new URL("https://cdn.discordapp.com/icons/"
                                 + Long.toUnsignedString(getServerId()) + "/" + serverIcon + ".png")));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Icon> getServerSplash() {
        if (serverSplash == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(new IconImpl(
                    api, new URL("https://cdn.discordapp.com/splashes/"
                                 + Long.toUnsignedString(getServerId()) + "/" + serverSplash + ".png")));
        } catch (MalformedURLException e) {
            logger.warn("Seems like the url of the icon is malformed! Please contact the developer!", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ServerChannel> getChannel() {
        return api.getServerChannelById(channelId);
    }

    @Override
    public long getChannelId() {
        return channelId;
    }

    @Override
    public String getChannelName() {
        return channelName;
    }

    @Override
    public ChannelType getChannelType() {
        return channelType;
    }

    @Override
    public CompletableFuture<Void> delete(String reason) {
        return new RestRequest<Void>(getApi(), RestMethod.DELETE, RestEndpoint.INVITE)
                .setUrlParameters(getCode())
                .setAuditLogReason(reason)
                .execute(result -> null);
    }

    @Override
    public User getInviter() {
        return inviter;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public int getMaxUses() {
        return maxUses;
    }

    @Override
    public int getMaxAgeInSeconds() {
        return maxAge;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    @Override
    public boolean isRevoked() {
        return revoked;
    }
}