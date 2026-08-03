package supersymmetry.api.metatileentity.multiblock;

import supersymmetry.api.rocketry.components.AbstractComponent;

/**
 * Implemented by multiblock controllers that build a rocket one component at a
 * time using {@link supersymmetry.api.recipes.logic.RocketAssemblerLogic}.
 * Implementors must also extend
 * {@link gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController}.
 * <p>
 * The "assembly site" is wherever the rocket is taking shape: the transporter
 * erector parked on the rails for the rocket assembler, the pad itself for the
 * lunar launch complex.
 */
public interface IRocketAssemblyController {

    boolean isAssemblyWorking();

    /**
     * The component currently being built, or null if there is nothing left to
     * build. Implementors are allowed to abort the assembly as a side effect of
     * this call when the index has run off the end of the list.
     */
    AbstractComponent<?> getCurrentCraftTarget();

    /**
     * Advances to the next component. Called once the current component's recipe
     * completes.
     */
    void nextComponent();

    int getComponentIndex();

    int getComponentCount();

    /**
     * Whether an assembly site exists at all. Checked before a recipe is looked up.
     */
    boolean isAssemblySiteAvailable();

    /**
     * Whether the assembly site exists <em>and</em> is in sync with
     * {@link #getComponentIndex()}.
     */
    boolean isAssemblySiteReady();

    /** Called once every component in the list has been built. */
    void finishAssembly();

    /**
     * Called when a component's recipe starts, for controllers that show the rocket
     * taking shape in-world.
     */
    default void onComponentSetup() {}
}
