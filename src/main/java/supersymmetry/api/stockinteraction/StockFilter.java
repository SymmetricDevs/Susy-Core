package supersymmetry.api.stockinteraction;

import cam72cam.immersiverailroading.entity.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StockFilter implements INBTSerializable<NBTTagCompound> {

    private HashMap<String, Boolean> selection;

    private List<String> subFilter;

    private static HashMap<String, Class> nameToStockClassMap = new HashMap<>();
    static {
        nameToStockClassMap.put("any", EntityRollingStock.class);
        nameToStockClassMap.put("locomotive", Locomotive.class);
        nameToStockClassMap.put("freight_tank", CarTank.class);
        nameToStockClassMap.put("freight", CarFreight.class);
        nameToStockClassMap.put("coupleable", EntityCoupleableRollingStock.class);
    }

    // Default constructor, every filter option available
    public StockFilter() {
        this(nameToStockClassMap.keySet().stream().collect(Collectors.toList()));
    }

    public StockFilter(List<String> subFilter) {
        this.setSubFilter(subFilter);
        this.selection = new HashMap<>();
    }

    public List<Class> getSelection() {
        return selection.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> nameToStockClassMap.get(entry.getKey()))
                .collect(Collectors.toList());
    }

    public void setFilterIsSelected(String name, boolean isSelected) {
        if(this.getSubFilter().contains(name))
            this.selection.put(name, isSelected);
    }

    public boolean getFilterIsSelected(String name) {
        return this.selection.getOrDefault(name, false);
    }

    public void toggleFilterSelected(String name) {
        if(this.getSubFilter().contains(name))
            this.selection.put(name, !this.selection.getOrDefault(name, false));
    }

    public List<String> getSubFilter() {
        return subFilter;
    }

    public void setSubFilter(List<String> subFilter) {
        this.subFilter = subFilter;
    }

    public AbstractWidgetGroup getFilterWidgetGroup() {
        WidgetGroup widgetGroup = new WidgetGroup();

        for (int i = 0; i < getSubFilter().size(); i++) {
            String name = getSubFilter().get(i);
            widgetGroup.addWidget(
                new ToggleButtonWidget(7, 8 + i * 18, 18, 18, GuiTextures.BUTTON_LOCK, () -> this.getFilterIsSelected(name), (isSelected) -> this.setFilterIsSelected(name, isSelected))
                    .setTooltipText("susy.gui.stock_filter.filter_button.tooltip", new Object[]{name})
                    .shouldUseBaseBackground());
        }
        
        return widgetGroup;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        selection.forEach(compound::setBoolean);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        selection.clear();
        compound.getKeySet().forEach(key -> selection.put(key, compound.getBoolean(key)));
    }

    public List<EntityRollingStock> filterEntities(List<EntityRollingStock> entities) {
        return entities.stream()
                .filter(this::shouldIncludeEntity)
                .collect(Collectors.toList());
    }

    private boolean shouldIncludeEntity(EntityRollingStock entity) {
        for (Map.Entry<String, Boolean> entry : selection.entrySet()) {
            String className = entry.getKey();
            Class targetClass = nameToStockClassMap.get(className);

            if (entry.getValue() && targetClass.isInstance(entity) && entity.getClass().equals(targetClass))
                return true;
        }
        return false;
    }
}
