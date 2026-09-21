package com.enderio.core.common.interfaces;

import net.minecraft.item.ItemStack;

import org.jspecify.annotations.NonNull;

public interface IOverlayRenderAware {

    public void renderItemOverlayIntoGUI(@NonNull ItemStack stack, int xPosition, int yPosition);

}
