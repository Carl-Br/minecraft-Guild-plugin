package com.guildplugin.Listener;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import net.md_5.bungee.api.ChatColor;

public class PlotCreateListener implements Listener {

    int minSideLength = 5;
    int maxSideLength = 200;
    int minHight = 4;
    int maxHight = 200;

    Block rightCorner = null;
    Block leftCorner = null;
    Block rightCornerbehindt = null;
    Block leftCornerbehindt = null;
    int hight;

    // creates group plots
    @EventHandler
    public void SignChangeEvent(SignChangeEvent event) {
        event.getPlayer().sendMessage(
                "A " + ChatColor.YELLOW + event.getBlock().getType().name() + ChatColor.WHITE + " got placed");

        String firstLine = event.getLines()[0];

        event.getPlayer().sendMessage("The first Line of your sign is : " + ChatColor.DARK_PURPLE + firstLine);

        if (firstLine.toLowerCase().contains("create plot")) {
            replaceWithMoosStone(event);

            if (rightCornerbehindt.getX() != leftCornerbehindt.getX()
                    && rightCornerbehindt.getZ() != leftCornerbehindt.getZ()) {// die hinteren Blöcke sind nicht
                                                                               // nebeneinander
                event.getPlayer().sendMessage(ChatColor.RED + "[Failed] The ground is not a square!");
                return;
            }

            if (getDistance(rightCorner, leftCorner) > maxSideLength
                    || getDistance(rightCorner, leftCorner) < minSideLength
                    || getDistance(rightCorner, rightCornerbehindt) > maxSideLength
                    || getDistance(rightCorner, rightCornerbehindt) < minSideLength
                    || getDistance(rightCornerbehindt, leftCornerbehindt) > maxSideLength
                    || getDistance(rightCornerbehindt, leftCornerbehindt) < minSideLength || hight > maxHight
                    || hight < minHight) {

                event.getPlayer()
                        .sendMessage(ChatColor.RED + "[Failed] minimum side length : " + minSideLength
                                + "\nmaximum side length : " + maxSideLength + "\nminimum hight : " + minHight
                                + "\nmaximum hight : " + maxHight);
                return;
            }
        }
    }

    private void replaceWithMoosStone(SignChangeEvent event) {

        World world = event.getBlock().getWorld();
        if (world.getName().endsWith("_nether") || world.getName().endsWith("_end")) {
            event.getPlayer().sendMessage(
                    "You can't create a plot in the nether or in the end! Since these worlds get dompletly restted");
            return;
        }

        Block blockBehindSign = getBlockBehind(event.getBlock());

        if (blockBehindSign == null) {
            event.getPlayer().sendMessage(ChatColor.RED + "Please put the sign on a fence");
            return;
        }

        boolean onXCoordinate = false; // // fence is parallel to x axis
        boolean inPositiveDirection = true; //

        if (event.getBlock().getX() + 1 == blockBehindSign.getX()) {// x hat sich verändert
            onXCoordinate = true;
            inPositiveDirection = true;
        } else if (event.getBlock().getX() - 1 == blockBehindSign.getX()) {
            onXCoordinate = true;
            inPositiveDirection = false;
        } else if (event.getBlock().getZ() + 1 == blockBehindSign.getZ()) {
            onXCoordinate = false;
            inPositiveDirection = true;
        } else if (event.getBlock().getZ() - 1 == blockBehindSign.getZ()) {
            onXCoordinate = false;
            inPositiveDirection = false;
        }
        event.getPlayer().sendMessage("onXCoordinate :" + onXCoordinate);
        event.getPlayer().sendMessage("inPositiveDirection :" + inPositiveDirection);

        // get right corner

        if (!onXCoordinate) {
            int i = 1;
            while (world.getBlockAt(blockBehindSign.getX() + i, blockBehindSign.getY(), blockBehindSign.getZ())
                    .getType().name().contains("FENCE")) {
                if (inPositiveDirection) {
                    i--;
                } else {
                    i++;
                }

            }
            if (inPositiveDirection) {
                rightCorner = world.getBlockAt(blockBehindSign.getX() + i + 1, blockBehindSign.getY(),
                        blockBehindSign.getZ());
            } else {
                rightCorner = world.getBlockAt(blockBehindSign.getX() + i - 1, blockBehindSign.getY(),
                        blockBehindSign.getZ());
            }

        } else {
            int i = 1;
            while (world.getBlockAt(blockBehindSign.getX(), blockBehindSign.getY(), blockBehindSign.getZ() + i)
                    .getType().name().contains("FENCE")) {
                if (inPositiveDirection) {
                    i++;
                } else {
                    i--;
                }

            }
            if (inPositiveDirection) {
                rightCorner = world.getBlockAt(blockBehindSign.getX(), blockBehindSign.getY(),
                        blockBehindSign.getZ() + i - 1);
            } else {
                rightCorner = world.getBlockAt(blockBehindSign.getX(), blockBehindSign.getY(),
                        blockBehindSign.getZ() + i + 1);
            }
        }

        // get left corner

        if (!onXCoordinate) {
            int i = 1;
            while (world.getBlockAt(blockBehindSign.getX() + i, blockBehindSign.getY(), blockBehindSign.getZ())
                    .getType().name().contains("FENCE")) {
                if (inPositiveDirection) {
                    i++;
                } else {
                    i--;
                }

            }
            if (inPositiveDirection) {
                leftCorner = world.getBlockAt(blockBehindSign.getX() + i - 1, blockBehindSign.getY(),
                        blockBehindSign.getZ());
            } else {
                leftCorner = world.getBlockAt(blockBehindSign.getX() + i + 1, blockBehindSign.getY(),
                        blockBehindSign.getZ());
            }

        } else {
            int i = 1;
            while (world.getBlockAt(blockBehindSign.getX(), blockBehindSign.getY(), blockBehindSign.getZ() + i)
                    .getType().name().contains("FENCE")) {
                if (inPositiveDirection) {
                    i--;
                } else {
                    i++;
                }

            }
            if (inPositiveDirection) {
                leftCorner = world.getBlockAt(blockBehindSign.getX(), blockBehindSign.getY(),
                        blockBehindSign.getZ() + i + 1);
            } else {
                leftCorner = world.getBlockAt(blockBehindSign.getX(), blockBehindSign.getY(),
                        blockBehindSign.getZ() + i - 1);
            }
        }

        // get right corner behindt

        if (!onXCoordinate) {
            int i;
            if (inPositiveDirection) {
                i = 1;
            } else {
                i = -1;
            }
            while (world.getBlockAt(rightCorner.getX(), rightCorner.getY(), rightCorner.getZ() + i).getType().name()
                    .contains("FENCE")) {
                if (inPositiveDirection) {
                    i++;
                } else {
                    i--;
                }

            }
            if (inPositiveDirection) {
                rightCornerbehindt = world.getBlockAt(rightCorner.getX(), rightCorner.getY(),
                        rightCorner.getZ() + i - 1);
            } else {
                rightCornerbehindt = world.getBlockAt(rightCorner.getX(), rightCorner.getY(),
                        rightCorner.getZ() + i + 1);
            }

        } else {
            int i;
            if (inPositiveDirection) {
                i = 1;
            } else {
                i = -1;
            }
            while (world.getBlockAt(rightCorner.getX() + i, rightCorner.getY(), rightCorner.getZ()).getType().name()
                    .contains("FENCE")) {
                if (inPositiveDirection) {
                    i++;
                } else {
                    i--;
                }

            }
            if (inPositiveDirection) {
                rightCornerbehindt = world.getBlockAt(rightCorner.getX() + i - 1, rightCorner.getY(),
                        rightCorner.getZ());
            } else {
                rightCornerbehindt = world.getBlockAt(rightCorner.getX() + i + 1, rightCorner.getY(),
                        rightCorner.getZ());
            }
        }

        // get left corner behindt

        if (!onXCoordinate) {
            int i;
            if (inPositiveDirection) {
                i = 1;
            } else {
                i = -1;
            }
            while (world.getBlockAt(leftCorner.getX(), leftCorner.getY(), leftCorner.getZ() + i).getType().name()
                    .contains("FENCE")) {
                if (inPositiveDirection) {
                    i++;
                } else {
                    i--;
                }

            }
            if (inPositiveDirection) {
                leftCornerbehindt = world.getBlockAt(leftCorner.getX(), leftCorner.getY(), leftCorner.getZ() + i - 1);
            } else {
                leftCornerbehindt = world.getBlockAt(leftCorner.getX(), leftCorner.getY(), leftCorner.getZ() + i + 1);
            }

        } else {
            int i;
            if (inPositiveDirection) {
                i = 1;
            } else {
                i = -1;
            }
            while (world.getBlockAt(leftCorner.getX() + i, leftCorner.getY(), leftCorner.getZ()).getType().name()
                    .contains("FENCE")) {
                if (inPositiveDirection) {
                    i++;
                } else {
                    i--;
                }

            }
            if (inPositiveDirection) {
                leftCornerbehindt = world.getBlockAt(leftCorner.getX() + i - 1, leftCorner.getY(), leftCorner.getZ());
            } else {
                leftCornerbehindt = world.getBlockAt(leftCorner.getX() + i + 1, leftCorner.getY(), leftCorner.getZ());
            }
        }

        // get the height

        int i = 0;
        while (world.getBlockAt(blockBehindSign.getX(), blockBehindSign.getY() + i, blockBehindSign.getZ()).getType()
                .name().contains("FENCE")) {

            i++;
        }
        hight = i;
    }

    private int getDistance(Block blockA, Block blockB) {// they have to be on the same Z or X axis
        if (blockA.getX() == blockB.getX()) {// Both blocks are on the X axis, so get the difference betweeen the Z axis
            if (blockA.getZ() > blockB.getZ()) {// always a positive int as return
                return (blockA.getZ() - blockB.getZ()) + 1;
            } else {
                return (blockB.getZ() - blockA.getZ()) + 1;
            }
        } else {// Both blocks are on the Z axis, so get the difference betweeen the X axis
            if (blockA.getX() > blockB.getX()) {// always a positive int as return
                return (blockA.getX() - blockB.getX()) + 1;
            } else {
                return (blockB.getX() - blockA.getX()) + 1;
            }
        }
    }

    private Block getBlockBehind(Block block) {

        if (block != null) {
            BlockData data = block.getBlockData();
            if (data instanceof Directional) {
                Directional directional = (Directional) data;
                Block blockBehind = block.getRelative(directional.getFacing().getOppositeFace());
                return blockBehind;
            }
            return null;
        }
        return null;
    }
}
