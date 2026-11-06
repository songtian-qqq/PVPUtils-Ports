package com.pvp_utils.client.compat;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

/**
 * 1.21.10 compatibility shim.
 * ItemTags.SPEARS and ItemTags.MELEE_WEAPON_ENCHANTABLE were introduced in 1.21.11,
 * so they do not exist on 1.21.10 and earlier.
 */
public final class ItemTagCompat {
    private ItemTagCompat() {
    }

    /** No spear items / spear tag exist before 1.21.11. */
    public static boolean isSpear(ItemStack stack) {
        return false;
    }

    /** Approximates 1.21.11 MELEE_WEAPON_ENCHANTABLE with the sword/axe tags available in 1.21.10. */
    public static boolean isMeleeWeaponEnchantable(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES);
    }
}
