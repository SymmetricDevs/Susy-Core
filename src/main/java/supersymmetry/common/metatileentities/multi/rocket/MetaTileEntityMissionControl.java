package supersymmetry.common.metatileentities.multi.rocket;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityBlueprintRocket;
import supersymmetry.common.mui.widget.ConditionalWidget;
import supersymmetry.common.rocketry.RocketConfiguration.*;
import supersymmetry.common.rocketry.SuccessCalculation.LaunchResult;

public class MetaTileEntityMissionControl extends MultiblockWithDisplayBase implements IDataStickIntractable {

    private UUID selectedRocketUuid;

    public MetaTileEntityMissionControl(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {}

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "CCCCC")
                .aisle("CCCCC", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "CCCCC")
                .aisle("CCCCC", "C   C", "CCCCC")
                .aisle("CCSCC", "CCCCC", "CCCCC")
                .where('S', selfPredicate())
                .where(' ', air())
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setMinGlobalLimited(1, 1))
                        .or(maintenancePredicate()))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HIGH_POWER_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMissionControl(metaTileEntityId);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tag = super.writeToNBT(data);
        if (selectedRocketUuid != null) {
            tag.setUniqueId("RocketUUID", selectedRocketUuid);
        }
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasUniqueId("RocketUUID")) {
            selectedRocketUuid = data.getUniqueId("RocketUUID");
        }
    }

    @Override
    public void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = dataStick.getTagCompound();
        if (tag == null || !player.isSneaking()) {
            return;
        }

        // Not Focus Rn
        /*
         * String metaName = tag.getString("MetaTileEntity");
         * if (metaName == null || !metaName.equals("susy:ground_station")) {
         * return;
         * }
         * if (tag.getInteger("Dimension") != this.getWorld().provider.getDimension()) {
         * return;
         * }
         * 
         * int[] pos = tag.getIntArray("Position");
         * BlockPos blockPos = new BlockPos(pos[0], pos[1], pos[2]);
         * 
         * TileEntity tileEntity = this.getWorld().getTileEntity(blockPos);
         * if (!(tileEntity instanceof IGregTechTileEntity gtTileEntity)) {
         * return;
         * }
         * if (!(gtTileEntity.getMetaTileEntity() instanceof MetaTileEntityGroundStation groundStation)) {
         * return;
         * }
         * 
         * this.groundStation = groundStation;
         */

        if (tag.hasUniqueId("RocketUUID")) {
            this.selectedRocketUuid = tag.getUniqueId("RocketUUID");
        }
    }

    @Override
    public boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        return false;
    }

    private static ITextComponent dimensionName(int dimension) {
        Planetoid planetoid = Planetoid.PLANETOIDS.inverse().get(dimension);
        return planetoid == null ? new TextComponentString(String.valueOf(dimension)) :
                new TextComponentTranslation(planetoid.getTranslationKey());
    }

    private EntityBlueprintRocket getRocket() {
        if (this.selectedRocketUuid == null) {
            return null;
        }

        if (!(this.getWorld() instanceof WorldServer worldServer)) {
            return null;
        }
        return worldServer.getEntityFromUuid(this.selectedRocketUuid) instanceof EntityBlueprintRocket rocket ?
                rocket : null;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        int width = 320;
        int height = 210;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, width, height);
        builder.image(4, 4, width - 8, height - 8, GuiTextures.DISPLAY);
        builder.image(width - 24, height - 24, 16, 16, GuiTextures.GREGTECH_LOGO_XMAS);

        ConditionalWidget mainGroup = new ConditionalWidget(4, 4, width - 8, height - 8, () -> true);

        // TODO: switch to lang
        mainGroup.addWidget(new AdvancedTextWidget(4, 4, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                l.add(new TextComponentString("Selected Rocket: " + rocket.getName()));
            } else {
                l.add(new TextComponentString("No Rocket Selected"));
            }
        }, 0xe38a0e));
        mainGroup.addWidget(new AdvancedTextWidget(4, 8 + 8, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                if (rocket.isLaunched()) {
                    if (rocket.posY == rocket.prevPosY) {
                        LaunchResult result = rocket.getLaunchResult();
                        if (result == LaunchResult.CRASHES) {
                            BlockPos crash = rocket.getCrashPosition();
                            l.add(new TextComponentString("Launch Status: Crashed" +
                                    (crash == null ? "" : ", Position: " + crash)));
                        } else if (result == LaunchResult.EXPLODES) {
                            l.add(new TextComponentString("Launch Status: Exploded"));
                        } else {
                            l.add(new TextComponentString("Launch Status: Launching"));
                        }
                    } else {
                        l.add(new TextComponentString("Launch Status: Launching"));
                    }
                } else {
                    l.add(new TextComponentString("Launch Status: Waiting to launch"));
                }
            }
        }, 0xffffff));

        mainGroup.addWidget(new AdvancedTextWidget(4, 16 + 24, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                l.add(new TextComponentString("Cargo: " + rocket.getCargoMass() + " kg"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget((width - 8) / 3, 12 + 16, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                l.add(new TextComponentString("Height: " + rocket.getPosition().getY() + " m"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget((width - 8) / 3, 16 + 24, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                double totalVelocity = Math.sqrt(Math.pow(rocket.motionX, 2) +
                        Math.pow(rocket.motionY, 2) + Math.pow(rocket.motionZ, 2));
                l.add(new TextComponentString("Velocity: " + Math.round(totalVelocity * 20) + " m/s"));
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget(4, 20 + 32, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                if (rocket.hasActed()) {
                    l.add(new TextComponentString("Action Status: Has acted"));
                } else {
                    l.add(new TextComponentString("Action Status: Waiting to act"));
                }
            }
        }, 0xffffff));
        mainGroup.addWidget(new AdvancedTextWidget(4, 24 + 40, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                List<MissionConfiguration> missions = rocket.getRocketConfiguration().getMissions();
                for (int i = 0; i < missions.size(); i++) {
                    MissionConfiguration mission = missions.get(i);
                    l.add(new TextComponentString("Mission " + (i + 1) + ": "));
                    l.add(dimensionName(mission.dimension));
                    l.add(new TextComponentString(", " + mission.destinationType.name()));
                }
                if (missions.isEmpty()) {
                    l.add(new TextComponentString("No missions"));
                }
            }
        }, 0xffffff));

        // Debug stuff
        mainGroup.addWidget(new LabelWidget(4, 32 + 64, "[DEBUG]", 0xff0000));
        mainGroup.addWidget(new AdvancedTextWidget(4, 36 + 72, (l) -> {
            var rocket = getRocket();
            if (rocket != null) {
                l.add(new TextComponentString("Launch Result: " + rocket.getLaunchResult()));
            }
        }, 0xff9999));

        builder.widget(mainGroup);

        return builder;
    }
}
