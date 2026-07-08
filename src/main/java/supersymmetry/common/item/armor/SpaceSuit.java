package supersymmetry.common.item.armor;

import static net.minecraft.inventory.EntityEquipmentSlot.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static supersymmetry.api.util.SuSyUtility.susyId;
import static supersymmetry.common.event.DimensionBreathabilityHandler.ABSORB_ALL;
import static supersymmetry.common.event.DimensionBreathabilityHandler.isInHazardousEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL20;

import gregtech.api.damagesources.DamageSources;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.api.items.IGeoMetaArmor;
import supersymmetry.client.renderer.handler.GeoMetaArmorRenderer;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.item.SuSyArmorItem;

public class SpaceSuit extends BreathingApparatus implements IGeoMetaArmor {

    private static final double DEFAULT_ABSORPTION = 0;
    // TODO balancing
    private static final double LEAK_PER_PUNCTURE = 0.3;
    private static final int MAX_VISUAL_PUNCTURES = 15;

    private static float[] getHolePositions(int count) {
        int max = Math.min(count, MAX_VISUAL_PUNCTURES);
        float[] pos = new float[max * 2];
        for (int i = 0; i < max; i++) {
            // TODO replace with dumber rng
            Random rng = new Random(i * 0x9e3779b9 ^ 0x12312312);
            pos[i * 2] = rng.nextFloat();
            pos[i * 2 + 1] = rng.nextFloat();
        }
        return pos;
    }

    private final double hoursOfLife;
    private final String name;
    private final int tier;
    private final double relativeAbsorption;

    private AnimationFactory factory;

    public SpaceSuit(EntityEquipmentSlot slot, int maxDurability, double hoursOfLife, String name, int tier,
                     double relativeAbsorption) {
        super(slot, maxDurability);
        this.hoursOfLife = hoursOfLife;
        this.name = name;
        this.tier = tier;
        this.relativeAbsorption = relativeAbsorption;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return textureRL().toString();
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
                                    EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
        return GeoMetaArmorRenderer.INSTANCE
                .setCurrentItem(entityLiving, itemStack, armorSlot)
                .applyEntityStats(defaultModel)
                .applySlot(armorSlot);
    }

    @Override
    public List<ResourceLocation> getTextureLocations() {
        return Collections.emptyList();
    }

    @Override
    public void registerControllers(AnimationData data) {
        /* Do nothing */
    }

    @Override
    public AnimationFactory getFactory() {
        if (this.factory == null) {
            this.factory = new AnimationFactory(this);
        }
        return this.factory;
    }

    @Override
    public String getGeoName() {
        return name + "_armor";
    }

    // No animation needed
    @Override
    public ResourceLocation animationRL() {
        return susyId("animations/dummy.animation.json");
    }

    @Override
    public boolean mayBreatheWith(ItemStack stack, EntityPlayer player) {
        return true;
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage,
                            EntityEquipmentSlot slot) {
        super.damageArmor(entity, stack, source, damage, slot);
        if (SLOT == CHEST && isPunctureDamage(source) && getMaxFlowRate(stack) > 0) {
            setPunctures(stack, getPunctures(stack) + 1);
        }
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        if (player.getItemStackFromSlot(HEAD) != stack) return;
        super.onArmorTick(world, player, stack);
        if (!isInHazardousEnvironment(player)) return;
        ItemStack chest = player.getItemStackFromSlot(CHEST);
        if (!(chest.getItem() instanceof SuSyArmorItem item)) return;
        if (!(item.getItem(chest).getArmorLogic() instanceof BreathingApparatus tank)) return;
        if (tank.getMaxFlowRate(chest) <= 0) return;
        if (tank.getOxygen(chest) <= 0) return;

        double maxFlow = tank.getMaxFlowRate(chest);
        double baseDrain = 0.05;
        double leakRate = tank.getPunctures(chest) * LEAK_PER_PUNCTURE;
        double totalDemand = baseDrain + leakRate;
        double effectiveDrain = Math.min(maxFlow, totalDemand);
        tank.changeOxygen(chest, -effectiveDrain);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHelmetOverlay(ItemStack stack, EntityPlayer player,
                                    ScaledResolution resolution, float partialTicks) {
        ItemStack chest = player.getItemStackFromSlot(CHEST);
        int punctures = 0;
        float[] holePos = null;
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof BreathingApparatus tank) {
                punctures = tank.getPunctures(chest);
                if (punctures > 0) holePos = getHolePositions(punctures);
            }
        }

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.DST_COLOR,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.disableAlpha();

        int sf = resolution.getScaleFactor();
        int fbW = resolution.getScaledWidth() * sf;
        int fbH = resolution.getScaledHeight() * sf;

        int program = ShaderManager.getRawProgram("visor.vert", "visor.frag");
        if (program > 0) {
            GL20.glUseProgram(program);
            GL20.glUniform2f(GL20.glGetUniformLocation(program, "u_resolution"), fbW, fbH);
            int holeCount = holePos != null ? Math.min(punctures, MAX_VISUAL_PUNCTURES) : 0;
            GL20.glUniform1i(GL20.glGetUniformLocation(program, "u_holeCount"), holeCount);

            glActiveTexture(GL_TEXTURE0);
            Minecraft.getMinecraft().getTextureManager()
                    .bindTexture(new ResourceLocation("susy", "textures/armor/hole_mask.png"));
            GL20.glUniform1i(GL20.glGetUniformLocation(program, "u_holeMask"), 0);

            glActiveTexture(GL_TEXTURE1);
            Minecraft.getMinecraft().getTextureManager()
                    .bindTexture(new ResourceLocation("susy", "textures/armor/hole_tex.png"));
            GL20.glUniform1i(GL20.glGetUniformLocation(program, "u_holeTex"), 1);
            glActiveTexture(GL_TEXTURE0);

            for (int i = 0; i < holeCount; i++) {
                String name = String.format("u_holes[%d]", i);
                int loc = GL20.glGetUniformLocation(program, name);
                if (loc >= 0) {
                    GL20.glUniform2f(loc, holePos[i * 2], holePos[i * 2 + 1]);
                }
            }

            GlStateManager.matrixMode(GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buf.pos(-1, 1, 0).tex(0, 0).endVertex();
            buf.pos(-1, -1, 0).tex(0, 1).endVertex();
            buf.pos(1, -1, 0).tex(1, 1).endVertex();
            buf.pos(1, 1, 0).tex(1, 0).endVertex();
            tess.draw();

            GlStateManager.matrixMode(GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL_MODELVIEW);
            GlStateManager.popMatrix();

            GL20.glUseProgram(0);
        } else {
            // 🙏
        }

        glPopAttrib();
    }

    @Override
    public double getDamageAbsorbed(ItemStack stack, EntityPlayer player) {
        this.handleDamage(stack, player);

        ItemStack chest = player.getItemStackFromSlot(CHEST);
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof SpaceSuit tank && tank.tier == tier) {
                tank.handleDamage(chest, player);

                int piecesCount = 0;
                ItemStack leggings = player.getItemStackFromSlot(LEGS);
                if (leggings.getItem() instanceof SuSyArmorItem item2) {
                    if (item2.getItem(leggings).getArmorLogic() instanceof SpaceSuit legLogic) {
                        legLogic.handleDamage(leggings, player);
                        piecesCount++;
                    }
                }

                ItemStack boots = player.getItemStackFromSlot(FEET);
                if (boots.getItem() instanceof SuSyArmorItem item2) {
                    if (item2.getItem(boots).getArmorLogic() instanceof SpaceSuit bootLogic) {
                        bootLogic.handleDamage(boots, player);
                        piecesCount++;
                    }
                }

                if (tank.getOxygen(chest) <= 0) {
                    return 0.5;
                }

                double maxFlow = tank.getMaxFlowRate(chest);
                if (maxFlow > 0) {
                    double p = tank.getPunctures(chest);
                    double baseDrain = 0.05;
                    double totalDemand = baseDrain + p * LEAK_PER_PUNCTURE;
                    double effective = Math.min(maxFlow, totalDemand);
                    double pressure = totalDemand > 0 ? effective / totalDemand : 1.0;
                    if (pressure < 0.2) {
                        return DEFAULT_ABSORPTION;
                    }
                }

                switch (piecesCount) {
                    case 0:
                    case 1:
                        return DEFAULT_ABSORPTION;
                    case 2:
                        return ABSORB_ALL;
                }
            }
        }
        return DEFAULT_ABSORPTION;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (SLOT == CHEST && getMaxOxygen(itemStack) != -1) {
            return getOxygen(itemStack) / getMaxOxygen(itemStack);
        } else {
            if (hoursOfLife > 0) {
                return 1 - getDamage(itemStack);
            }
        }
        return 1;
    }

    @Override
    public float getHeatResistance() {
        return 0.25F;
    }

    @Override
    public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor,
                                                       DamageSource source,
                                                       double damage, EntityEquipmentSlot equipmentSlot) {
        ISpecialArmor.ArmorProperties prop = new ISpecialArmor.ArmorProperties(0, 0.0, 0);
        if (source.isUnblockable())
            return prop;

        if (source == DamageSources.getHeatDamage())
            return new ISpecialArmor.ArmorProperties(0, 0.25, 5);
        if (source == DamageSources.getFrostDamage())
            return new ISpecialArmor.ArmorProperties(0, 0.20, 2);
        if (source == DamageSource.IN_FIRE)
            return new ISpecialArmor.ArmorProperties(0, 0.10, 2);
        if (source == DamageSource.ON_FIRE)
            return new ISpecialArmor.ArmorProperties(0, 0.0750, 2);
        if (source == DamageSource.LAVA)
            return new ISpecialArmor.ArmorProperties(0, 0.0375, 2);

        prop.Armor = getAbsorption(armor) * relativeAbsorption * 20;
        return prop;
    }

    public void addInformation(ItemStack stack, List<String> strings) {
        if (hoursOfLife > 0) {
            double lifetime = 60 * 60 * hoursOfLife;
            int secondsRemaining = (int) (lifetime - getDamage(stack) * lifetime);
            strings.add(I18n.format("supersymmetry.seconds_left", secondsRemaining));
        } else {
            strings.add(I18n.format("supersymmetry.unlimited"));
        }

        int armor = (int) Math.round(20.0F * this.getAbsorption(this.SLOT) * this.relativeAbsorption);
        if (armor > 0)
            strings.add(I18n.format("attribute.modifier.plus.0", armor, I18n.format("attribute.name.generic.armor")));
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return (int) Math.round(20.0F * this.getAbsorption(armor) * relativeAbsorption);
    }

    protected float getAbsorption(ItemStack itemStack) {
        return getAbsorption(getEquipmentSlot(itemStack));
    }

    protected float getAbsorption(EntityEquipmentSlot slot) {
        return switch (slot) {
            case HEAD, FEET -> 0.15F;
            case CHEST -> 0.4F;
            case LEGS -> 0.3F;
            default -> 0.0F;
        };
    }

    private double getDamage(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        if (!stack.getTagCompound().hasKey("damage")) {
            stack.getTagCompound().setDouble("damage", 0);
        }
        return stack.getTagCompound().getDouble("damage");
    }

    private void changeDamage(ItemStack stack, double damageChange) {
        NBTTagCompound compound = stack.getTagCompound();
        compound.setDouble("damage", getDamage(stack) + damageChange);
        stack.setTagCompound(compound);
    }

    private void handleDamage(ItemStack stack, EntityPlayer player) {
        if (hoursOfLife == 0 || player.dimension == DimensionBreathabilityHandler.BENEATH_ID) {
            return; // No damage
        }
        double amount = (1. / (60. * 60. * hoursOfLife));
        changeDamage(stack, amount); // It's actually ticked every overall second, not just every tick.
        if (getDamage(stack) >= 1) {
            player.renderBrokenItemStack(stack);
            stack.shrink(1);
            player.setItemStackToSlot(HEAD, ItemStack.EMPTY);
        }
    }
}
