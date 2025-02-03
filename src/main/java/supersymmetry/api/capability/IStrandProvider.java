package supersymmetry.api.capability;

public interface IStrandProvider {
    Strand getStrand();
    Strand take();
}
