package supersymmetry.mixins.geographicraft;

import climateControl.generator.CorrectedContinentsGenerator;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.GenLayerRiverMix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import supersymmetry.api.geology.GenLayerTectonicPlates;

@Mixin(value = CorrectedContinentsGenerator.class, remap = false)
public class CorrectedContinentsGeneratorMixin  {
    /*@ModifyVariable(
            method = "fromSeed",
            at = @At(
                    value = "STORE",
                    opcode = Opcodes.ASTORE,
                    ordinal = 0
            )
    )
    private GenLayerPack replaceGenLayerIsland(GenLayerPack original) {
        try {
            Field field = GenLayerPack.class.getDeclaredField("baseSeed");
            field.setAccessible(true);
            return new GenLayerTectonicPlates((Long) field.get(original), original.getParent());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }*/

    /**
     * @author
     * @reason
     */
    @Overwrite
    public GenLayerRiverMix fromSeed(long worldSeed, WorldType worldType) {

        GenLayerTectonicPlates tectonicPlates = new GenLayerTectonicPlates(worldSeed, null);

        tectonicPlates.initWorldGenSeed(worldSeed);

        GenLayerRiverMix finalLayer = new GenLayerRiverMix(worldSeed, tectonicPlates, tectonicPlates);
        return finalLayer;
    }
}