package com.guildplugin.Listener;

import com.guildplugin.ManageDB.SQLPlayerManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.md_5.bungee.api.ChatColor;

public class MyListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        event.setJoinMessage(ChatColor.GREEN + "Welcome " + event.getPlayer().getName());
        SQLPlayerManager.addPlayer(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
    }

}