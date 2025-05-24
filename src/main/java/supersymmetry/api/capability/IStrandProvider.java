package supersymmetry.api.capability;

public interface IStrandProvider {
    Strand getStrand();
    Strand take();

    // Returns what is not inserted
    Strand insertStrand(Strand strand);
}
