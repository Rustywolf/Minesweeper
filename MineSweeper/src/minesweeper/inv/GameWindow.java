package minesweeper.inv;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public class GameWindow implements Listener {
    
    private static final int GAME_WIDTH = 9;
    private static final int GAME_HEIGHT = 6;
    
    private Inventory inventory;
    private Player player;
    
    private boolean[] mineList = new boolean[GAME_WIDTH * GAME_HEIGHT];
    private GameItem[] gameItems = new GameItem[GAME_WIDTH * GAME_HEIGHT];
    private boolean hasLost = false;
    
    public GameWindow(Plugin plugin, Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player, GAME_WIDTH * GAME_HEIGHT, ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "MineSweeper");
        
        this.setupGame();
        this.setupInventory();
        this.player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGame() {
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            boolean hasSet = false;
            do {
                int position = r.nextInt(GAME_WIDTH * GAME_HEIGHT);
                if (!mineList[position]) {
                    mineList[position] = true;
                    hasSet = true;
                }
            } while (!hasSet);
        }
    }
    
    private void setupInventory() {
        for (int i = 0; i < GAME_WIDTH * GAME_HEIGHT; i++) {
            inventory.setItem(i, GameItem.COVER.getItem());
            gameItems[i] = GameItem.COVER;
        }
    }
    
    private void setItem(int slot, GameItem item) {
        gameItems[slot] = item;
        inventory.setItem(slot, item.getItem());
    }
    
    private void setWarning(int slot) {
        if (mineList[slot]) {
            return;
        }
        
        int nearbyMines = getNearbyMines(slot);
        if (nearbyMines != 0) {
            gameItems[slot] = GameItem.WARNING;
            inventory.setItem(slot, GameItem.getWarningLevelItem(nearbyMines));
        } else {
            clearEmptyArea(slot);
        }
    }
    
    private void clearEmptyArea(int slot) {
        if (mineList[slot]) {
            return;
        }
        
        int nearbyMines = getNearbyMines(slot);
        if (nearbyMines != 0) {
            setWarning(slot);
            return;
        }
        
        setItem(slot, GameItem.NONE);
        
        int column = Math.floorDiv(slot, GAME_WIDTH);
        int row = slot % GAME_WIDTH;
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                int newColumn = (column + x);
                if (newColumn < 0 || newColumn >= GAME_HEIGHT) {
                    continue;
                }
                
                int newRow = (row + y);
                if (newRow < 0 || newRow >= GAME_WIDTH) {
                    continue;
                }
                
                int newSlot = newColumn * GAME_WIDTH + newRow;
                if (!mineList[newSlot] && gameItems[newSlot] == GameItem.COVER) {
                    clearEmptyArea(newSlot);
                }
            }
        }
    }
    
    private int getNearbyMines(int slot) {
        int count = 0;
        int column = Math.floorDiv(slot, GAME_WIDTH);
        int row = slot % GAME_WIDTH;
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                int newColumn = (column + x);
                if (newColumn < 0 || newColumn >= GAME_HEIGHT) {
                    continue;
                }
                
                int newRow = (row + y);
                if (newRow < 0 || newRow >= GAME_WIDTH) {
                    continue;
                }
                
                if(mineList[newColumn * GAME_WIDTH + newRow]) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().equals(inventory.getTitle())) {
            event.setCancelled(true);
        
            if (hasLost) {
                return;
            }
            
            if (event.getSlot() < 0 || event.getSlot() >= gameItems.length) {
                return;
            }
            
            GameItem item = gameItems[event.getSlot()];
            switch (event.getClick()) {
                case LEFT:
                    handleLeftClick(item, event);
                    break;
                    
                case RIGHT:
                    handleRightClick(item, event);
                    break;
            }
            
            checkForWin();
        }
    }
    
    private void handleLeftClick(GameItem item, InventoryClickEvent event) {
        if (item == GameItem.COVER) {
            if (mineList[event.getSlot()]) {
                hasLost = true;
                for (int i = 0; i < GAME_WIDTH * GAME_HEIGHT; i++) {
                    if (mineList[i]) {
                        setItem(i, GameItem.BOMB);
                    }
                }
            } else {
                setWarning(event.getSlot());
            }
        }
    }
    
    private void handleRightClick(GameItem item, InventoryClickEvent event) {
        switch (item) {
            case COVER:
                setItem(event.getSlot(), GameItem.FLAG);
                break;
                
            case FLAG:
                setItem(event.getSlot(), GameItem.COVER);
                break;
        }
    }
    
    private void checkForWin() {
        boolean hasWon = true;
        for (int i = 0; i < GAME_WIDTH * GAME_HEIGHT; i++) {
            if (!mineList[i]) {
                if (gameItems[i] == GameItem.COVER || gameItems[i] == GameItem.FLAG) {
                    hasWon = false;
                    break;
                }
            }
        }
        
        if (hasWon) {
            for (int i= 0; i < GAME_WIDTH * GAME_HEIGHT; i++) {
                if (mineList[i]) {
                    setItem(i, GameItem.WON);
                }
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getTitle().equals(inventory.getTitle())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HandlerList.unregisterAll(this);
        inventory = null;
        player = null;
    }
    
}
