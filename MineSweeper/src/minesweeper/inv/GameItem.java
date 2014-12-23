package minesweeper.inv;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public enum GameItem {

    COVER(Material.STONE), FLAG(Material.WOOD), BOMB(Material.TNT), WARNING(Material.STAINED_CLAY), NONE(Material.AIR), WON(Material.DIAMOND);
    
    private Material itemMaterial;
    
    private GameItem(Material itemMaterial) {
        this.itemMaterial = itemMaterial;
    }
    
    public ItemStack getItem() {
        ItemStack item = new ItemStack(itemMaterial);
        return item;
    }
    
    private static byte[] colours = new byte[] {
        DyeColor.LIGHT_BLUE.getData(),
        DyeColor.GREEN.getData(),
        DyeColor.PINK.getData(),
        DyeColor.BLUE.getData(),
        DyeColor.RED.getData(),
        DyeColor.CYAN.getData(),
        DyeColor.PURPLE.getData(),
        DyeColor.BROWN.getData()
    };
    
    public static ItemStack getWarningLevelItem(int warning) {
        ItemStack item = WARNING.getItem();
        item.setData(new MaterialData(item.getType(), colours[warning - 1]));
        item.setAmount(warning);
        return item;
    }
}
