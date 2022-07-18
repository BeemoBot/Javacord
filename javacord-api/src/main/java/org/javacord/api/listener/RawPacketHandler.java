package org.javacord.api.listener;

import com.fasterxml.jackson.databind.JsonNode;
import org.javacord.api.DiscordApi;

/**
 * This class is extended by all RawPacketHandler.
 */
public interface RawPacketHandler {

    /**
     * This method is called whenever a discord event of matching type is dispatched.
     *
     * @param api    The discord api instance emitting this event.
     * @param type   The type of this packet, such as "GUILD_MEMBER_UPDATE".
     * @param packet The packet (the "d"-object).
     */
    void handle(DiscordApi api, String type, JsonNode packet);

}
