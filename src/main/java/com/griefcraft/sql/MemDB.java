package com.griefcraft.sql;

import com.griefcraft.model.Action;
import com.griefcraft.model.Protection;
import com.griefcraft.util.Performance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MemDB extends Database {

    public MemDB() {
        super();
    }

    public MemDB(Type currentType) {
        super(currentType);
    }

    @Override
    protected void postPrepare() {
        Performance.addMemDBQuery();
    }

    public Action getAction(String action, String player) {
        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "actions WHERE player = ? AND action = ?");
            statement.setString(1, player);
            statement.setString(2, action);

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                final int id = set.getInt("id");
                final String actionString = set.getString("action");
                final String playerString = set.getString("player");
                final int chestID = set.getInt("chest");
                final String data = set.getString("data");

                final Action act = new Action();
                act.setID(id);
                act.setAction(actionString);
                act.setPlayer(playerString);
                act.setChestID(chestID);
                act.setData(data);

                return act;
            }


        } catch (final Exception e) {
            printException(e);
        }

        return null;
    }

    /**
     * Get the chest ID associated with a player's unlock request
     *
     * @param player the player to lookup
     * @return the chest ID
     */
    public int getActionID(String action, String player) {
        try {
            int chestID = -1;

            PreparedStatement statement = prepare("SELECT chest FROM " + prefix + "actions WHERE action = ? AND player = ?");
            statement.setString(1, action);
            statement.setString(2, player);

            final ResultSet set = statement.executeQuery();

            while (set.next()) {
                chestID = set.getInt("chest");
            }


            return chestID;
        } catch (final Exception e) {
            printException(e);
        }

        return -1;
    }

    /**
     * Get all the active actions for a player
     *
     * @param player the player to get actions for
     * @return the List<String> of actions
     */
    public List<String> getActions(String player) {
        final List<String> actions = new ArrayList<String>();

        try {
            PreparedStatement statement = prepare("SELECT action FROM " + prefix + "actions WHERE player = ?");
            statement.setString(1, player);

            final ResultSet set = statement.executeQuery();

            while (set.next()) {
                final String action = set.getString("action");

                actions.add(action);
            }

        } catch (final Exception e) {
            printException(e);
        }

        return actions;
    }

    /**
     * @return the path where the database file should be saved
     */
    @Override
    public String getDatabasePath() {
        // if we're using mysql, just open another connection
        if (currentType == Type.MySQL) {
            return super.getDatabasePath();
        }

        return ":memory:";
    }

    /**
     * Get the password submitted for a pending chest lock
     *
     * @param player the player to lookup
     * @return the password for the pending lock
     */
    public String getLockPassword(String player) {
        try {
            String password = "";

            PreparedStatement statement = prepare("SELECT password FROM " + prefix + "locks WHERE player = ?");
            statement.setString(1, player);

            final ResultSet set = statement.executeQuery();

            while (set.next()) {
                password = set.getString("password");
            }


            return password;
        } catch (final Exception e) {
            printException(e);
        }

        return null;
    }

    /**
     * Get the mode data for a player's mode
     *
     * @param player
     * @param mode
     * @return
     */
    public String getModeData(String player, String mode) {
        String ret = null;
        try {
            PreparedStatement statement = prepare("SELECT data FROM " + prefix + "modes WHERE player = ? AND mode = ?");
            statement.setString(1, player);
            statement.setString(2, mode);

            final ResultSet set = statement.executeQuery();
            if (set.next()) {
                ret = set.getString("data");
            }


        } catch (final Exception e) {
            printException(e);
        }
        return ret;
    }

    /**
     * Get the modes a player has activated
     *
     * @param player the player to get
     * @return the List of modes the player is using
     */
    public List<String> getModes(String player) {
        final List<String> modes = new ArrayList<String>();

        try {
            PreparedStatement statement = prepare("SELECT * FROM " + prefix + "modes WHERE player = ?");
            statement.setString(1, player);

            final ResultSet set = statement.executeQuery();

            while (set.next()) {
                final String mode = set.getString("mode");

                modes.add(mode);
            }


        } catch (final Exception e) {
            printException(e);
        }

        return modes;
    }

    /**
     * Get all of the users "logged in" to a chest
     *
     * @param chestID the chest ID to look at
     * @return
     */
    public List<String> getSessionUsers(int chestID) {
        final List<String> sessionUsers = new ArrayList<String>();

        try {
            PreparedStatement statement = prepare("SELECT player FROM " + prefix + "sessions WHERE chest = ?");
            statement.setInt(1, chestID);

            final ResultSet set = statement.executeQuery();

            while (set.next()) {
                final String player = set.getString("player");

                sessionUsers.add(player);
            }

        } catch (final Exception e) {
            printException(e);
        }

        return sessionUsers;
    }

    /**
     * Get the chest ID associated with a player's unlock request
     *
     * @param player the player to lookup
     * @return the chest ID
     */
    public int getUnlockID(String player) {
        return getActionID("unlock", player);
    }

    /**
     * Check if a player has an active chest session
     *
     * @param player  the player to check
     * @param chestID the chest ID to check
     * @return true if the player has access
     */
    public boolean hasAccess(String player, int chestID) {
        try {
            PreparedStatement statement = prepare("SELECT player FROM " + prefix + "sessions WHERE chest = ?");
            statement.setInt(1, chestID);

            final ResultSet set = statement.executeQuery();

            while (set.next()) {
                final String player2 = set.getString("player");

                if (player.equals(player2)) {


                    return true;
                }
            }


        } catch (final Exception e) {
            printException(e);
        }

        return false;
    }

    /**
     * Check if a player has an active chest session
     *
     * @param player the player to check
     * @param chest  the chest to check
     * @return true if the player has access
     */
    public boolean hasAccess(String player, Protection chest) {
        return chest == null || hasAccess(player, chest.getId());

    }

    /**
     * Return if a player has the mode
     *
     * @param player the player to check
     * @param mode   the mode to check
     */
    public boolean hasMode(String player, String mode) {
        List<String> modes = getModes(player);

        return modes.size() > 0 && modes.contains(mode);
    }

    /**
     * Check if a player has a pending action
     *
     * @param player the player to check
     * @param action the action to check
     * @return true if they have a record
     */
    public boolean hasPendingAction(String action, String player) {
        return getAction(action, player) != null;
    }

    /**
     * Check if a player has a pending chest request
     *
     * @param player The player to check
     * @return true if the player has a pending chest request
     */
    public boolean hasPendingChest(String player) {
        try {
            PreparedStatement statement = prepare("SELECT id FROM " + prefix + "locks WHERE player = ?");
            statement.setString(1, player);

            ResultSet set = statement.executeQuery();

            if (set.next()) {
                set.close();
                return true;
            }

            set.close();
        } catch (final Exception e) {
            printException(e);
        }

        return false;
    }

    /**
     * Check if a player has a pending unlock request
     *
     * @param player the player to check
     * @return true if the player has a pending unlock request
     */
    public boolean hasPendingUnlock(String player) {
        return getUnlockID(player) != -1;
    }

    /**
     * create the in-memory table which hold sessions, users that have activated a chest. Not needed past a restart, so no need for extra disk i/o
     */
    @Override
    public void load() {
        if (loaded) {
            return;
        }

        try {
            // reusable column
            Column column;

            Table sessions = new Table(this, "sessions");
            sessions.setMemory(true);

            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                sessions.add(column);

                column = new Column("player");
                column.setType("VARCHAR(255)");
                sessions.add(column);

                column = new Column("chest");
                column.setType("INTEGER");
                sessions.add(column);
            }

            Table locks = new Table(this, "locks");
            locks.setMemory(true);

            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                locks.add(column);

                column = new Column("player");
                column.setType("VARCHAR(255)");
                locks.add(column);

                column = new Column("password");
                column.setType("VARCHAR(100)");
                locks.add(column);
            }

            Table actions = new Table(this, "actions");
            actions.setMemory(true);

            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                actions.add(column);

                column = new Column("action");
                column.setType("VARCHAR(255)");
                actions.add(column);

                column = new Column("player");
                column.setType("VARCHAR(255)");
                actions.add(column);

                column = new Column("chest");
                column.setType("INTEGER");
                actions.add(column);

                column = new Column("data");
                column.setType("VARCHAR(255)");
                actions.add(column);
            }

            Table modes = new Table(this, "modes");
            modes.setMemory(true);

            {
                column = new Column("id");
                column.setType("INTEGER");
                column.setPrimary(true);
                modes.add(column);

                column = new Column("player");
                column.setType("VARCHAR(255)");
                modes.add(column);

                column = new Column("mode");
                column.setType("VARCHAR(255)");
                modes.add(column);

                column = new Column("data");
                column.setType("VARCHAR(255)");
                modes.add(column);
            }

            // now create all of the tables
            sessions.execute();
            locks.execute();
            actions.execute();
            modes.execute();
        } catch (final Exception e) {
            printException(e);
        }

        loaded = true;
    }

    /**
     * @return the number of pending chest locks
     */
    public int pendingCount() {
        int count = 0;

        try {
            Statement statement = connection.createStatement();
            final ResultSet set = statement.executeQuery("SELECT id FROM " + prefix + "locks");

            while (set.next()) {
                count++;
            }

            statement.close();

        } catch (final Exception e) {
            printException(e);
        }

        return count;
    }

    /**
     * Register a pending chest unlock, for when the player does /unlock <pass>
     *
     * @param action
     * @param player
     */
    public void registerAction(String action, String player) {
        try {
            /*
                * We only want 1 action per player, no matter what!
                */
            unregisterAction(action, player);

            PreparedStatement statement = prepare("INSERT INTO " + prefix + "actions (action, player, chest) VALUES (?, ?, ?)");
            statement.setString(1, action);
            statement.setString(2, player);
            statement.setInt(3, -1);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Register a pending chest unlock, for when the player does /unlock <pass>
     *
     * @param player  the player to register
     * @param chestID the chestID to unlock
     */
    public void registerAction(String action, String player, int chestID) {
        try {
            /*
                * We only want 1 action per player, no matter what!
                */
            unregisterAction(action, player);

            PreparedStatement statement = prepare("INSERT INTO " + prefix + "actions (action, player, chest) VALUES (?, ?, ?)");
            statement.setString(1, action);
            statement.setString(2, player);
            statement.setInt(3, chestID);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Register an action, used for various actions (stating the obvious here)
     *
     * @param player the player to register
     * @param data   data
     */
    public void registerAction(String action, String player, String data) {
        try {
            /*
                * We only want 1 action per player, no matter what!
                */
            unregisterAction(action, player);

            PreparedStatement statement = prepare("INSERT INTO " + prefix + "actions (action, player, data) VALUES (?, ?, ?)");
            statement.setString(1, action);
            statement.setString(2, player);
            statement.setString(3, data);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Register a mode to a player (temporary)
     *
     * @param player the player to register the mode to
     * @param mode   the mode to register
     */
    public void registerMode(String player, String mode) {
        try {
            PreparedStatement statement = prepare("INSERT INTO " + prefix + "modes (player, mode) VALUES (?, ?)");
            statement.setString(1, player);
            statement.setString(2, mode);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Register a mode with data to a player (temporary)
     *
     * @param player the player to register the mode to
     * @param mode   the mode to register
     * @param data   additional data
     */
    public void registerMode(String player, String mode, String data) {
        try {
            PreparedStatement statement = prepare("INSERT INTO " + prefix + "modes (player, mode, data) VALUES (?, ?, ?)");
            statement.setString(1, player);
            statement.setString(2, mode);
            statement.setString(3, data);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Register a pending lock request to a player
     *
     * @param player   the player to assign the chest to
     * @param password the password to register with
     */
    public void registerPendingLock(String player, String password) {
        try {
            PreparedStatement statement = prepare("INSERT INTO " + prefix + "locks (player, password) VALUES (?, ?)");
            statement.setString(1, player);
            statement.setString(2, password);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Add a player to be allowed to access a chest
     *
     * @param player  the player to add
     * @param chestID the chest ID to allow them to access
     */
    public void registerPlayer(String player, int chestID) {
        try {
            PreparedStatement statement = prepare("INSERT INTO " + prefix + "sessions (player, chest) VALUES(?, ?)");
            statement.setString(1, player);
            statement.setInt(2, chestID);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Register a pending chest unlock, for when the player does /unlock <pass>
     *
     * @param player  the player to register
     * @param chestID the chestID to unlock
     */
    public void registerUnlock(String player, int chestID) {
        registerAction("unlock", player, chestID);
    }

    /**
     * @return the number of active session
     */
    public int sessionCount() {
        int count = 0;

        try {
            Statement statement = connection.createStatement();
            final ResultSet set = statement.executeQuery("SELECT id FROM " + prefix + "sessions");

            while (set.next()) {
                count++;
            }

            statement.close();

        } catch (final Exception e) {
            printException(e);
        }

        return count;
    }

    /**
     * Unregister a pending chest unlock
     *
     * @param player the player to unregister
     */
    public void unregisterAction(String action, String player) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "actions WHERE action = ? AND player = ?");
            statement.setString(1, action);
            statement.setString(2, player);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Unregister all of the actions for a player
     *
     * @param player the player to unregister
     */
    public void unregisterAllActions(String player) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "actions WHERE player = ?");
            statement.setString(1, player);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Remove all the pending chest requests
     */
    public void unregisterAllChests() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM " + prefix + "locks");

            statement.close();

        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Unregister all of the modes FROM " + prefix + "a player
     *
     * @param player the player to unregister all modes from
     */
    public void unregisterAllModes(String player) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "modes WHERE player = ?");
            statement.setString(1, player);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Unregister a mode FROM " + prefix + "a player
     *
     * @param player the player to register the mode to
     * @param mode   the mode to unregister
     */
    public void unregisterMode(String player, String mode) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "modes WHERE player = ? AND mode = ?");
            statement.setString(1, player);
            statement.setString(2, mode);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Remove a pending lock request FROM " + prefix + "a player
     *
     * @param player the player to remove
     */
    public void unregisterPendingLock(String player) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "locks WHERE player = ?");
            statement.setString(1, player);

            statement.executeUpdate();


        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Remove a player FROM " + prefix + "any sessions
     *
     * @param player the player to remove
     */
    public void unregisterPlayer(String player) {
        try {
            PreparedStatement statement = prepare("DELETE FROM " + prefix + "sessions WHERE player = ?");
            statement.setString(1, player);

            statement.executeUpdate();

        } catch (final Exception e) {
            printException(e);
        }
    }

    /**
     * Unregister a pending chest unlock
     *
     * @param player the player to unregister
     */
    public void unregisterUnlock(String player) {
        unregisterAction("unlock", player);
    }

}
