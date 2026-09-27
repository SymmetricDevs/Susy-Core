package supersymmetry.integration.groovyscript;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.Alias;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;

import supersymmetry.api.rocketry.costs.RocketBlueprintCosts;
import supersymmetry.api.rocketry.costs.RocketCostEntry;
import supersymmetry.api.rocketry.costs.RocketCostGroup;

/**
 * Script-facing view of {@link RocketBlueprintCosts}.
 *
 * <pre>{@code
 * mods.susy.rocketCosts.add('soyuz', 'plumbing')
 *         .input(ore('pipeSmallStainlessSteel'), 16)
 *         .input(item('minecraft:redstone'), 8)
 *         .duration(20)
 *         .register()
 * }</pre>
 *
 * The blueprint does not have to exist yet — costs are keyed by name and resolved
 * when assembly starts, so script order against SuSy's own registration does not
 * matter.
 */
public class RocketCostRegistry extends VirtualizedRegistry<RocketCostRegistry.ScriptedGroup> {

    public RocketCostRegistry() {
        super(Alias.generateOf("RocketCosts"));
    }

    @Override
    public void onReload() {
        removeScripted().forEach(entry -> RocketBlueprintCosts.remove(entry.blueprint, entry.group));
        restoreFromBackup().forEach(entry -> RocketBlueprintCosts.add(entry.blueprint, entry.group));
    }

    public GroupBuilder add(String blueprintName, String groupName) {
        return new GroupBuilder(blueprintName, groupName);
    }

    /** Drops every cost group on one blueprint. */
    public void remove(String blueprintName) {
        for (RocketCostGroup group : RocketBlueprintCosts.get(blueprintName)) {
            addBackup(new ScriptedGroup(blueprintName, group));
        }
        RocketBlueprintCosts.clear(blueprintName);
    }

    public void removeAll() {
        RocketBlueprintCosts.getAll()
                .forEach((blueprint, groups) -> groups.forEach(g -> addBackup(new ScriptedGroup(blueprint, g))));
        RocketBlueprintCosts.clearAll();
    }

    public static class ScriptedGroup {

        private final String blueprint;
        private final RocketCostGroup group;

        ScriptedGroup(String blueprint, RocketCostGroup group) {
            this.blueprint = blueprint;
            this.group = group;
        }
    }

    public class GroupBuilder {

        private final String blueprint;
        private final String groupName;
        private final List<RocketCostEntry> entries = new ArrayList<>();
        private double duration = RocketCostGroup.DEFAULT_ASSEMBLY_DURATION;

        GroupBuilder(String blueprint, String groupName) {
            this.blueprint = blueprint;
            this.groupName = groupName;
        }

        /** Seconds this group takes to assemble. */
        public GroupBuilder duration(double seconds) {
            this.duration = seconds;
            return this;
        }

        public GroupBuilder input(IIngredient ingredient) {
            return input(ingredient, ingredient.getAmount());
        }

        public GroupBuilder input(IIngredient ingredient, int amount) {
            RocketCostEntry entry = toEntry(ingredient, amount);
            if (entry != null) {
                entries.add(entry);
            }
            return this;
        }

        public GroupBuilder input(String oreDict, int amount) {
            entries.add(new RocketCostEntry(oreDict, amount));
            return this;
        }

        public void register() {
            if (entries.isEmpty()) {
                GroovyLog.msg("Error adding SuSy rocket blueprint cost")
                        .add("group '{}' on blueprint '{}' has no inputs", groupName, blueprint)
                        .error().post();
                return;
            }
            RocketCostGroup group = new RocketCostGroup(groupName, entries, duration);
            RocketBlueprintCosts.add(blueprint, group);
            addScripted(new ScriptedGroup(blueprint, group));
        }

        private RocketCostEntry toEntry(IIngredient ingredient, int amount) {
            if (ingredient instanceof OreDictIngredient oreDict) {
                return new RocketCostEntry(oreDict.getOreDict(), amount);
            }
            Object raw = ingredient;
            if (raw instanceof ItemStack stack) {
                return new RocketCostEntry(stack, amount);
            }
            GroovyLog.msg("Error adding SuSy rocket blueprint cost")
                    .add("'{}' is not an item or ore dictionary entry", ingredient)
                    .error().post();
            return null;
        }
    }
}
