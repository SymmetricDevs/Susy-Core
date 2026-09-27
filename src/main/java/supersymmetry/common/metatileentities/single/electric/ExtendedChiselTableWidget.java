package supersymmetry.common.metatileentities.single.electric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.chisel.ExtendedChiselItem;
import supersymmetry.common.metatileentities.single.electric.ExtendedChiselFolderTree.FolderNode;
import supersymmetry.common.metatileentities.single.electric.ExtendedChiselFolderTree.ItemLeaf;
import team.chisel.api.block.ICarvable;
import team.chisel.api.block.VariationData;

/**
 * Extended Chisel Maker GUI body, styled after the Marking Maker's layout:
 *
 * <ul>
 * <li><b>Left sidebar</b> (built as native GT widgets in {@code createUI}): input slot, result slot
 * and a GregTech work-enable power toggle at the bottom-left.</li>
 * <li><b>Middle grid</b>: a recessed GT panel listing every <em>deduplicated</em> item of the active
 * folder (or of the search match within that folder), scrolled with its own scrollbar.</li>
 * <li><b>Right accordeon</b>: a vertical folder tree of the items' Extended Location Identifiers.
 * Clicking a folder makes it the active folder and expands/collapses it downward.</li>
 * </ul>
 *
 * <p>
 * The search bar (a native {@code TextFieldWidget2} above this widget) feeds its text into
 * {@link #setSearchQuery(String)}. A search is active whenever the query has at least one keyword;
 * keywords are the query split on spaces and combined with AND. An item matches when <em>every</em>
 * lowercase keyword is a substring of the item's {@linkplain ExtendedChiselItem#getSearchableText()
 * searchable text} (its registered id plus the whole of every location path).
 * </p>
 *
 * <p>
 * All geometry, drawing and mouse handling are client only; picking an item sends its unique item id
 * to the server via a client action, which selects it as the machine's output.
 * </p>
 */
public class ExtendedChiselTableWidget extends Widget {

    private static final int ACTION_SELECT = 1;

    // ArchitectureCraft palette
    private static final int DEFAULT_TEXT_COLOR = 0x404040;
    private static final int ACCENT_TEXT_COLOR = 0xFF2A87A3;
    private static final int TREE_BG = 0xE6141400;
    private static final int TREE_BG_HOVER = 0xE62A2A00;
    private static final int TREE_BG_SELECTED = 0x602A87A3;

    // Middle grid (ArchitectureCraft shape menu cell size)
    private static final int CELL = 28;
    private static final int CELL_MARGIN = 4;
    private static final int PAD = 4;

    // Right accordeon (ArchitectureCraft page menu style)
    private static final int ROW_H = 12;
    private static final int INDENT = 8;
    private static final int SCROLL_W = 4;

    // Region split (widget spans middle grid + right accordeon)
    public static final int TREE_WIDTH = 150;
    private static final int GRID_END_GAP = 6;

    private final ExtendedChiselFolderTree tree;
    private FolderNode activeFolder;
    private final Set<FolderNode> expanded = new HashSet<>();

    private String searchQuery = "";
    private List<ItemLeaf> gridItems = new ArrayList<>();
    private int gridScrollRow = 0;
    private int gridCols = 4;
    private int gridRows = 4;

    private List<FolderNode> treeRows = new ArrayList<>();
    private int treeScroll = 0;

    private boolean draggingGridScroll = false;
    private boolean draggingTreeScroll = false;
    private int dragGrabOffset = 0;

    @Nullable private ItemLeaf hoveredLeaf;
    @Nullable private Runnable clearSearchCallback;

    private static TextureArea panel() {
        return GuiTextures.DISPLAY;
    }

    public ExtendedChiselTableWidget(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.tree = new ExtendedChiselFolderTree();
        this.activeFolder = tree.getRoot();
        computeGeometry();
        rebuildTreeRows();
        rebuildGrid();
    }

    // ------------------------------------------------------------------ data

    private void computeGeometry() {
        this.gridCols = Math.max(1, (getSize().width - TREE_WIDTH - GRID_END_GAP - 2 * PAD) / CELL);
        this.gridRows = Math.max(1, (getSize().height - 2 * PAD) / CELL);
    }

    private void rebuildGrid() {
        List<ItemLeaf> items = tree.getAllItems(activeFolder);
        if (isSearchActive()) {
            List<String> keywords = searchKeywords();
            items.removeIf(item -> !matches(item, keywords));
        }
        this.gridItems = items;
        int extra = (int) Math.ceil(gridItems.size() / (double) gridCols) - gridRows;
        this.gridScrollRow = Math.max(0, Math.min(this.gridScrollRow, Math.max(0, extra)));
    }

    private void rebuildTreeRows() {
        this.treeRows = new ArrayList<>();
        buildRows(tree.getRoot());
        int maxScroll = Math.max(0, treeRows.size() - treeVisibleRows());
        this.treeScroll = Math.max(0, Math.min(this.treeScroll, maxScroll));
    }

    private void buildRows(FolderNode node) {
        for (FolderNode child : node.getFolderChildren()) {
            treeRows.add(child);
            if (expanded.contains(child)) {
                buildRows(child);
            }
        }
    }

    private void setActiveFolder(FolderNode folder) {
        boolean changed = this.activeFolder != folder;
        this.activeFolder = folder;
        this.gridScrollRow = 0;
        if (changed) {
            // A new folder shows all its items; any active search no longer applies.
            this.searchQuery = "";
            if (clearSearchCallback != null) {
                clearSearchCallback.run();
            }
        }
        rebuildGrid();
        rebuildTreeRows();
    }

    /** Sets a callback invoked when the active folder changes, so the UI can clear its search bar. */
    public void setClearSearchCallback(@Nullable Runnable callback) {
        this.clearSearchCallback = callback;
    }

    private void expandOrCollapse(FolderNode folder) {
        if (!expanded.add(folder)) {
            expanded.remove(folder);
        }
        rebuildTreeRows();
    }

    // ------------------------------------------------------------------ search

    /** The current search query, as typed into the GUI's search bar. */
    public String getSearchQuery() {
        return searchQuery;
    }

    /** Called by the search bar's setter; rebuilds the grid when the query changes. */
    public void setSearchQuery(String query) {
        String q = query == null ? "" : query;
        if (Objects.equals(this.searchQuery, q)) {
            return;
        }
        this.searchQuery = q;
        this.gridScrollRow = 0;
        rebuildGrid();
    }

    public boolean isSearchActive() {
        return !searchQuery.trim().isEmpty();
    }

    private List<String> searchKeywords() {
        List<String> out = new ArrayList<>();
        for (String part : searchQuery.toLowerCase(Locale.ROOT).split(" ")) {
            if (!part.isEmpty()) {
                out.add(part);
            }
        }
        return out;
    }

    private static boolean matches(ItemLeaf leaf, List<String> keywords) {
        String haystack = leaf.getItem().getSearchableText();
        for (String keyword : keywords) {
            if (!haystack.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------ geometry

    private int gridLeft() {
        return getPosition().x;
    }

    private int gridTop() {
        return getPosition().y;
    }

    private int gridWidth() {
        return gridCols * CELL + 2 * PAD;
    }

    private int treeLeft() {
        return gridLeft() + gridWidth() + GRID_END_GAP;
    }

    private int treeVisibleRows() {
        return Math.max(1, getSize().height / ROW_H);
    }

    private int treeWidth() {
        int w = getPosition().x + getSize().width - treeLeft();
        return Math.max(1, w);
    }

    @SideOnly(Side.CLIENT)
    private int gridTotalRows() {
        return (int) Math.ceil(gridItems.size() / (double) gridCols);
    }

    @SideOnly(Side.CLIENT)
    private boolean gridScrollable() {
        return gridTotalRows() > gridRows;
    }

    @SideOnly(Side.CLIENT)
    private int maxTreeScroll() {
        return Math.max(0, treeRows.size() - treeVisibleRows());
    }

    @SideOnly(Side.CLIENT)
    private int gridTrackX() {
        return gridLeft() + gridWidth() - SCROLL_W - 1;
    }

    @SideOnly(Side.CLIENT)
    private int gridTrackTop() {
        return gridTop() + 1;
    }

    @SideOnly(Side.CLIENT)
    private int gridTrackH() {
        return getSize().height - 2;
    }

    @SideOnly(Side.CLIENT)
    private int gridBarH(int totalRows) {
        return Math.max(8, gridTrackH() * gridRows / totalRows);
    }

    @SideOnly(Side.CLIENT)
    private int treeTrackX() {
        return treeLeft() + treeWidth() - SCROLL_W - 1;
    }

    // ------------------------------------------------------------------ rendering

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        this.hoveredLeaf = null;
        drawMiddleGrid(mouseX, mouseY);
        drawRightTree(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (hoveredLeaf != null) {
            ItemStack stack = tree.getItemStack(hoveredLeaf);
            if (stack != null && !stack.isEmpty()) {
                List<String> tooltip = new ArrayList<>();
                tooltip.add(stack.getDisplayName());
                String variant = getChiselVariantName(stack);
                if (variant != null && !variant.isEmpty()) {
                    tooltip.add(variant);
                }
                drawHoveringText(ItemStack.EMPTY, tooltip, 300, mouseX, mouseY);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawMiddleGrid(int mouseX, int mouseY) {
        int ox = gridLeft();
        int oy = gridTop();
        int gw = gridWidth();
        int gh = getSize().height;

        drawSolidRect(ox, oy, gw, gh, 0xFF2D2D2D);
        panel().draw(ox, oy, gw, gh);

        for (int i = gridScrollRow * gridCols; i < gridItems.size(); i++) {
            int idx = i - gridScrollRow * gridCols;
            int col = idx % gridCols;
            int row = idx / gridCols;
            if (row >= gridRows) {
                break;
            }
            int cx = ox + PAD + col * CELL;
            int cy = oy + PAD + row * CELL;
            ItemLeaf hovered = drawItemCell(gridItems.get(i), cx, cy, mouseX, mouseY);
            if (hovered != null) {
                this.hoveredLeaf = hovered;
            }
        }

        // grid scrollbar
        int visibleRows = gridRows;
        int totalRows = (int) Math.ceil(gridItems.size() / (double) gridCols);
        if (totalRows > visibleRows) {
            int trackH = gh - 2;
            int barH = Math.max(8, trackH * visibleRows / totalRows);
            int barY = oy + 1 + (trackH - barH) * gridScrollRow / (totalRows - visibleRows);
            drawSolidRect(ox + gw - SCROLL_W - 1, oy + 1, SCROLL_W, trackH, 0xFF000000);
            drawSolidRect(ox + gw - SCROLL_W - 1, barY, SCROLL_W, barH, 0xFF8C8C8C);
        }
    }

    @SideOnly(Side.CLIENT)
    private @Nullable ItemLeaf drawItemCell(ItemLeaf leaf, int x, int y, int mouseX, int mouseY) {
        boolean selected = isSelected(leaf);
        boolean hover = mouseOverIn(x, y, CELL, CELL, mouseX, mouseY);
        if (selected) {
            drawSolidRect(x, y, CELL, CELL, 0xFF2A87A3);
        } else if (hover) {
            drawSolidRect(x, y, CELL, CELL, 0xFF33545E);
        }
        ItemStack icon = tree.getItemStack(leaf);
        if (icon != null) {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableDepth();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(icon, x + (CELL - 16) / 2, y + (CELL - 16) / 2);
            GlStateManager.disableDepth();
            RenderHelper.disableStandardItemLighting();
        }
        return hover ? leaf : null;
    }

    @SideOnly(Side.CLIENT)
    private void drawRightTree(int mouseX, int mouseY) {
        int ox = treeLeft();
        int oy = getPosition().y;
        int tw = treeWidth();
        int th = getSize().height;

        drawSolidRect(ox, oy, tw, th, TREE_BG);

        net.minecraft.client.gui.FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int visible = treeVisibleRows();
        for (int i = treeScroll; i < treeRows.size(); i++) {
            int row = i - treeScroll;
            if (row >= visible) {
                break;
            }
            FolderNode node = treeRows.get(i);
            int ry = oy + row * ROW_H;
            boolean hover = mouseOverIn(ox, ry, tw, ROW_H, mouseX, mouseY);
            if (node.equals(activeFolder)) {
                drawSolidRect(ox, ry, tw, ROW_H, TREE_BG_SELECTED);
            } else if (hover) {
                drawSolidRect(ox, ry, tw, ROW_H, TREE_BG_HOVER);
            }
            int labelX = ox + 2 + indentOf(node) * INDENT;
            boolean ex = expanded.contains(node);
            font.drawString(ex ? "\u25be" : "\u25b8", labelX, ry + 2, 0xCBB17B);
            String name = node.name;
            int maxW = tw - (labelX - ox) - 4;
            if (font.getStringWidth(name) > maxW) {
                name = font.trimStringToWidth(name, maxW - 2) + "\u2026";
            }
            if (node == activeFolder) {
                font.drawString(name, labelX + 10, ry + 2, ACCENT_TEXT_COLOR);
            } else {
                font.drawString(name, labelX + 10, ry + 2, 0xE8D5AC);
            }
        }

        // tree scrollbar
        int maxScroll = Math.max(0, treeRows.size() - treeVisibleRows());
        if (maxScroll > 0) {
            int trackH = getSize().height - 2;
            int barH = Math.max(8, trackH * treeVisibleRows() / treeRows.size());
            int barY = oy + 1 + (trackH - barH) * treeScroll / maxScroll;
            drawSolidRect(ox + tw - SCROLL_W - 1, oy + 1, SCROLL_W, trackH, 0xFF000000);
            drawSolidRect(ox + tw - SCROLL_W - 1, barY, SCROLL_W, barH, 0xFF8C7550);
        }
    }

    private int indentOf(FolderNode node) {
        int d = 0;
        for (FolderNode n = node; n != null && n.parent != null; n = n.parent) {
            d++;
        }
        return Math.max(0, d - 1);
    }

    @SideOnly(Side.CLIENT)
    private boolean isSelected(ItemLeaf leaf) {
        MetaTileEntityExtendedChiselMaker mte = getMte();
        return mte != null && Objects.equals(mte.getSelectedItemId(), leaf.itemId);
    }

    // ------------------------------------------------------------------ interaction

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (getSize() == null || !isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        if (button == 0) {
            if (withinGridScrollTrack(mouseX, mouseY)) {
                startGridScrollDrag(mouseY);
                return true;
            }
            if (withinTreeScrollTrack(mouseX, mouseY)) {
                startTreeScrollDrag(mouseY);
                return true;
            }
        }
        if (mouseX >= treeLeft()) {
            return clickTree(mouseX, mouseY);
        }
        return clickGrid(mouseX, mouseY);
    }

    @SideOnly(Side.CLIENT)
    private boolean withinGridScrollTrack(int mouseX, int mouseY) {
        if (!gridScrollable()) {
            return false;
        }
        int tx = gridTrackX();
        return mouseX >= tx && mouseX < tx + SCROLL_W && mouseY >= gridTrackTop() &&
                mouseY < gridTrackTop() + gridTrackH();
    }

    @SideOnly(Side.CLIENT)
    private boolean withinTreeScrollTrack(int mouseX, int mouseY) {
        if (maxTreeScroll() <= 0) {
            return false;
        }
        int tx = treeTrackX();
        return mouseX >= tx && mouseX < tx + SCROLL_W && mouseY >= gridTrackTop() &&
                mouseY < gridTrackTop() + gridTrackH();
    }

    @SideOnly(Side.CLIENT)
    private void startGridScrollDrag(int mouseY) {
        int total = gridTotalRows();
        int barH = gridBarH(total);
        int barTop = gridTrackTop() + (gridTrackH() - barH) * gridScrollRow / (total - gridRows);
        this.draggingGridScroll = true;
        this.draggingTreeScroll = false;
        this.dragGrabOffset = (mouseY >= barTop && mouseY <= barTop + barH) ? mouseY - barTop : 0;
        updateGridScroll(mouseY);
    }

    @SideOnly(Side.CLIENT)
    private void updateGridScroll(int mouseY) {
        int total = gridTotalRows();
        int barH = gridBarH(total);
        int usable = gridTrackH() - barH;
        int max = total - gridRows;
        int top = Math.max(gridTrackTop(), Math.min(mouseY - dragGrabOffset, gridTrackTop() + usable));
        int scroll = usable > 0 ? (int) Math.round((top - gridTrackTop()) * (double) max / usable) : 0;
        this.gridScrollRow = Math.max(0, Math.min(scroll, max));
    }

    @SideOnly(Side.CLIENT)
    private void startTreeScrollDrag(int mouseY) {
        int max = maxTreeScroll();
        int barH = Math.max(8, gridTrackH() * treeVisibleRows() / treeRows.size());
        int barTop = gridTrackTop() + (gridTrackH() - barH) * treeScroll / max;
        this.draggingTreeScroll = true;
        this.draggingGridScroll = false;
        this.dragGrabOffset = (mouseY >= barTop && mouseY <= barTop + barH) ? mouseY - barTop : 0;
        updateTreeScroll(mouseY);
    }

    @SideOnly(Side.CLIENT)
    private void updateTreeScroll(int mouseY) {
        int max = maxTreeScroll();
        int barH = Math.max(8, gridTrackH() * treeVisibleRows() / treeRows.size());
        int usable = gridTrackH() - barH;
        int top = Math.max(gridTrackTop(), Math.min(mouseY - dragGrabOffset, gridTrackTop() + usable));
        int scroll = usable > 0 ? (int) Math.round((top - gridTrackTop()) * (double) max / usable) : 0;
        this.treeScroll = Math.max(0, Math.min(scroll, max));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (button == 0 && draggingGridScroll) {
            updateGridScroll(mouseY);
            return true;
        }
        if (button == 0 && draggingTreeScroll) {
            updateTreeScroll(mouseY);
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0 && (draggingGridScroll || draggingTreeScroll)) {
            draggingGridScroll = false;
            draggingTreeScroll = false;
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private boolean clickGrid(int mouseX, int mouseY) {
        int ox = gridLeft();
        int oy = gridTop();
        if (mouseX < ox || mouseY < oy) {
            return false;
        }
        int col = (mouseX - ox - PAD) / CELL;
        int row = (mouseY - oy - PAD) / CELL;
        int index = gridScrollRow * gridCols + row * gridCols + col;
        if (col >= 0 && row >= 0 && col < gridCols && index >= 0 && index < gridItems.size()) {
            selectItem(gridItems.get(index));
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private boolean clickTree(int mouseX, int mouseY) {
        int ox = treeLeft();
        int oy = getPosition().y;
        int row = (mouseY - oy) / ROW_H;
        int index = treeScroll + row;
        if (row < 0 || index < 0 || index >= treeRows.size()) {
            return clickTreeBackground();
        }
        FolderNode node = treeRows.get(index);
        if (node.equals(activeFolder)) {
            // Deselect: back to the root, which shows every item.
            setActiveFolder(tree.getRoot());
        } else {
            setActiveFolder(node);
        }
        expandOrCollapse(node);
        playButtonClickSound();
        return true;
    }

    @SideOnly(Side.CLIENT)
    private boolean clickTreeBackground() {
        if (!activeFolder.equals(tree.getRoot())) {
            // Deselect: back to the root, which shows every item.
            setActiveFolder(tree.getRoot());
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return false;
        }
        if (mouseX >= treeLeft()) {
            int max = Math.max(0, treeRows.size() - treeVisibleRows());
            if (max > 0 && wheelDelta != 0) {
                this.treeScroll = Math.max(0, Math.min(this.treeScroll - (wheelDelta > 0 ? 1 : -1), max));
                return true;
            }
            return false;
        }
        int totalRows = (int) Math.ceil(gridItems.size() / (double) gridCols);
        int max = Math.max(0, totalRows - gridRows);
        if (max > 0 && wheelDelta != 0) {
            this.gridScrollRow = Math.max(0, Math.min(this.gridScrollRow - (wheelDelta > 0 ? 1 : -1), max));
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private void selectItem(ItemLeaf leaf) {
        writeClientAction(ACTION_SELECT, buf -> buf.writeString(leaf.itemId));
        playButtonClickSound();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == ACTION_SELECT) {
            String itemId = buffer.readString(Short.MAX_VALUE);
            MetaTileEntityExtendedChiselMaker mte = getMte();
            if (mte != null) {
                mte.setSelectedItemId(itemId);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    @Nullable private MetaTileEntityExtendedChiselMaker getMte() {
        if (gui != null && gui.holder instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) gui.holder).getMetaTileEntity();
            if (mte instanceof MetaTileEntityExtendedChiselMaker) {
                return (MetaTileEntityExtendedChiselMaker) mte;
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    private static boolean mouseOverIn(int x, int y, int w, int h, int mx, int my) {
        return mx >= x && my >= y && x + w > mx && y + h > my;
    }

    @SideOnly(Side.CLIENT)
    private static String getChiselVariantName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        Block block = Block.getBlockFromItem(stack.getItem());
        if (!(block instanceof ICarvable)) {
            return null;
        }
        try {
            ICarvable carvable = (ICarvable) block;
            int index = carvable.getVariationIndex(block.getStateFromMeta(stack.getMetadata()));
            VariationData data = carvable.getVariationData(index);
            if (data == null || data.name == null || data.name.isEmpty()) {
                return null;
            }
            // Chisel lang files name variations with keys like tile.chisel.andesite.braid.desc.1.
            String key = block.getTranslationKey() + "." + data.name + ".desc.1";
            return I18n.hasKey(key) ? I18n.format(key) : data.name;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
