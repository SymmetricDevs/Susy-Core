package supersymmetry.common.metatileentities.creative;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IControllable;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.api.particle.IParticleBeamProvider;
import supersymmetry.api.particle.ParticleBeam;
import supersymmetry.api.particle.Particles;
import supersymmetry.api.util.SuSyUtility;

import java.util.function.Function;

public class MetaTileEntityCreativeParticleSource extends MetaTileEntity implements IParticleBeamProvider, IControllable {

    private boolean active = false;

    private double energy = 0;
    private double bunchSpacing = 1;
    private double bunchLength = 1;
    private int nBunches = 1000;
    private int particlesPerBunch = (int) 1e6;
    private double emittance = 1.d;
    private double beamSize = 1.d;

    private ParticleBeam particleBeam = new ParticleBeam(
            Particles.electron,
            0.d,
            1.d,
            1.d,
            1000,
            (int) 1e6,
            1.d,
            1.d
    );

    public MetaTileEntityCreativeParticleSource() {
        super(SuSyUtility.susyId("infinite_particles"));
    }

    public MetaTileEntityCreativeParticleSource(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[14].render(renderState, translation, renderPipeline, Cuboid6.full);
        for (EnumFacing face : EnumFacing.VALUES) {
            Textures.INFINITE_EMITTER_FACE.renderSided(face, renderState, translation, pipeline);
        }
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[14].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeParticleSource();
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == SuSyCapabilities.PARTICLE_BEAM_PROVIDER) {
            return SuSyCapabilities.PARTICLE_BEAM_PROVIDER.cast(this);
        } else {
            return super.getCapability(capability, side);
        }
    }

    @Override
    public boolean isWorkingEnabled() {
        return active;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        setActive(isWorkingAllowed);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.label(7, 10, "susy.creative.particle.type");
        builder.label(7, 20, "susy.creative.particle.energy");
        builder.widget(new TextFieldWidget2(9, 30, 152, 8, () -> String.valueOf(energy), value -> {
            if (!value.isEmpty()) {
                energy = Double.parseDouble(value);
            }
        }).setAllowedChars(TextFieldWidget2.DECIMALS).setMaxLength(19).setValidator(getTextFieldValidator()));
        builder.label(7, 40, "susy.creative.particle.bunchspacing");
        builder.widget(new TextFieldWidget2(9, 50, 152, 8, () -> String.valueOf(bunchSpacing), value -> {
            if (!value.isEmpty()) {
                bunchSpacing = Double.parseDouble(value);
            }
        }).setAllowedChars(TextFieldWidget2.DECIMALS).setMaxLength(19).setValidator(getTextFieldValidator()));
        builder.label(7, 60, "susy.creative.particle.bunchlength");
        builder.widget(new TextFieldWidget2(9, 70, 152, 8, () -> String.valueOf(bunchLength), value -> {
            if (!value.isEmpty()) {
                bunchLength = Double.parseDouble(value);
            }
        }).setAllowedChars(TextFieldWidget2.DECIMALS).setMaxLength(19).setValidator(getTextFieldValidator()));
        builder.label(7, 80, "susy.creative.particle.nbunches");
        builder.widget(new TextFieldWidget2(9, 90, 152, 8, () -> String.valueOf(nBunches), value -> {
            if (!value.isEmpty()) {
                nBunches = Integer.parseInt(value);
            }
        }).setAllowedChars(TextFieldWidget2.NATURAL_NUMS).setMaxLength(19).setValidator(getTextFieldValidator()));
        builder.label(7, 100, "susy.creative.particle.particlesperbunch");
        builder.widget(new TextFieldWidget2(9, 110, 152, 8, () -> String.valueOf(particlesPerBunch), value -> {
            if (!value.isEmpty()) {
                particlesPerBunch = Integer.parseInt(value);
            }
        }).setAllowedChars(TextFieldWidget2.NATURAL_NUMS).setMaxLength(19).setValidator(getTextFieldValidator()));
        builder.label(7, 120, "susy.creative.particle.emittance");
        builder.widget(new TextFieldWidget2(9, 130, 152, 8, () -> String.valueOf(emittance), value -> {
            if (!value.isEmpty()) {
                emittance = Double.parseDouble(value);
            }
        }).setAllowedChars(TextFieldWidget2.DECIMALS).setMaxLength(19).setValidator(getTextFieldValidator()));
        builder.label(7, 140, "susy.creative.particle.beamsize");
        builder.widget(new TextFieldWidget2(9, 150, 152, 8, () -> String.valueOf(beamSize), value -> {
            if (!value.isEmpty()) {
                beamSize = Double.parseDouble(value);
            }
        }).setAllowedChars(TextFieldWidget2.DECIMALS).setMaxLength(19).setValidator(getTextFieldValidator()));
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public ParticleBeam getParticleBeam() {
        return particleBeam;
    }

    @Override
    public ParticleBeam insertBeam(ParticleBeam beam) {
        return null;
    }

    public static Function<String, String> getTextFieldValidator() {
        return val -> {
            if (val.isEmpty()) {
                return "0";
            }
            long num;
            try {
                num = Long.parseLong(val);
            } catch (NumberFormatException ignored) {
                return "0";
            }
            if (num < 0) {
                return "0";
            }
            return val;
        };
    }
}
