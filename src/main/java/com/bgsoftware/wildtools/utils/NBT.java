package com.bgsoftware.wildtools.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class NBT {

    private ItemStack item;

    public NBT(ItemStack item) {
        this.item = item;
    }

    public Object getNBTTags() {
        Object itemstack = Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        Object tag = Ref.invoke(itemstack,"getTag");

        if(tag != null) { return tag; }

        try { return Ref.getClass("net.minecraft.server."+Ref.version()+".NBTTagCompound").newInstance(); }
        catch (IllegalAccessException | InstantiationException e) { System.out.println("Error while getting nbtTag of " + item.getType().name() + ":"); e.printStackTrace(); return null;}
    }

    public boolean hasNBTTags() {
        Object itemstack = Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        return (boolean)Ref.invoke(itemstack,"hasTag");
    }

    public void setString(String tagName, String tagValue) { set(tagName, tagValue, String.class, "String"); }
    public void setInt(String tagName, int tagValue) { set(tagName, tagValue, int.class, "Int"); }
    public void setBoolean(String tagName, boolean tagValue) { set(tagName, tagValue, boolean.class, "Boolean"); }
    public void setDouble(String tagName, double tagValue) { set(tagName, tagValue, double.class, "Double"); }

    private void set(String tagName, Object tagValue, Class type, String n) {
        Object itemstack = Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class), item);
        Object nbtTags = getNBTTags();

        Ref.invoke(nbtTags, Ref.method(Ref.nms("NBTTagCompound"), "set" + n, String.class, type), tagName, tagValue);
        Ref.invoke(itemstack, Ref.method(Ref.nms("ItemStack"), "setTag", Ref.nms("NBTTagCompound")), nbtTags);
        item.setItemMeta((ItemMeta) Ref.invokeNulled(Ref.method(Ref.craft("inventory.CraftItemStack"), "getItemMeta", Ref.nms("ItemStack")), itemstack));
    }

    public String getString(String tagName) { return get(tagName, "String") + ""; }
    public int getInt(String tagName) { return (int)get(tagName, "Int"); }
    public boolean getBoolean(String tagName) { return (boolean)get(tagName, "Boolean"); }
    public double getDouble(String tagName) { return (double)get(tagName, "Double"); }

    private Object get(String tagName, String n) {
        return Ref.invoke(getNBTTags(), Ref.method(Ref.nms("NBTTagCompound"), "get" + n, String.class), tagName);
    }

}