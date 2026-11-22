package supersymmetry.common.item.behavior;

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.Nullable;

import supersymmetry.api.guns.attachment.AttachmentSlot;
import supersymmetry.api.guns.attachment.AttachmentStats;

public final class SuSyAttachmentData {

    public static final String NBT_ATTACHMENTS = "SuSyAttachments";

    private SuSyAttachmentData() {}

    public static boolean hasAttachment(ItemStack gun, AttachmentSlot slot) {
        Objects.requireNonNull(slot, "slot");
        NBTTagCompound attachments = getAttachments(gun, false);
        return attachments != null && attachments.hasKey(slot.getSerializedName(), Constants.NBT.TAG_COMPOUND);
    }

    /** null when the slot is empty. */
    @Nullable
    public static ItemStack getAttachment(ItemStack gun, AttachmentSlot slot) {
        Objects.requireNonNull(slot, "slot");
        NBTTagCompound attachments = getAttachments(gun, false);
        if (attachments == null || !attachments.hasKey(slot.getSerializedName(), Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        ItemStack attachment = new ItemStack(attachments.getCompoundTag(slot.getSerializedName()));
        return attachment.isEmpty() ? null : attachment;
    }

    public static void putAttachment(ItemStack gun, AttachmentSlot slot, ItemStack attachment) {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(attachment, "attachment");
        if (attachment.isEmpty()) {
            throw new IllegalArgumentException("attachment must not be empty");
        }
        ItemStack single = attachment.copy();
        single.setCount(1);
        getAttachments(gun, true).setTag(slot.getSerializedName(), single.writeToNBT(new NBTTagCompound()));
    }

    @Nullable
    public static ItemStack removeAttachment(ItemStack gun, AttachmentSlot slot) {
        ItemStack removed = getAttachment(gun, slot);
        if (removed == null) return null;
        NBTTagCompound attachments = getAttachments(gun, false);
        if (attachments != null) attachments.removeTag(slot.getSerializedName());
        return removed;
    }

    // Combined stat changes of every attachment on the gun
    public static AttachmentStats.Modifiers getModifiers(ItemStack gun) {
        return getModifiers(gun, false);
    }

    public static AttachmentStats.Modifiers getModifiers(ItemStack gun, boolean crouching) {
        AttachmentStats.Modifiers modifiers = AttachmentStats.Modifiers.NONE;
        for (AttachmentSlot slot : AttachmentSlot.values()) {
            ItemStack attachment = getAttachment(gun, slot);
            if (attachment == null) continue;
            SuSyAttachmentBehaviour behavior = SuSyAttachmentBehaviour.getAttachmentBehaviour(attachment);
            if (behavior != null) modifiers = modifiers.combined(behavior.getModifiers(crouching));
        }
        return modifiers;
    }

    @Nullable
    private static NBTTagCompound getAttachments(ItemStack gun, boolean create) {
        Objects.requireNonNull(gun, "gun");
        NBTTagCompound tag = gun.getTagCompound();
        if (tag == null) {
            if (!create) return null;
            tag = SuSyGunBehaviour.getOrCreateTag(gun);
        }
        if (!tag.hasKey(NBT_ATTACHMENTS, Constants.NBT.TAG_COMPOUND)) {
            if (!create) return null;
            tag.setTag(NBT_ATTACHMENTS, new NBTTagCompound());
        }
        return tag.getCompoundTag(NBT_ATTACHMENTS);
    }
}
