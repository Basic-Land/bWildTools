package com.bgsoftware.wildtools.hooks;

import com.bgsoftware.wildtools.WildToolsPlugin;
import com.bgsoftware.wildtools.api.hooks.PricesProvider;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import cz.devfire.bshop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public final class PricesProvider_bShop implements PricesProvider {

    public PricesProvider_bShop(){
        WildToolsPlugin.log(" - Using bShop as PricesProvider.");
    }

    @Override
    public double getPrice(Player player, ItemStack itemStack) {
        return Shop.getApi().getPrice(itemStack);
    }
}
