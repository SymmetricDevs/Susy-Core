package supersymmetry.api.space;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.tianmi.sussypatches.common.helper.DimDisplayRegistry;

public class Planetoid extends CelestialObject {

    private PlanetType planetType;
    private int dimension;
    public static BiMap<Planetoid, Integer> PLANETOIDS = HashBiMap.create();

    public Planetoid(String translationKey, double mass, double posT, double posX, double posY, double posZ,
                     @Nullable CelestialObject parentBody, PlanetType planetType) {
        super(translationKey, posT, posX, posY, posZ, mass, CelestialBodyType.PLANETOID, parentBody);
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
        PLANETOIDS.put(this, dimension);
        return this;
    }

    public int getDimension() {
        return dimension;
    }

    public ItemStack getDisplayItem() {
        if (DimDisplayRegistry.getDisplayItem(this.dimension).isEmpty()) {
            return new ItemStack(Item.getItemById(this.dimension + 1));
        }
        return DimDisplayRegistry.getDisplayItem(this.dimension);
    }
}
