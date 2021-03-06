package com.bgsoftware.wildtools.handlers;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.handlers.ProvidersManager;
import com.bgsoftware.wildtools.api.hooks.ClaimsProvider;
import com.bgsoftware.wildtools.api.hooks.ContainerProvider;
import com.bgsoftware.wildtools.hooks.*;
import com.bgsoftware.wildtools.api.hooks.DropsProvider;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;

import com.bgsoftware.wildtools.api.hooks.SellInfo;
import com.google.common.collect.Lists;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ProvidersHandler implements ProvidersManager {

    private static final WildToolsPlugin plugin = WildToolsPlugin.getPlugin();
    private static final SellInfo EMPTY_INFO = new SellInfo(new HashMap<>(), 0.0);

    static String pricesPlugin;

    private boolean isVaultEnabled = false;
    private Economy economy;

    private final List<DropsProvider> dropsProviders = Lists.newArrayList();

    private final List<ContainerProvider> containerProviders = Lists.newArrayList();
    private final ContainerProvider defaultContainer = new ContainerProvider_Default(plugin);

    private final List<ClaimsProvider> claimsProviders = Lists.newArrayList();
    private PricesProvider pricesProvider;
    private FactionsProvider factionsProvider;

    /*
     * Hooks' methods
     */

    public double getPrice(Player player, ItemStack itemStack){
        if(plugin.getToolsManager().getTool(itemStack) != null)
            return -1;

        try {
            return pricesProvider.getPrice(player, itemStack);
        }catch(Exception ex){
            return -1;
        }
    }

    public int getTNTAmountFromBank(Player player){
        return factionsProvider.getTNTAmountFromBank(player);
    }

    public void takeTNTFromBank(Player player, int amount){
        factionsProvider.takeTNTFromBank(player, amount);
    }

    public List<ItemStack> getBlockDrops(Player player, Block block, boolean onlySpawner){
        List<ItemStack> drops = new ArrayList<>();
        dropsProviders.stream().filter(dropsProvider -> onlySpawner == dropsProvider.isSpawnersOnly())
                .forEach(dropsProvider -> drops.addAll(dropsProvider.getBlockDrops(player, block)));
        return drops;
    }

    public boolean isContainer(BlockState blockState){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState))
                return true;
        }

        return defaultContainer.isContainer(blockState);
    }

    public SellInfo sellContainer(BlockState blockState, Inventory inventory, Player player){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState))
                return containerProvider.sellContainer(blockState, inventory, player);
        }

        if(defaultContainer.isContainer(blockState))
            return defaultContainer.sellContainer(blockState, inventory, player);

        return EMPTY_INFO;
    }

    public void removeContainer(BlockState blockState, Inventory inventory, SellInfo sellInfo){
        for(ContainerProvider containerProvider : containerProviders){
            if(containerProvider.isContainer(blockState)) {
                containerProvider.removeContainer(blockState, inventory, sellInfo);
                return;
            }
        }

        if(defaultContainer.isContainer(blockState))
            defaultContainer.removeContainer(blockState, inventory, sellInfo);
    }

    public boolean isInsideClaim(Player player, Location location){
        return claimsProviders.stream().anyMatch(claimsProvider -> claimsProvider.isPlayerClaim(player, location));
    }

    /*
     * Handler' methods
     */

    public void enableVault(){
        isVaultEnabled = true;
        economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
    }

    public void depositPlayer(Player player, double price){
        economy.depositPlayer(player, price);
    }

    public boolean canSellItem(Player player, ItemStack itemStack){
        return isVaultEnabled && getPrice(player, itemStack) > 0;
    }

    public boolean isVaultEnabled(){
        return isVaultEnabled;
    }

    @Override
    public void addContainerProvider(ContainerProvider containerProvider) {
        containerProviders.add(containerProvider);
    }

    @Override
    public void addDropsProvider(DropsProvider dropsProvider) {
        dropsProviders.add(dropsProvider);
    }

    @Override
    public void setPricesProvider(PricesProvider pricesProvider) {
        this.pricesProvider = pricesProvider;
    }

    @Override
    public void addClaimsProvider(ClaimsProvider claimsProvider) {
        claimsProviders.add(claimsProvider);
    }

    public void loadData(){
        if(pricesProvider == null) {
            // Prices Plugin Hookup
            if (pricesPlugin.equalsIgnoreCase("ShopGUIPlus") && Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus"))
                pricesProvider = new PricesProvider_ShopGUIPlus();
            else if (pricesPlugin.equalsIgnoreCase("GUIShop") && Bukkit.getPluginManager().isPluginEnabled("GUIShop"))
                pricesProvider = new PricesProvider_GUIShop();
            else if (pricesPlugin.equalsIgnoreCase("Essentials") && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                try {
                    pricesProvider = new PricesProvider_Essentials();
                } catch (Throwable ex) {
                    pricesProvider = (PricesProvider) getInstance("com.bgsoftware.wildtools.hooks.PricesProvider_EssentialsOld");
                }
            }
            else if (pricesPlugin.equalsIgnoreCase("bShop") && Bukkit.getPluginManager().isPluginEnabled("bShop")) {
                pricesProvider = new PricesProvider_bShop();
            }
            else if (pricesPlugin.equalsIgnoreCase("newtShop") && Bukkit.getPluginManager().isPluginEnabled("newtShop"))
                pricesProvider = new PricesProvider_NewtShop();
            else pricesProvider = new PricesProvider_Default();
        }

        // Factions Hookup
        if(Bukkit.getPluginManager().isPluginEnabled("Factions") &&
                Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("ProSavage"))
            factionsProvider = (FactionsProvider) getInstance("com.bgsoftware.wildtools.hooks.FactionsProvider_SavageFactions");
        else if(Bukkit.getPluginManager().isPluginEnabled("FactionsX") &&
                containsClass("net.prosavage.factionsx.persist.TNTAddonData")){
            factionsProvider = new FactionsProvider_FactionsX();
        }
        else factionsProvider = new FactionsProvider_Default();

        // Drops hookup
        if(Bukkit.getPluginManager().isPluginEnabled("ChunkHoppers")) {
            addDropsProvider(new DropsProvider_ChunkHoppers());
        }

        // Spawners drops
        if(Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
            addDropsProvider(new DropsProvider_WildStacker());
        }
        else if(Bukkit.getPluginManager().isPluginEnabled("SilkSpawners")){
            try{
                de.dustplanet.util.SilkUtil.class.getMethod("getCreatureName", String.class);
                addDropsProvider(new DropsProvider_SilkSpawners());
            }catch(Throwable ex){
                addDropsProvider((DropsProvider) getInstance("com.bgsoftware.wildtools.hooks.DropsProvider_SilkSpawnersOld"));
            }
        }
        else if(Bukkit.getPluginManager().isPluginEnabled("MergedSpawner")) {
            addDropsProvider(new DropsProvider_MergedSpawner());
        }
        else {
            addDropsProvider(new DropsProviders_WildToolsSpawners());
        }

        // Containers hookup
        if(Bukkit.getPluginManager().isPluginEnabled("bWildChests")){
            addContainerProvider(new ContainerProvider_WildChests(plugin));
        }

        if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
            if(Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("drtshock")){
                addClaimsProvider(new ClaimsProvider_FactionsUUID());
            }
            else{
                addClaimsProvider(new ClaimsProvider_MassiveFactions());
            }
        }
        if(Bukkit.getPluginManager().isPluginEnabled("FactionsX")){
            addClaimsProvider(new ClaimsProvider_FactionsX());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")){
            addClaimsProvider(new ClaimsProvider_GriefPrevention());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Towny")){
            addClaimsProvider(new ClaimsProvider_Towny());
        }
        if(Bukkit.getPluginManager().isPluginEnabled("Villages")){
            addClaimsProvider(new ClaimsProvider_Villages());
        }

    }

    public static void reload(){
        plugin.getProviders().loadData();
    }

    private static Object getInstance(String clazz){
        try{
            return Class.forName(clazz).newInstance();
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static boolean containsClass(String path){
        try{
            Class.forName(path);
            return true;
        }catch (Throwable ex){
            return false;
        }
    }

}
