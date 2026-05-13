package supersymmetry.api.mixin;

import java.util.List;

import supersymmetry.common.entities.EntityDropPod;

public interface IDropPodRadar {

    List<EntityDropPod> susy$getIncomingDropPods();
}
