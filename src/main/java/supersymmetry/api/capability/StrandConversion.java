package supersymmetry.api.capability;

import gregtech.api.unification.ore.OrePrefix;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Set;

public class StrandConversion {
    public static Set<StrandConversion> CONVERSIONS = new ObjectArraySet<>();

    static {
        new StrandConversion(32, 40, 1. / 40, 1. / 32, OrePrefix.foil, 72);
        new StrandConversion(8, 10, 1. / 10, 1. / 8, OrePrefix.plate, 18);
        new StrandConversion(1, 2, 1, 1. / 2., OrePrefix.plateDense, 2);
        new StrandConversion(3, 5, 1 / 5., 1 / 3., OrePrefix.plateDouble, 9);
        new StrandConversion(2 / 5., 3 / 5., 1 / 5., 2 / 5., OrePrefix.ingot, 18);
        new StrandConversion(2 / 5., 3 / 5., 1 / 5., 2 / 5., OrePrefix.stick, 36);

    }

    public double minWidth;
    public double maxWidth;
    public double minThickness;
    public double maxThickness;
    public OrePrefix prefix;
    public int amount;


    public StrandConversion(double minWidth, double maxWidth, double minThickness, double maxThickness, OrePrefix prefix, int amount) {
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minThickness = minThickness;
        this.maxThickness = maxThickness;
        this.prefix = prefix;
        this.amount = amount;

        CONVERSIONS.add(this);
    }

    public static StrandConversion getConversion(Strand strand) {
        for (StrandConversion conversion : CONVERSIONS) {
            if (strand.width >= conversion.minWidth && strand.width <= conversion.maxWidth && strand.thickness >= conversion.minThickness && strand.thickness <= conversion.maxThickness) {
                return conversion;
            }
        }
        return null;
    }
}
