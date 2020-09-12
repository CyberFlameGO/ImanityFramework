package org.imanity.framework.bukkit.util;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.imanity.framework.util.thread.ServerThreadLock;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Setter
public class Skin {

    private static final LoadingCache<String, Skin> SKIN_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(60L, TimeUnit.SECONDS)
            .initialCapacity(60)
            .build(new CacheLoader<String, Skin>() {
                @Override
                public @Nullable Skin load(@NonNull String s) throws Exception {

                    Player player = Bukkit.getPlayer(s);
                    if (player != null) {
                        try (ServerThreadLock lock = ServerThreadLock.obtain()) {

                            EntityPlayer playerNMS = ((CraftPlayer) player).getHandle();
                            GameProfile profile = playerNMS.getProfile();

                            if (!profile.getProperties().get("textures").isEmpty()) {
                                Property property = profile.getProperties().get("textures").iterator().next();
                                String texture = property.getValue();
                                String signature = property.getSignature();

                                return new Skin(texture, signature);
                            }
                        }
                        return null;
                    }

                    return Skin.download(s);
                }
            });
    public static Skin GRAY = new Skin(
            "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=",
            "u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw="
    );

    public String skinValue;
    public String skinSignature;

    public Skin(String skinValue, String skinSig) {
        this.skinValue = skinValue;
        this.skinSignature = skinSig;
    }

    @Override
    public String toString() {
        return skinSignature + skinValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Skin skin = (Skin) o;

        if (!Objects.equals(skinValue, skin.skinValue)) return false;
        return Objects.equals(skinSignature, skin.skinSignature);
    }

    @Override
    public int hashCode() {
        int result = skinValue != null ? skinValue.hashCode() : 0;
        result = 31 * result + (skinSignature != null ? skinSignature.hashCode() : 0);
        return result;
    }

    public static Skin fromPlayer(Player player) {
        Skin skin = SKIN_CACHE.get(player.getName());

        return skin == null ? Skin.GRAY : skin;
    }

    public static Skin download(String name) throws Exception {
        URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());

        String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

        if (uuid == null || uuid.isEmpty()) {
            return Skin.GRAY;
        }

        URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
        JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
        String texture = textureProperty.get("value").getAsString();
        String signature = textureProperty.get("signature").getAsString();

        return new Skin(texture, signature);
    }
}
