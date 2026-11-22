package supersymmetry.common.item.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import lombok.Getter;
import supersymmetry.api.guns.attachment.AttachmentSlot;
import supersymmetry.api.guns.attachment.AttachmentStats;

public class SuSyAttachmentBehaviour implements IItemBehaviour {

    @Getter
    private final AttachmentSlot slot;
    @Getter
    private final AttachmentStats.Modifiers modifiers;
    // only counted while crouching, for mounts that have to be braced (a bipod)
    private final AttachmentStats.Modifiers deployedModifiers;

    public SuSyAttachmentBehaviour(AttachmentSlot slot, AttachmentStats.Modifiers modifiers) {
        this(slot, modifiers, AttachmentStats.Modifiers.NONE);
    }

    public SuSyAttachmentBehaviour(AttachmentSlot slot, AttachmentStats.Modifiers modifiers,
                                   AttachmentStats.Modifiers deployedModifiers) {
        this.slot = Objects.requireNonNull(slot, "slot");
        this.modifiers = Objects.requireNonNull(modifiers, "modifiers");
        this.deployedModifiers = Objects.requireNonNull(deployedModifiers, "deployedModifiers");
    }

    public AttachmentStats.Modifiers getModifiers(boolean crouching) {
        if (!crouching || deployedModifiers.isNone()) {
            return modifiers;
        }
        return modifiers.combined(deployedModifiers);
    }

    /** True when the attachment only work while crouching */
    public boolean hasDeployedModifiers() {
        return !deployedModifiers.isNone();
    }

    @Override
    public void addInformation(ItemStack stack, List<String> lines) {
        lines.add(I18n.format("susy.gun.attachment.tooltip.slot",
                I18n.format("susy.gui.gun_attachments.slot." + slot.getSerializedName())));
        addMultipliers(lines, modifiers);
        List<String> deployed = new ArrayList<>();
        addMultipliers(deployed, deployedModifiers);
        if (!deployed.isEmpty()) {
            lines.add(I18n.format("susy.gun.attachment.tooltip.deployed"));
            lines.addAll(deployed);
        }
    }

    private static void addMultipliers(List<String> lines, AttachmentStats.Modifiers modifiers) {
        addMultiplier(lines, "damage", modifiers.getDamageMultiplier());
        addMultiplier(lines, "spread", modifiers.getSpreadMultiplier());
        addMultiplier(lines, "recoil", modifiers.getRecoilMultiplier());
        addMultiplier(lines, "aim_time", modifiers.getAimTimeMultiplier());
        addMultiplier(lines, "zoom", modifiers.getZoomMultiplier());
    }

    private static void addMultiplier(List<String> lines, String stat, float multiplier) {
        if (multiplier == 1f) {
            return;
        }
        lines.add(I18n.format("susy.gun.attachment.tooltip." + stat, Math.round((multiplier - 1f) * 100f)));
    }

    @Nullable
    public static SuSyAttachmentBehaviour getAttachmentBehaviour(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof MetaItem metaItem)) {
            return null;
        }
        for (Object behaviour : metaItem.getBehaviours(stack)) {
            if (behaviour instanceof SuSyAttachmentBehaviour attachment) {
                return attachment;
            }
        }
        return null;
    }
}
