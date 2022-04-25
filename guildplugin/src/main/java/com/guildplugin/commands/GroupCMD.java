package com.guildplugin.commands;

import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Random;

import com.guildplugin.App;
import com.guildplugin.ManageDB.SQLGroupManager;
import com.guildplugin.ManageDB.SQLPlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.ChatColor;

public class GroupCMD implements CommandExecutor {

    @SuppressWarnings("unused")
    private App plugin;

    public GroupCMD(App plugin) {
        this.plugin = plugin;
        plugin.getCommand("group").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {// create join leave//

        // Check if sender is player player
        if (!(sender instanceof Player)) {
            sender.sendMessage("You are not a Player! ;)");
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            help(sender);
            return false;
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("leave")) {
                leave(player, false);
            } else if (args[0].equalsIgnoreCase("help")) {
                help(sender);
            } else if (args[0].equalsIgnoreCase("create")) {

                if (args.length == 1) {
                    player.sendMessage(
                            ChatColor.RED + "Please provide a name: \ngroup create [group name (only letters!)]  ");
                    return false;
                } else if (args.length > 2) {
                    player.sendMessage(ChatColor.RED + "spaces are not allowed!");
                    return false;
                }
                create(player, args[1]);
            } else if (args[0].equalsIgnoreCase("leaveAndDeleteGroup")) {
                leave(player, true);
            } else if (args[0].equalsIgnoreCase("invite")) {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.RED + "Please provide the name on the user you want to invite!");
                } else {
                    invite(player, args[1]);
                }
            } else if (args[0].equals("acceptInvite")) {

                try {
                    int code = Integer.parseInt(args[1]);
                    acceptInvite(player, code);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Invite is invalid!");
                }
            } else if (args[0].equalsIgnoreCase("rename")) {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.RED + "Please add a name to the command");
                } else {
                    rename(player, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("promote")) {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.RED + "Please add the name of the player you want to promote");
                } else {
                    promote(player, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("degrade")) {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.RED + "Please add the name of the player you want to degrade");
                } else {
                    degrade(player, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("kick")) {
                if (args.length == 1) {
                    player.sendMessage(ChatColor.RED + "Please add the name of the player you want to kick");
                } else {
                    kick(player, args[1]);
                }
            }
            else {
                help(sender);
            }

        }

        return true;
    }

    private void create(Player player, String groupName) {

        // if(player is in group){"You need to leave your current group first"}
        if (SQLPlayerManager.isInGroup(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "Please leave your current group first in order to create one!");
            return;
        }

        // check name
        if (!Pattern.matches("[a-zA-Z]+", groupName)) {
            player.sendMessage(ChatColor.RED + "Only letters are allowed!");
            return;
        }

        if (groupName.length() > 20) {
            player.sendMessage(ChatColor.RED + "The group name can't be longer than 20 letters!");
            return;
        }

        if (SQLGroupManager.groupNameExists(groupName)) {
            player.sendMessage(ChatColor.RED + "This group name already exists!");
            return;
        }

        // create group
        int groupId = SQLGroupManager.createGroup(player.getUniqueId().toString(), groupName);
        SQLPlayerManager.setGroupId(player.getUniqueId().toString(), groupId);
        player.sendMessage(ChatColor.GREEN + "Your group has successfully been created!");

    }

    ////////////////////////////////////////////////////////
    // INVITE//
    ////////////////////////////////////////////////////////
    private static HashMap<Integer, Integer> invitesHasMap = new HashMap<Integer, Integer>(); // code groupId
    private static Random rand = new Random();

    private void invite(Player player, String playerToinviteName) {

        String playerId = player.getUniqueId().toString();

        if (!SQLPlayerManager.isInGroup(playerId)) {

            player.sendMessage(ChatColor.RED + "You are not in a group yet!");
            return;
        }

        // Check permission
        if (!SQLGroupManager.isMod(playerId) && !SQLGroupManager.isLeader(playerId)) {
            player.sendMessage(
                    ChatColor.RED + "You must be a mod or the leader of the group in order to invite someone!");
            return;
        }

        Player invitedPlayer = Bukkit.getPlayer(playerToinviteName);// invited player exists
        if (invitedPlayer == null) {
            player.sendMessage(ChatColor.RED + playerToinviteName + " does not exist or is not online!");
            return;
        }
        if (SQLPlayerManager.isInGroup(invitedPlayer.getUniqueId().toString())) {// invited player is in group
            player.sendMessage(ChatColor.RED + playerToinviteName + " is already in a group!");
            return;
        } else {
            player.sendMessage(ChatColor.GREEN + invitedPlayer.getName() + " got invited into your group!");
        }

        int inviteCode = rand.nextInt(1000000000);
        TextComponent message = new TextComponent(ChatColor.GREEN + "[accept]");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group acceptInvite " + inviteCode));
        int groupId = SQLPlayerManager.getGroupId(playerId);
        invitedPlayer
                .sendMessage(ChatColor.AQUA + "You got invited to the group " + SQLGroupManager.getGroupName(groupId));
        invitedPlayer.spigot().sendMessage(message);

        invitesHasMap.put(inviteCode, groupId);

        // Hasmap guildId acceptCode(random)
    }

    private void acceptInvite(Player player, int code) {
        Integer groupId = invitesHasMap.get(code);
        invitesHasMap.remove(code);
        // check if group exists
        if (groupId == null) {
            player.sendMessage(ChatColor.RED + " The invite is invalid!");
            return;
        }
        if (!SQLGroupManager.groupExists(groupId)) {
            player.sendMessage(ChatColor.RED + " This group doesn't exist");
            return;
        }

        // add Member
        player.sendMessage(ChatColor.GREEN + " You joined the group!");
        SQLGroupManager.addMember(player.getUniqueId().toString(), groupId);
        SQLPlayerManager.setGroupId(player.getUniqueId().toString(), groupId);
    }

    private void kick(Player player, String name) {
        Player playerToKick = Bukkit.getPlayer(name);// invited player exists
        if (playerToKick == null) {
            player.sendMessage(ChatColor.RED + name + " does not exist or is not online!");
            return;
        }
        String playerToKickId = playerToKick.getUniqueId().toString();
        String playerId = player.getUniqueId().toString();

        int playerToKickGroupId = SQLPlayerManager.getGroupId(playerToKickId);
        int playerGroupId = SQLPlayerManager.getGroupId(playerId);

        if (playerToKickGroupId != playerGroupId) {
            player.sendMessage(ChatColor.RED + name + " is not in you group!");
            return;
        }

        if (!SQLGroupManager.isMod(playerId) && !SQLGroupManager.isLeader(playerId)) {
            player.sendMessage(ChatColor.RED + name + " You must be a mod or a leader to kick someone!");
            return;
        }

        if (!SQLGroupManager.isMod(playerToKickId) && !SQLGroupManager.isLeader(playerToKickId)) {// if he not a mod or a leader
            // kick him
            SQLPlayerManager.removeGroupId(playerId);  
            SQLGroupManager.removeIdFromMemberIds(playerId);

        } else if (SQLGroupManager.isLeader(playerId)) {
            // kick him
            SQLGroupManager.removeIdFromModIds(playerId);
            SQLGroupManager.removeIdFromMemberIds(playerId);
            SQLPlayerManager.removeGroupId(playerId);
        } else {
            player.sendMessage(ChatColor.RED+"You can't kick players that are one the same rank as you or higher!");
        }

    }

    private void leave(Player player, boolean confirmed) {

        if (!SQLPlayerManager.isInGroup(player.getUniqueId().toString())) {// check if player is in group
            player.sendMessage(ChatColor.RED + " You are not a member of any group.");
            return;
        }

        int groupId = SQLPlayerManager.getGroupId(player.getUniqueId().toString());
        String groupName = SQLGroupManager.getGroupName(groupId);

        // check if he is the leader

        // check if he is the last member
        if (SQLGroupManager.getGroupMemberIds(groupId).length == 1 && !confirmed) {//
            // get confirmation to delete the group
            player.sendMessage(ChatColor.AQUA
                    + "You are the last member of this group. It will be deleted if you leave it. The plot of this group won't exists anymore after the next reload of the map and any o ther players can grief it! Please confirm that you still want to leave the goup.");

            TextComponent message = new TextComponent(ChatColor.GREEN + "[confirm]");

            // this command will call this ,methode again, but "confirmed" will be true
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group leaveAndDeleteGroup"));
            player.spigot().sendMessage(message);
            return;
        }
        if (confirmed) {
            // delete group
            SQLGroupManager.deleteGroup(groupId);
            player.sendMessage(ChatColor.GREEN + "Your group got deleted!");
        } else {
            // remove memberId from member/mod ids
            SQLGroupManager.leaveGroup(player.getUniqueId().toString());
        }

        // update groupId from player
        SQLPlayerManager.removeGroupId(player.getUniqueId().toString());
        player.sendMessage(ChatColor.GREEN + "You left the group " + groupName);
    }

    private void promote(Player player, String PlayerNameToPromote) {

        Player playerToPromote = Bukkit.getPlayer(PlayerNameToPromote);// invited player exists
        if (playerToPromote == null) {
            player.sendMessage(ChatColor.RED + PlayerNameToPromote + " does not exist or is not online!");
            return;
        }

        String playerToPromoteId = playerToPromote.getUniqueId().toString();
        Integer playerGroupId = SQLPlayerManager.getGroupId(player.getUniqueId().toString());// Integer to check if it's
                                                                                             // null
        Integer playerToPromoteGroupId = SQLPlayerManager.getGroupId(playerToPromoteId);// Integer to check if it's null

        // check if both players are in the same group
        if (!SQLPlayerManager.isInGroup(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "You are not a member of any group!");
            return;
        }
        if (!SQLPlayerManager.isInGroup(playerToPromoteId)) {
            player.sendMessage(ChatColor.RED + PlayerNameToPromote + " is not in your group!");
            return;
        } else if (playerToPromoteGroupId != playerGroupId) {
            player.sendMessage(ChatColor.RED + PlayerNameToPromote + " is not in your group!");
            return;
        }

        if (!SQLGroupManager.isLeader(player.getUniqueId().toString())) {

            player.sendMessage(ChatColor.RED + " You must be the leader of the group to promote someone!");
            return;
        }


        if (SQLGroupManager.isMod(playerToPromoteId)) {
            player.sendMessage(ChatColor.RED + PlayerNameToPromote + " is already a mod!");
            return;
        }

        SQLGroupManager.promoteToMod(playerToPromoteId);
        player.sendMessage(ChatColor.GREEN + playerToPromote.getName() + " is now a mod!");
        

    }

    private void degrade(Player player, String PlayerNameToDegrade) {

        Player playerToDegrade = Bukkit.getPlayer(PlayerNameToDegrade);// invited player exists
        if (playerToDegrade == null) {
            player.sendMessage(ChatColor.RED + PlayerNameToDegrade + " does not exist or is not online!");
            return;
        }

        String playerToDegradeId = playerToDegrade.getUniqueId().toString();



        // check if both players are in the same group
        if (!SQLPlayerManager.isInGroup(player.getUniqueId().toString())) {
            player.sendMessage(ChatColor.RED + "You are not a member of any group!");
            return;
        }
        if (!SQLPlayerManager.isInGroup(playerToDegradeId)) {
            player.sendMessage(ChatColor.RED + PlayerNameToDegrade + " is not in your group!");
            return;
        } 

        int playerToDegradeGroupId = SQLPlayerManager.getGroupId(playerToDegradeId);
        int playerGroupId = SQLPlayerManager.getGroupId(player.getUniqueId().toString());
        if (playerToDegradeGroupId != playerGroupId) {
            player.sendMessage(ChatColor.RED + PlayerNameToDegrade + " is not in your group!");
            return;
        }

        if (!SQLGroupManager.isLeader(player.getUniqueId().toString())) {

            player.sendMessage(ChatColor.RED + " You must be the leader of the group to degrade someone!");
            return;
        }



        if (!SQLGroupManager.isMod(playerToDegradeId)) {
            player.sendMessage(ChatColor.RED + PlayerNameToDegrade + " is not a mod!");
            return;
        }

        SQLGroupManager.removeIdFromModIds(playerToDegradeId);
        player.sendMessage(ChatColor.GREEN + PlayerNameToDegrade + " is now not a mod anymore!");
    }

    private void setNewLeader() {

    }

    private void rename(Player player, String newName) {
        String playerId = player.getUniqueId().toString();
        if (!SQLPlayerManager.isInGroup(playerId)) {
            player.sendMessage(ChatColor.RED + " You are not a member of any group.");
            return;
        }

        int groupId = SQLPlayerManager.getGroupId(playerId);
        if (!SQLGroupManager.isLeader(playerId)) {
            player.sendMessage(ChatColor.RED + " You must be the leader to change to group name.");
            return;
        }

        // check name

        // check name
        if (!Pattern.matches("[a-zA-Z]+", newName)) {
            player.sendMessage(ChatColor.RED + "Only letters are allowed!");
            return;
        }

        if (newName.length() > 20) {
            player.sendMessage(ChatColor.RED + "The group name can't be longer than 20 letters!");
            return;
        }

        if (SQLGroupManager.groupNameExists(newName)) {
            player.sendMessage(ChatColor.RED + "This group name already exists!");
            return;
        }

        // set new name

        SQLGroupManager.setNewName(newName, groupId);
        player.sendMessage(ChatColor.GREEN + "The name of your group is now " + newName);

    }

    private void groupInfo() {

    }

    private void help(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW
                + "group commands :\n /group create [group name (only letters!)]  \n/group join [groupName] "
                + "\n/group leave \n/group promote [username]\n/group degrade [username] \n/group leader [username] \n/group rename [new name]");
    }

}
