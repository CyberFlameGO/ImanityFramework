package org.imanity.framework.bukkit.tablist.util.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.tablist.ImanityTablist;
import org.imanity.framework.bukkit.tablist.util.*;
import org.imanity.framework.bukkit.util.Skin;
import org.imanity.framework.bukkit.util.BukkitUtil;
import org.imanity.framework.bukkit.util.reflection.MinecraftReflection;
import org.imanity.framework.bukkit.util.reflection.version.PlayerVersion;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.UUID;

public class ProtocolLibTabImpl implements IImanityTabImpl {

    public ProtocolLibTabImpl() {
    }

    @Override
    public void registerLoginListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Imanity.PLUGIN, PacketType.Play.Server.LOGIN) {
            @Override
            public void onPacketSending(PacketEvent event) {
                event.getPacket().getIntegers().write(2, 60);
            }
        });
    }

    @Override
    public TabEntry createFakePlayer(ImanityTablist tablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        UUID uuid = UUID.randomUUID();
        final Player player = tablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        WrappedGameProfile profile = new WrappedGameProfile(uuid, playerVersion != PlayerVersion.v1_7  ? string : LegacyClientUtil.entry(rawSlot - 1) + "");
        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerVersion != PlayerVersion.v1_7 ?  "" : profile.getName()));
        if (playerVersion != PlayerVersion.v1_7) {
            playerInfoData.getProfile().getProperties().put("texture", new WrappedSignedProperty("textures", Skin.GRAY.skinValue, Skin.GRAY.skinSignature));
        }
        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        sendPacket(player, packet);
        return new TabEntry(string, uuid, "", tablist, Skin.GRAY, column, slot, rawSlot, 0);
    }

    @Override
    public void updateFakeName(ImanityTablist tablist, TabEntry tabEntry, String text) {
        if (tabEntry.getText().equals(text)) {
            return;
        }

        final Player player = tablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);
        String[] newStrings = ImanityTablist.splitStrings(text, tabEntry.getRawSlot());
        if (playerVersion == PlayerVersion.v1_7) {
            Imanity.IMPLEMENTATION.sendTeam(
                    player,
                    LegacyClientUtil.name(tabEntry.getRawSlot() - 1),
                    BukkitUtil.color(newStrings[0]),
                    newStrings.length > 1 ? BukkitUtil.color(newStrings[1]) : "",
                    Collections.singleton(LegacyClientUtil.entry(tabEntry.getRawSlot() - 1)),
                    2
            );
        }else {
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
            packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME);
            WrappedGameProfile profile = new WrappedGameProfile(
                    tabEntry.getUuid(),
                    tabEntry.getId()
            );
            PlayerInfoData playerInfoData = new PlayerInfoData(
                    profile,
                    1,
                    EnumWrappers.NativeGameMode.SURVIVAL,
                    WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', newStrings.length > 1 ? newStrings[0] + newStrings[1] : newStrings[0]))
            );
            packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
            sendPacket(player, packet);
        }
        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(ImanityTablist tablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) {
            return;
        }

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.UPDATE_LATENCY);

        WrappedGameProfile profile = new WrappedGameProfile(
                tabEntry.getUuid(),
                tabEntry.getId()
        );

        PlayerInfoData playerInfoData = new PlayerInfoData(
                profile,
                latency,
                EnumWrappers.NativeGameMode.SURVIVAL,
                WrappedChatComponent.fromText(ChatColor.translateAlternateColorCodes('&', tabEntry.getText()))
        );

        packet.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));
        sendPacket(tablist.getPlayer(), packet);
        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(ImanityTablist zigguratTablist, TabEntry tabEntry, Skin skin) {
        if (tabEntry.getTexture() == skin) {
            return;
        }

        final Player player = zigguratTablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        WrappedGameProfile profile = new WrappedGameProfile(tabEntry.getUuid(), playerVersion != PlayerVersion.v1_7  ? tabEntry.getId() : LegacyClientUtil.entry(tabEntry.getRawSlot() - 1) + "");
        PlayerInfoData playerInfoData = new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(playerVersion != PlayerVersion.v1_7 ?  "" : profile.getName()));

        if (playerVersion != PlayerVersion.v1_7) {
            playerInfoData.getProfile().getProperties().put("texture", new WrappedSignedProperty("textures", skin.skinValue, skin.skinSignature));
        }

        PacketContainer remove = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        remove.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        remove.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));


        PacketContainer add = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.PLAYER_INFO);
        add.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        add.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        sendPacket(player, remove);
        sendPacket(player, add);

        tabEntry.setTexture(skin);
    }

    @Override
    public void updateHeaderAndFooter(ImanityTablist tablist, String header, String footer) {
        PacketContainer headerAndFooter = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        final Player player = tablist.getPlayer();
        final PlayerVersion playerVersion = MinecraftReflection.getProtocol(player);

        if (playerVersion != PlayerVersion.v1_7) {

            headerAndFooter.getChatComponents().write(0, WrappedChatComponent.fromText(header));
            headerAndFooter.getChatComponents().write(1, WrappedChatComponent.fromText(footer));

            sendPacket(player, headerAndFooter);
        }
    }

    private static void sendPacket(Player player, PacketContainer packetContainer){
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
