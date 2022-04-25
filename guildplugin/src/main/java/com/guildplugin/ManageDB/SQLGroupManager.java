package com.guildplugin.ManageDB;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLGroupManager {

    public static void onCreate() {

        LiteSQL.onUpdate(
                "CREATE TABLE IF NOT EXISTS groupData(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT, basePosition TEXT, memberIds TEXT, modIds TEXT, leaderId TEXT, guildId INTEGER)");

    }

    public static boolean groupNameExists(String groupName) {
        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT name  FROM groupData WHERE name  ='" + groupName + "' COLLATE NOCASE");// COLLATE
                                                                                                             // NOCASE
                                                                                                             // => not
                                                                                                             // case
                                                                                                             // sensetive
        try {
            set.getString("name");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int createGroup(String leaderId, String name) {
        name = name.toLowerCase();
        LiteSQL.onUpdate("INSERT INTO groupData (name,memberIds,leaderId) VALUES ('" + name + "','" + leaderId + "','"
                + leaderId + "')");

        // return group id

        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT id  FROM groupData WHERE name  ='" + name + "'");
        try {
            return Integer.parseInt(set.getString("id"));
        } catch (Exception e) {
            // can not happen
            return 0;

        }
    }

    public static void leaveGroup(String playerUuid) {

        int groupId = SQLPlayerManager.getGroupId(playerUuid);

        String[] memberIdsArray = getGroupMemberIds(groupId);
        String[] modIdsArray = getGroupModIds(groupId);// can be null if there are no modIds

        String memberIds = "";
        String modIds = "";

        if (memberIdsArray != null) {
            for (String s : memberIdsArray) {
                if (!s.equals(playerUuid)) {
                    memberIds += s + " ";
                }
            }
            memberIds = memberIds.substring(0, memberIds.length() - 1);// removes the last " "again
        }

        if (modIdsArray != null) {
            for (String s : modIdsArray) {
                if (!s.equals(playerUuid)) {
                    modIds += s + " ";
                }
            }
            modIds = modIds.substring(0, modIds.length() - 1);// removes the last " "again
        }

        // Saves the empty Ids as null and not as " "
        if (memberIds.equals("")) {
            memberIds = "NULL";
        } else {
            memberIds = "'" + memberIds + "'";
        }
        if (modIds.equals("")) {
            modIds = "NULL";
        } else {
            modIds = "'" + modIds + "'";
        }

        LiteSQL.onUpdate("UPDATE groupData SET modIds =" + modIds + " WHERE id = " + groupId);
        LiteSQL.onUpdate("UPDATE groupData SET memberIds =" + memberIds + " WHERE id = " + groupId);
    }

    public static String[] getGroupMemberIds(int groupId) {

        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT memberIds FROM groupData WHERE id  =" + groupId);
        try {
            String memberIds = set.getString("memberIds");
            return memberIds.split(" ");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] getGroupModIds(int groupId) {

        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT modIds FROM groupData WHERE id  =" + groupId);
        try {
            String memberIds = set.getString("modIds");
            return memberIds.split(" ");
        } catch (Exception e) {
            // can not happen
            return null;
        }
    }

    public static String getGroupName(int groupId) {

        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT name FROM groupData WHERE id  =" + groupId);
        try {
            return set.getString("name");
        } catch (Exception e) {
            // can not happen
            return null;
        }
    }

    public static void deleteGroup(int groupId) {
        LiteSQL.onUpdate("DELETE FROM groupData WHERE id = " + groupId);
    }

    public static String getLeaderId(int groupId) {
        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT leaderId FROM groupData WHERE id  =" + groupId);
        try {
            return set.getString("leaderId").replaceAll(" ", "");// replace just to make sure
        } catch (Exception e) {
            // can not happen
            return null;
        }
    }

    public static boolean isMod(String playerId) {
        int groupId = SQLPlayerManager.getGroupId(playerId);

        String[] modIds = getGroupModIds(groupId);

        if (modIds == null) {// Id there are no mods
            return false;
        }

        for (String s : modIds) {
            if (s.equals(playerId)) {
                return true;
            }
        }
        return false;
    }

    public static void promoteToMod(String playerId) {
        int groupId = SQLPlayerManager.getGroupId(playerId);

        String modIds = "";
        String[] modIdsArray = getGroupModIds(groupId);

        if (modIdsArray != null) {
            for (String s : modIdsArray) {
                modIds += s + " ";
            }
        }

        modIds += playerId;
        LiteSQL.onUpdate("UPDATE groupData SET modIds ='" + modIds + "' WHERE id = " + groupId);
    }

    public static void removeIdFromModIds(String playerId) {
        int groupId = SQLPlayerManager.getGroupId(playerId);

        String modIds = "";

        for (String s : getGroupModIds(groupId)) {
            if (!s.equals(playerId)) {
                modIds += s + " ";
            }
        }
        if (modIds.equals("")) {// if the only mod got removed
            modIds = "NULL";
        } else {
            modIds = modIds.substring(0, modIds.length() - 1);// removes the last space
        }
        LiteSQL.onUpdate("UPDATE groupData SET modIds ='" + modIds + "' WHERE id = " + groupId);
    }

    public static void removeIdFromMemberIds(String playerId) {
        int groupId = SQLPlayerManager.getGroupId(playerId);

        String memberIds = "";

        for (String s : getGroupMemberIds(groupId)) {
            if (!s.equals(playerId)) {
                memberIds += s + " ";
            }
        }

        memberIds = memberIds.substring(0, memberIds.length() - 1);// removes the last space

        LiteSQL.onUpdate("UPDATE groupData SET memberIds ='" + memberIds + "' WHERE id = " + groupId);
    }

    public static boolean isLeader(String playerId) {
        Integer groupId = SQLPlayerManager.getGroupId(playerId);
        if (groupId == null) {// if player is even in a group
            return false;
        }

        String leaderId = getLeaderId(groupId);
        if (playerId.equals(leaderId)) {
            return true;
        }
        return false;
    }

    public static boolean groupExists(int groupId) {
        ResultSet set = null;
        set = LiteSQL.onQuery("SELECT name FROM groupData WHERE id  =" + groupId);
        try {
            if (set.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void addMember(String playerid, int groupId) {
        String[] memberIdsArray = getGroupMemberIds(groupId);
        String memberIds = "";
        for (String s : memberIdsArray) {
            memberIds += s + " ";
        }
        memberIds += playerid;

        LiteSQL.onUpdate("UPDATE groupData SET memberIds ='" + memberIds + "' WHERE id = " + groupId);
    }

    public static void setNewName(String newName, int groupId) {
        LiteSQL.onUpdate("UPDATE groupData SET name ='" + newName + "' WHERE id = " + groupId);
    }
}
