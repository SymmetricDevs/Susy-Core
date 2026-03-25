package supersymmetry.common.mui.widget;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resources.I18n;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class RocketSimulatorComponentContainerWidget extends AbstractWidgetGroup {

    public static int rowSeparation = 18; // mc slot size hopefully
    public int rowSkip; // to keep track of the distance between widgets
    public Map<String, RocketComponentEntryWidget> components = new HashMap<>();

    public RocketSimulatorComponentContainerWidget(Position position, Size size) {
        super(position, size);
        this.setSize(size);
        this.setSelfPosition(position);
        this.setParentPosition(Position.ORIGIN);
    }

    // note that you need the unlocalized text here
    public void addSlotList(
                            String entryName, String localizationKey, RocketComponentEntryWidget entry) {
        int scrollbarPadding = entry.itemList.sliderActive ? HorizontalScrollableListWidget.scrollPaneWidth : 0;

        LabelWidget textWidget = new LabelWidget(
                0,
                0,
                I18n.format(localizationKey),
                () -> {
                    return 0xffffff;
                });

        textWidget.setSelfPosition(new Position(0, rowSkip + rowSeparation + scrollbarPadding));
        this.addWidget(textWidget);
        entry.setSelfPosition(
                new Position(
                        /* this.getSelfPosition().x */
                        /*
                         * replaced with a magic number because i have no idea whats wrong with it, its
                         * correct on the server but its always wrong on the client, its 134 on the client and 9 on the
                         * server
                         */
                        9 + this.getSize().width - entry.getSize().width - 40, // 40 is some space for the shortview
                                                                               // button
                        rowSkip + rowSeparation + scrollbarPadding));

        entry.setSize(new Size(100, 28));
        this.addWidget(entry);
        this.components.put(entryName, entry);

        rowSkip += rowSeparation + scrollbarPadding;
    }

    // slots continue to work when the parent widget is disabled somehow
    public void setPrimary(boolean active) {
        this.setActive(active);
        this.setVisible(active);

        for (RocketComponentEntryWidget entry : this.components.values()) {
            // entry
            // .getSlots()
            // .forEach(
            // x -> {
            // x.setLocked(!active);
            // });
            entry.widgets.forEach(
                    x -> {
                        x.setActive(active);
                        x.setVisible(active);
                    });

            entry.setActive(active);
            entry.setVisible(active);
            entry.setShortView(entry.shortView); // to reset the state of the shortview buttons
        }
    }

    public void RemoveSlotLists() {
        this.clearAllWidgets();
        this.components.clear();
        rowSkip = 0;
    }
}
