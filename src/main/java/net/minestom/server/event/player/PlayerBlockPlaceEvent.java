package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.*;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;

/**
 * Called when a player tries placing a block.
 */
public class PlayerBlockPlaceEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {

    private final Player player;
    private final Instance instance;
    private Block block;
    private final BlockFace blockFace;
    private final BlockVec blockPosition;
    private final Point cursorPosition;
    private final PlayerHand hand;

    private int consumeBlockAmount;
    private boolean doBlockUpdates;

    private boolean cancelled;

    public PlayerBlockPlaceEvent(Player player, Instance instance, Block block,
                                 BlockFace blockFace, BlockVec blockPosition,
                                 Point cursorPosition, PlayerHand hand) {
        this.player = player;
        this.instance = instance;
        this.block = block;
        this.blockFace = blockFace;
        this.blockPosition = blockPosition;
        this.cursorPosition = cursorPosition;
        this.hand = hand;
        this.consumeBlockAmount = 1;
        this.doBlockUpdates = true;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    /**
     * Gets the block which will be placed.
     *
     * @return the block to place
     */
    @Override
    public Block getBlock() {
        return block;
    }

    /**
     * Changes the block to be placed.
     *
     * @param block the new block
     */
    public void setBlock(Block block) {
        this.block = block;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
    }

    public Point getCursorPosition() {
        return cursorPosition;
    }

    /**
     * Gets the hand with which the player is trying to place.
     *
     * @return the hand used
     */
    public PlayerHand getHand() {
        return hand;
    }

    /**
     * How many blocks (or items) should be consumed.
     *
     * @param amount is the amount of items consumed, 0 for no counsumtion
     */
    public void setConsumeBlockAmount(int amount) {
        if (amount < 0) amount = 0;
        this.consumeBlockAmount = amount;
    }
    /**
     * Should the block be consumed if not cancelled.
     *
     * @param consumeBlock true if the block should be consumer (remove 1 in amount), false otherwise
     */
    @Deprecated
    public void consumeBlock(boolean consumeBlock) {
        this.consumeBlockAmount = consumeBlock ? 1 : 0;
    }

    /**
     * Does any items get consumed when placing.
     * If player is in creative mode no items will be consumed
     * @return true if blocks will be consumed, false otherwise
     */
    public boolean doesConsumeBlock() {
        if (player.getGameMode().equals(GameMode.CREATIVE)) return false;
        return consumeBlockAmount != 0;
    }

    /**
     * @return the amount of items consumed, one is the defautlt here.
     */
    public int getConsumeBlockAmount() {
        return consumeBlockAmount;
    }

    /**
     * Should the place trigger updates (on self and neighbors)
     * @param doBlockUpdates true if this placement should do block updates
     */
    public void setDoBlockUpdates(boolean doBlockUpdates) {
        this.doBlockUpdates = doBlockUpdates;
    }

    /**
     * Should the place trigger updates (on self and neighbors)
     * @return true if this placement should do block updates
     */
    public boolean shouldDoBlockUpdates() {
        return doBlockUpdates;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
