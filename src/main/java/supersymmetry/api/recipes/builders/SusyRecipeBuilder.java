package supersymmetry.api.recipes.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.jspecify.annotations.NonNull;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import org.apache.commons.lang3.builder.ToStringBuilder;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import supersymmetry.api.recipes.properties.AtmosphereProperty;
import supersymmetry.api.recipes.properties.BiomeProperty;
import supersymmetry.api.recipes.properties.DimensionProperty;

/*
 * Contains a variety of helper methods for adding additional properties.
 */
public class SusyRecipeBuilder extends RecipeBuilder<SusyRecipeBuilder> {

    private int minimumDuration = 0;

    public SusyRecipeBuilder() {}

    public SusyRecipeBuilder(Recipe recipe, RecipeMap<SusyRecipeBuilder> recipeMap) {
        super(recipe, recipeMap);
    }

    public SusyRecipeBuilder(RecipeBuilder<SusyRecipeBuilder> recipeBuilder) {
        super(recipeBuilder);
    }

    @Override
    public SusyRecipeBuilder copy() {
        return new SusyRecipeBuilder(this);
    }

    @Override
    public boolean applyProperty(@NonNull String key, Object value) {
        if (key.equals(DimensionProperty.KEY)) {
            if (value instanceof Integer) {
                this.dimension((Integer) value);
            } else if (value instanceof List && !((List<?>) value).isEmpty() &&
                    ((List<?>) value).get(0) instanceof Integer) {
                        IntList dimensionIDs = getDimensionIDs();
                        if (dimensionIDs == IntLists.EMPTY_LIST) {
                            dimensionIDs = new IntArrayList();
                            this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
                        }
                        dimensionIDs.addAll((List<Integer>) value);
                    } else {
                        throw new IllegalArgumentException("Invalid Dimension Property Type!");
                    }
            return true;
        } else if (key.equals(BiomeProperty.KEY)) {
            if (value instanceof BiomeProperty.BiomePropertyList list) {
                BiomeProperty.BiomePropertyList biomes = getBiomePropertyList();
                if (biomes == BiomeProperty.BiomePropertyList.EMPTY_LIST) {
                    biomes = new BiomeProperty.BiomePropertyList();
                    this.applyProperty(BiomeProperty.getInstance(), biomes);
                }
                biomes.merge(list);
                return true;
            }
            return false;
        }
        return super.applyProperty(key, value);
    }

    public SusyRecipeBuilder dimension(int dimensionID) {
        IntList dimensionIDs = getDimensionIDs();
        if (dimensionIDs == IntLists.EMPTY_LIST) {
            dimensionIDs = new IntArrayList();
            this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
        }
        dimensionIDs.add(dimensionID);
        return this;
    }

    public SusyRecipeBuilder requireAtmosphere() {
        this.applyProperty(AtmosphereProperty.getInstance(), true);
        return this;
    }

    public SusyRecipeBuilder requireVacuum() {
        this.applyProperty(AtmosphereProperty.getInstance(), false);
        return this;
    }

    @Override
    public SusyRecipeBuilder duration(int duration) {
        return super.duration(Math.max(duration, this.minimumDuration));
    }

    public SusyRecipeBuilder minimumDuration(int minimumDuration) {
        this.minimumDuration = minimumDuration;
        return this;
    }

    public IntList getDimensionIDs() {
        return this.recipePropertyStorage == null ? IntLists.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(DimensionProperty.getInstance(),
                        IntLists.EMPTY_LIST);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                .append(DimensionProperty.getInstance().getKey(), getDimensionIDs().toString())
                .append("biomes", getBiomePropertyList()).toString();
    }

    public Boolean getAtmosphereProperty() {
        return this.recipePropertyStorage == null ? null :
                this.recipePropertyStorage.getRecipePropertyValue(AtmosphereProperty.getInstance(), null);
    }

    public SusyRecipeBuilder biomes(String... biomes) {
        return biomes(false, biomes);
    }

    private SusyRecipeBuilder biomes(boolean toBlacklist, String... biomeRLs) {
        List<Biome> biomes = new ArrayList<>();
        for (String biomeRL : biomeRLs) {
            Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeRL));
            if (biome != null) {
                biomes.add(biome);
            } else {
                throw new NoSuchElementException("No biome with ResouceLocation \"" + biomeRL + "\" found");
            }
        }
        return biomesInternal(toBlacklist, biomes);
    }

    private SusyRecipeBuilder biomesInternal(boolean toBlacklist, List<Biome> biomes) {
        BiomeProperty.BiomePropertyList biomePropertyList = getBiomePropertyList();
        if (biomePropertyList == BiomeProperty.BiomePropertyList.EMPTY_LIST) {
            biomePropertyList = new BiomeProperty.BiomePropertyList();
            this.applyProperty(BiomeProperty.getInstance(), biomePropertyList);
        }
        for (Biome biome : biomes) {
            biomePropertyList.add(biome, toBlacklist);
        }
        return this;
    }

    public BiomeProperty.BiomePropertyList getBiomePropertyList() {
        return this.recipePropertyStorage == null ? BiomeProperty.BiomePropertyList.EMPTY_LIST :
                this.recipePropertyStorage.getRecipePropertyValue(BiomeProperty.getInstance(),
                        BiomeProperty.BiomePropertyList.EMPTY_LIST);
    }
}
