package supersymmetry.api.particle;

import static supersymmetry.api.util.SuSyUtility.susyId;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.jspecify.annotations.Nullable;

import supersymmetry.api.SusyLog;

public class Particles {

    public static final Map<String, Particle> particleRegistry = new HashMap<String, Particle>();

    // Leptons
    public static Particle electronNeutrino;
    public static Particle muonNeutrino;
    public static Particle tauonNeutrino;
    public static Particle electron;
    public static Particle muon;
    public static Particle tauon;

    // Quarks
    public static Particle up;
    public static Particle down;
    public static Particle strange;
    public static Particle charm;
    public static Particle bottom;
    public static Particle top;

    // Gauge Bosons
    public static Particle gluon;
    public static Particle photon;
    public static Particle WPlus;
    public static Particle WMinus;
    public static Particle Z;
    public static Particle higgs;

    // Leptons
    public static Particle positron;
    public static Particle antiMuon;
    public static Particle antiTauon;
    public static Particle electronAntineutrino;
    public static Particle muonAntineutrino;
    public static Particle tauonAntineutrino;

    // Quarks
    public static Particle antiUp;
    public static Particle antiDown;
    public static Particle antiStrange;
    public static Particle antiCharm;
    public static Particle antiBottom;
    public static Particle antiTop;

    // Composite particles start here
    public static Particle proton;
    public static Particle antiProton;
    public static Particle neutron;
    public static Particle antiNeutron;

    public static void init() {
        // Leptons
        electronNeutrino = new Particle("electron_neutrino", 0.000002, 0, 0.5, 0.0, false, true,
                susyId("particles/electron_neutrino.png"));
        muonNeutrino = new Particle("muon_neutrino", 0.00019, 0, 0.5, 0.0, false, true,
                susyId("particles/muon_neutrino.png"));
        tauonNeutrino = new Particle("tauon_neutrino", 18.2, 0, 0.5, 0.0, false, true,
                susyId("particles/tauon_neutrino.png"));
        electron = new Particle("electron", 0.511, -1, 0.5, 0.0, false, true, susyId("particles/electron.png"));
        muon = new Particle("muon", 105.66, -1, 0.5, 0.0, false, true, susyId("particles/muon.png"));
        tauon = new Particle("tauon", 1776.86, -1, 0.5, 0.0, false, true, susyId("particles/tauon.png"));

        // Quarks
        up = new Particle("up", 2.2, +0.666f, 0.5, 0.0, true, true, susyId("particles/up.png"));
        down = new Particle("down", 4.7, -0.333f, 0.5, 0.0, true, true, susyId("particles/down.png"));
        strange = new Particle("strange", 96, -0.333f, 0.5, 0.0, true, true, susyId("particles/strange.png"));
        charm = new Particle("charm", 1270, +0.666f, 0.5, 0.0, true, true, susyId("particles/charm.png"));
        bottom = new Particle("bottom", 4180, -0.333f, 0.5, 0.0, true, true, susyId("particles/bottom.png"));
        top = new Particle("top", 172900, +0.666f, 0.5, 1400.0, true, true, susyId("particles/top.png"));

        // AntiLeptons
        positron = new Particle("positron", 0.511, +1, 0.5, 0.0, false, true, susyId("particles/positron.png"));
        antiMuon = new Particle("antimuon", 105.66, +1, 0.5, 0.0, false, true, susyId("particles/antimuon.png"));
        antiTauon = new Particle("antitauon", 1776.86, +1, 0.5, 0.0, false, true, susyId("particles/antitauon.png"));
        electronAntineutrino = new Particle("electron_antineutrino", 0.000002, 0, 0.5, 0.0, false, true,
                susyId("particles/electron_antineutrino.png"));
        muonAntineutrino = new Particle("muon_antineutrino", 0.00019, 0, 0.5, 0.0, false, true,
                susyId("particles/muon_antineutrino.png"));
        tauonAntineutrino = new Particle("tauon_antineutrino", 18.2, 0, 0.5, 0.0, false, true,
                susyId("particles/tauon_antineutrino.png"));

        // AntiQuarks
        antiUp = new Particle("antiup", 2.2, -0.666f, 0.5, 0.0, true, true, susyId("particles/antiup.png"));
        antiDown = new Particle("antidown", 4.7, +0.333f, 0.5, 0.0, true, true, susyId("particles/antidown.png"));
        antiStrange = new Particle("antistrange", 96, +0.333f, 0.5, 0.0, true, true,
                susyId("particles/antistrange.png"));
        antiCharm = new Particle("anticharm", 1270, -0.666f, 0.5, 0.0, true, true, susyId("particles/anticharm.png"));
        antiBottom = new Particle("antibottom", 4180, +0.333f, 0.5, 0.0, true, true,
                susyId("particles/antibottom.png"));
        antiTop = new Particle("antitop", 172900, -0.666f, 0.5, 1400.0, true, true, susyId("particles/antitop.png"));

        // Gauge Bosons
        gluon = new Particle("gluon", 0.0, 0, 1.0, 0.0, true, false, susyId("particles/gluon.png"));
        photon = new Particle("photon", 0.0, 0, 1.0, 0.0, false, false, susyId("particles/photon.png"));
        WPlus = new Particle("W+_boson", 80379.0, +1, 1.0, 2085.0, false, true, susyId("particles/wplus.png"));
        WMinus = new Particle("W-_boson", 80379.0, -1, 1.0, 2085.0, false, true, susyId("particles/wminus.png"));
        Z = new Particle("Z_boson", 91187.6, 0, 1.0, 2495.0, false, true, susyId("particles/z_boson.png"));
        higgs = new Particle("Higgs_boson", 125100.0, 0, 0.0, 4300.0, false, false, susyId("particles/higgs.png"));

        electron.setAntiParticle(positron);
        muon.setAntiParticle(antiMuon);
        tauon.setAntiParticle(antiTauon);
        electronNeutrino.setAntiParticle(electronAntineutrino);
        muonNeutrino.setAntiParticle(muonAntineutrino);
        tauonNeutrino.setAntiParticle(tauonAntineutrino);

        up.setAntiParticle(antiUp);
        down.setAntiParticle(antiDown);
        strange.setAntiParticle(antiStrange);
        charm.setAntiParticle(antiCharm);
        bottom.setAntiParticle(antiBottom);
        top.setAntiParticle(antiTop);

        WPlus.setAntiParticle(WMinus);

        // Composite particles
        proton = new Particle("proton", 938.272, +1, 0.5, 0, false, true, susyId("particles/proton.png"));
        proton.addComponent(up, 2);
        proton.addComponent(down, 1);
        proton.addComponent(gluon, 0);
        antiProton = makeAntiParticle(proton, susyId("particles/antiproton.png"));
        proton.setAntiParticle(antiProton);

        neutron = new Particle("neutron", 939.565, 0, 0.5, 0, false, true, susyId("particles/neutron.png"));
        neutron.addComponent(up, 1);
        neutron.addComponent(down, 2);
        neutron.addComponent(gluon, 0);
        antiNeutron = makeAntiParticle(neutron, susyId("particles/antineutron.png"));
        neutron.setAntiParticle(antiNeutron);
    }

    public static void register() {
        // Leptons
        registerParticle(electronNeutrino);
        registerParticle(muonNeutrino);
        registerParticle(tauonNeutrino);
        registerParticle(electron);
        registerParticle(muon);
        registerParticle(tauon);
        registerParticle(electronAntineutrino);
        registerParticle(muonAntineutrino);
        registerParticle(tauonAntineutrino);
        registerParticle(positron);
        registerParticle(antiMuon);
        registerParticle(antiTauon);

        // Quarks
        registerParticle(up);
        registerParticle(down);
        registerParticle(strange);
        registerParticle(charm);
        registerParticle(bottom);
        registerParticle(top);
        registerParticle(antiUp);
        registerParticle(antiDown);
        registerParticle(antiStrange);
        registerParticle(antiCharm);
        registerParticle(antiBottom);
        registerParticle(antiTop);

        // Gauge bosons
        registerParticle(gluon);
        registerParticle(photon);
        registerParticle(WPlus);
        registerParticle(WMinus);
        registerParticle(Z);
        registerParticle(higgs);

        // Composite particles
        registerParticle(proton);
        registerParticle(antiProton);
        registerParticle(neutron);
        registerParticle(antiNeutron);
    }

    public static void registerParticle(Particle particle) {
        if (!particleRegistry.containsKey(particle.getName())) {
            particleRegistry.put(particle.getName(), particle);
        } else {
            SusyLog.logger.error("Particle " + particle.getName() + " cannot be registered as it already exists");
        }
    }

    public static Particle getByName(String name) {
        if (name != null) {
            if (particleRegistry.containsKey(name)) {
                return particleRegistry.get(name);
            } else {
                SusyLog.logger.error("Particle " + name + " does not exist");
            }
        }
        return null;
    }

    /**
     * @param particle
     *                 The particle you want the antiparticle of, must be a composite
     *                 particle, fundamental antiparticles must be generated manually
     * @param name
     *                 The name of the antiparticle you want, another method is available
     *                 that will generate it automatically
     * @param location
     *                 Texture location
     * @return The desired antiparticle
     */
    @Nullable public static Particle makeAntiParticle(Particle particle, String name, ResourceLocation location) {
        if (particle.isFundamental())
            return null;
        Particle antiParticle = new Particle(name, particle.getMass(), -particle.getCharge(), particle.getSpin(),
                particle.getWidth(), particle.isColoured(), particle.isWeakInt(), location);
        for (Map.Entry<Particle, Integer> component : particle.getComponents().entrySet()) {
            antiParticle.setComponent(component.getKey().getAntiParticle(), component.getValue());
        }
        return antiParticle;
    }

    /**
     * @param particle
     *                 The particle you want the antiparticle of, must be a composite
     *                 particle, fundamental antiparticles must be generated manually
     * @param location
     *                 Texture location
     * @return The desired antiparticle
     */
    @Nullable public static Particle makeAntiParticle(Particle particle, ResourceLocation location) {
        return makeAntiParticle(particle, "anti" + particle.getName(), location);
    }
}
