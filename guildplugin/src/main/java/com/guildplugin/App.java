package com.guildplugin;

import com.guildplugin.Listener.MyListener;
import com.guildplugin.Listener.PlotCreateListener;
import com.guildplugin.ManageDB.LiteSQL;
import com.guildplugin.ManageDB.SQLGroupManager;
import com.guildplugin.ManageDB.SQLPlayerManager;
import com.guildplugin.commands.GroupCMD;

import org.bukkit.plugin.java.JavaPlugin;

public final class App extends JavaPlugin {

    @Override
    public void onEnable() {
        // create db
        LiteSQL.connect();
        SQLGroupManager.onCreate();
        SQLPlayerManager.onCreate();

        // CMDs
        new GroupCMD(this);

        // Listener
        getServer().getPluginManager().registerEvents(new MyListener(), this);

        // plot creation
        getServer().getPluginManager().registerEvents(new PlotCreateListener(), this);
    }

    @Override
    public void onDisable() {
        LiteSQL.disconnect();
    }

}
