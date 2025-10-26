package supersymmetry.api.space;

import dev.tianmi.sussypatches.common.helper.DimDisplayRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Planetoid extends CelestialObject {

    private PlanetType planetType;
    private int dimension;

    public Planetoid(double mass, double posT, double posX, double posY, double posZ,
                     @Nullable CelestialObject parentBody, PlanetType planetType) {
        super(mass, posT, posX, posY, posZ, parentBody, CelestialBodyType.PLANETOID);
        this.planetType = planetType;
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public void setPlanetType(PlanetType planetType) {
        this.planetType = planetType;
    }

    public Planetoid setDimension(int dimension) {
        this.dimension = dimension;
        return this;
    }

    public int getDimension() {
        return dimension;
    }

    public ItemStack getDisplayItem() {
        if (DimDisplayRegistry.getDisplayItem(this.dimension) == ItemStack.EMPTY) {
            return new ItemStack(Item.getItemById(this.dimension + 1));
        }
        return DimDisplayRegistry.getDisplayItem(this.dimension);
    }
}
