package supersymmetry.common.advancement;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

import supersymmetry.Supersymmetry;

public class RocketLaunchTrigger implements ICriterionTrigger<RocketLaunchTrigger.Instance> {

    public static final ResourceLocation ID = new ResourceLocation(Supersymmetry.MODID, "rocket_launch");
    private final Map<PlayerAdvancements, Listeners> listeners = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancements, Listener<Instance> listener) {
        Listeners l = this.listeners.get(playerAdvancements);
        if (l == null) {
            l = new Listeners(playerAdvancements);
            this.listeners.put(playerAdvancements, l);
        }
        l.add(listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancements, Listener<Instance> listener) {
        Listeners l = this.listeners.get(playerAdvancements);
        if (l != null) {
            l.remove(listener);
            if (l.isEmpty()) {
                this.listeners.remove(playerAdvancements);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancements) {
        this.listeners.remove(playerAdvancements);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance();
    }

    public void trigger(EntityPlayerMP player) {
        Listeners l = this.listeners.get(player.getAdvancements());
        if (l != null) {
            l.trigger();
        }
    }

    public static class Instance extends AbstractCriterionInstance {

        public Instance() {
            super(RocketLaunchTrigger.ID);
        }
    }

    static class Listeners {

        private final PlayerAdvancements playerAdvancements;
        private final Set<Listener<Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements playerAdvancements) {
            this.playerAdvancements = playerAdvancements;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(Listener<Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(Listener<Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger() {
            List<Listener<Instance>> toGrant = Lists.newArrayList(this.listeners);
            for (Listener<Instance> listener : toGrant) {
                listener.grantCriterion(this.playerAdvancements);
            }
        }
    }
}
