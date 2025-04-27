package supersymmetry.api.capability;

public interface RecipeMapExtension {
    void modifyMaxInputs(int maxInputs);

    void modifyMaxOutputs(int maxOutputs);

    void modifyMaxFluidInputs(int maxFluidInputs);
    
    void modifyMaxFluidOutputs(int maxFluidOutputs);
}
