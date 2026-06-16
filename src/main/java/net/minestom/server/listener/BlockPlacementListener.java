package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlockPredicates;
import net.minestom.server.item.component.ItemBlockState;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.DimensionType;

public class BlockPlacementListener {

    public static void listener(ClientPlayerBlockPlacementPacket packet, Player player) {
        final PlayerHand hand = packet.hand();
        final BlockFace face = packet.blockFace();
        final Point position = packet.blockPosition();

        final Instance instance = player.getInstance();
        if (instance == null) return;

        // Prevent outdated/modified client data
        final Chunk chunk = instance.getChunkAt(position);
        // If Client tried to place a block in an unloaded chunk, ignore the request
        if (!ChunkUtils.isLoaded(chunk)) return;

        final ItemStack item = player.getItemInHand(hand);
        final Block block = instance.getBlock(position);
        final Point cursor = new Vec(packet.cursorPositionX(), packet.cursorPositionY(), packet.cursorPositionZ());

        PlayerBlockInteractEvent playerBlockInteractEvent = new PlayerBlockInteractEvent(player, hand, block, position.asBlockVec(), face, cursor);
        EventDispatcher.call(playerBlockInteractEvent);

        if (playerBlockInteractEvent.isCancelled()) {
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }

        if (playerBlockInteractEvent.isBlockingItemUse()) {
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }
        final var handler = block.handler();
        if (handler != null && !handler.onInteract(new BlockHandler.Interaction(block, instance, face, position, cursor, player, hand, item))) {
            player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
            return;
        }
        PlayerUseItemOnBlockEvent playerUseItemOnBlockEvent = new PlayerUseItemOnBlockEvent(player, hand, item, block, position.asBlockVec(), face, cursor);
        EventDispatcher.call(playerUseItemOnBlockEvent);
        player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
        // update inventory so client does not predict item loss if no items are actually lossed
        player.getInventory().update();
    }
}
