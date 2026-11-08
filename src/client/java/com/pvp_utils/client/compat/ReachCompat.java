package com.pvp_utils.client.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * 1.21.10 compatibility shim for Player#isWithinAttackRange / isWithinEntityInteractionRange /
 * isWithinBlockInteractionRange, which only exist starting with 1.21.11.
 */
public final class ReachCompat {
    private ReachCompat() {
    }

    public static boolean isWithinAttackRange(Player player, AABB box, double margin) {
        return isWithin(player, box, player.entityInteractionRange(), margin);
    }

    public static boolean isWithinEntityInteractionRange(Player player, Entity entity, double margin) {
        if (entity == null || entity.isRemoved()) {
            return false;
        }
        return isWithin(player, entity.getBoundingBox(), player.entityInteractionRange(), margin);
    }

    public static boolean isWithinBlockInteractionRange(Player player, BlockPos pos, double margin) {
        return isWithin(player, new AABB(pos), player.blockInteractionRange(), margin);
    }

    private static boolean isWithin(Player player, AABB box, double range, double margin) {
        double allowed = range + margin;
        return box.distanceToSqr(player.getEyePosition()) < allowed * allowed;
    }
}
