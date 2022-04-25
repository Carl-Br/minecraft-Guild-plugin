package com.guildplugin.ManageDB;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLPlayerManager {

    public static void onCreate() {

        LiteSQL.onUpdate(
                "CREATE TABLE IF NOT EXISTS playerData(id TEXT NOT NULL PRIMARY KEY, name TEXT,groupId INTEGER, guildId INTEGER, kills INTEGER)");

    }

    public static void addPlayer(String name, String uuid) {
        LiteSQL.onUpdate("INSERT OR REPLACE INTO playerData(id,name,groupId, guildId, kills) " + "VALUES('" + uuid
                + "','" + name + "', (SELECT groupId FROM playerData WHERE id = '" + uuid + "'),"
                + "(SELECT guildId FROM playerData WHERE id = '" + uuid + "'),"
                + "(SELECT kills FROM playerData WHERE id = '" + uuid + "'))");
    }

    public static Integer getGroupId(String playerId) {
        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT groupId FROM playerData WHERE id = '" + playerId + "'");

        try {
            return set.getInt("groupId");
        } catch (SQLException e) {

            return null;
        }

    }

    public static void setGroupId(String playerId, int groupId) {
        LiteSQL.onUpdate("UPDATE playerData SET groupId = "+groupId+" WHERE id ='"+playerId+"'");
    }

    public static void removeGroupId(String playerId) {
        LiteSQL.onUpdate("UPDATE playerData SET groupId = NULL WHERE id ='"+playerId+"'");
    }

    public static boolean isInGroup(String playerId) {
        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT groupId FROM playerData WHERE id ='" + playerId + "'");

        try {
            if (set.getString("groupId") != null) {
                return true;
            }
            else{
                return false;  
            }
        } catch (Exception e) {
            return false;
        }
    }

}
