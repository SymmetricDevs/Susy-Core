package supersymmetry.supersymmetry.common.metatileentities.multiblockpart;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.client.utils.TrackedDummyWorld;
import supersymmetry.Bootstrap;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.common.metatileentities.multiblockpart.MetaTileEntityComponentScanner;

public class ComponentScannerTest {

    protected World world;
    private ComponentScannerWrapper scanner;

    @BeforeAll
    public static void setup() {
        Bootstrap.perform();
    }

    @BeforeEach
    public void setupWorld() {
        world = new TrackedDummyWorld();
        scanner = new ComponentScannerWrapper(new ResourceLocation("susy:test"));
        MetaTileEntityHolder scannerHolder = new MetaTileEntityHolder();
        scannerHolder.setMetaTileEntity(scanner);
        scannerHolder.setPos(new BlockPos(0, 0, 0));
        world.addTileEntity(scannerHolder);
    }

    @Test
    public void testScan() {
        scanner.detectComponents(new ArrayList<>());
        String reason = String.format("Scanner detected %s, when nothing was there at all!",
                scanner.struct.status);
        assertThat(scanner.struct.status, is(StructAnalysis.BuildStat.UNRECOGNIZED));
    }

    private class ComponentScannerWrapper extends MetaTileEntityComponentScanner {

        public ComponentScannerWrapper(ResourceLocation metaTileEntityId) {
            super(metaTileEntityId);
        }

        @Override
        public World getWorld() {
            return world;
        }
    }
}
