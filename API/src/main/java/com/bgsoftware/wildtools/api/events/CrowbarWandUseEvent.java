package com.bgsoftware.wildtools.api.events;

import com.bgsoftware.wildtools.api.objects.tools.CrowbarTool;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

@SuppressWarnings("unused")
/**
 * CrowbarWandUseEvent is called when a crowbar wand is used.
 */
public final class CrowbarWandUseEvent extends ToolUseEvent<CrowbarTool> {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Block affectedBlock;

    /**
     * The constructor of the event.
     * @param player The player who used the wand.
     * @param tool The wand that was used.
     * @param affectedBlock The affected block by the wand.
     */
    public CrowbarWandUseEvent(Player player, CrowbarTool tool, Block affectedBlock){
        super(player, tool);
        this.affectedBlock = affectedBlock;
    }

    /**
     * Get the affected block by the wand.
     */
    public Block getBlock() {
        return affectedBlock;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
