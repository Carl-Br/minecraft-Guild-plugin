package com.guildplugin.ManageDB;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LiteSQL {

    private static Connection conn;
    private static Statement stmt;

    public static void connect() {

        conn = null;
        try {
            File file = new File("GuildDatabase.db");
            if (!file.exists()) {
                System.out.println("created new database");
                file.createNewFile();
            }
            System.out.println("Databse location : " + file.getAbsolutePath());

            String url = "jdbc:sqlite:" + file.getPath();
            conn = DriverManager.getConnection(url);

            System.out.println("Successfully connected to databse");

            stmt = conn.createStatement();
        } catch (SQLException | IOException e) {

            System.out.println("Could't connect to databse: ");
            e.printStackTrace();
        }

    }

    public static void disconnect() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Successfully disconnected from database");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void onUpdate(String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet onQuery(String sql) {

        try {
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void delete(String sql) {

        try {
            stmt.executeQuery(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block

        }

    }
}
