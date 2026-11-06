package com.pvp_utils.client.modules.impl.Tool.FoodInfo;

import net.minecraft.resources.ResourceLocation;

public final class FoodInfoTextures {
    public static final ResourceLocation MOD_ICONS = ResourceLocation.fromNamespaceAndPath("pvp_utils", "textures/food_info/icons.png");
    public static final ResourceLocation HUNGER_OUTLINE_SPRITE = ResourceLocation.fromNamespaceAndPath("pvp_utils", "tooltip_hunger_outline");

    public static final ResourceLocation FOOD_EMPTY_HUNGER = ResourceLocation.withDefaultNamespace("hud/food_empty_hunger");
    public static final ResourceLocation FOOD_HALF_HUNGER = ResourceLocation.withDefaultNamespace("hud/food_half_hunger");
    public static final ResourceLocation FOOD_FULL_HUNGER = ResourceLocation.withDefaultNamespace("hud/food_full_hunger");
    public static final ResourceLocation FOOD_EMPTY = ResourceLocation.withDefaultNamespace("hud/food_empty");
    public static final ResourceLocation FOOD_HALF = ResourceLocation.withDefaultNamespace("hud/food_half");
    public static final ResourceLocation FOOD_FULL = ResourceLocation.withDefaultNamespace("hud/food_full");

    public static final ResourceLocation HEART_CONTAINER = ResourceLocation.withDefaultNamespace("hud/heart/container");
    public static final ResourceLocation HEART_HARDCORE_CONTAINER = ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore");
    public static final ResourceLocation HEART_FULL = ResourceLocation.withDefaultNamespace("hud/heart/full");
    public static final ResourceLocation HEART_HARDCORE_FULL = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full");
    public static final ResourceLocation HEART_HALF = ResourceLocation.withDefaultNamespace("hud/heart/half");
    public static final ResourceLocation HEART_HARDCORE_HALF = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half");

    private FoodInfoTextures() {
    }

    public enum FoodType {
        EMPTY,
        HALF,
        FULL
    }

    public enum HeartType {
        CONTAINER,
        FULL,
        HALF
    }

    public static ResourceLocation food(boolean rotten, FoodType type) {
        return switch (type) {
            case EMPTY -> rotten ? FOOD_EMPTY_HUNGER : FOOD_EMPTY;
            case HALF -> rotten ? FOOD_HALF_HUNGER : FOOD_HALF;
            case FULL -> rotten ? FOOD_FULL_HUNGER : FOOD_FULL;
        };
    }

    public static ResourceLocation heart(boolean hardcore, HeartType type) {
        return switch (type) {
            case CONTAINER -> hardcore ? HEART_HARDCORE_CONTAINER : HEART_CONTAINER;
            case FULL -> hardcore ? HEART_HARDCORE_FULL : HEART_FULL;
            case HALF -> hardcore ? HEART_HARDCORE_HALF : HEART_HALF;
        };
    }
}
