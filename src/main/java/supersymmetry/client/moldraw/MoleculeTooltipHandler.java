package supersymmetry.client.moldraw;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.lwjgl.opengl.GL11;

import codechicken.lib.gui.GuiDraw;
import gregtech.api.fluids.GTFluid;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.MaterialStack;
import supersymmetry.Supersymmetry;
import supersymmetry.SusyConfig;
import supersymmetry.client.moldraw.data.AlloyData;
import supersymmetry.client.moldraw.data.MoleculeData;
import supersymmetry.client.molecule.Atom;
import supersymmetry.client.molecule.Bond;
import supersymmetry.client.molecule.Circle;
import supersymmetry.client.molecule.Molecule;
import supersymmetry.client.molecule.Parens;
import supersymmetry.integration.jei.JeiModule;

/** Adds molecule structure drawings to ordinary GregTech material and fluid tooltips. */
@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public final class MoleculeTooltipHandler {

    private static final int MAX_DRAW_WIDTH = 170;
    private static Material pendingMaterial;

    private MoleculeTooltipHandler() {}

    @SubscribeEvent
    public static void addTooltip(ItemTooltipEvent event) {
        pendingMaterial = null;
        if (!SusyConfig.enableMoleculeDrawings) return;
        ItemStack stack = event.getItemStack();
        Material material = materialFor(stack);
        if (material == null) return;
        Molecule molecule = MoleculeData.get(material);
        List<AlloyData.Component> alloy = SusyConfig.enableAlloyTooltips ? AlloyData.get(material) : null;
        if (molecule == null && alloy == null && !SusyConfig.enableMoleculeFormulaColors) return;

        List<String> lines = event.getToolTip();
        String formula = material.getChemicalFormula();
        int index = formulaLine(lines, formula);
        if (SusyConfig.enableMoleculeFormulaColors && formula != null && !formula.isEmpty()) {
            if (index >= 0) lines.set(index, colorFormula(formula));
            else {
                index = Math.min(1, lines.size());
                lines.add(index, colorFormula(formula));
            }
        }

        if (molecule == null && alloy == null) return;
        if (SusyConfig.moleculeDrawingsShiftOnly && !GuiScreen.isShiftKeyDown()) {
            lines.add(molecule != null ? "§7Hold Shift to view the molecular structure" :
                    "§7Hold Shift to view the composition");
            return;
        }

        pendingMaterial = material;
    }

    @SubscribeEvent
    public static void drawTooltip(RenderTooltipEvent.PostText event) {
        Material material = pendingMaterial;
        pendingMaterial = null;
        if (!SusyConfig.enableMoleculeDrawings) return;
        if (material == null) material = materialFor(event.getStack());
        if (material == null && event.getStack().isEmpty() && Loader.isModLoaded("jei")) {
            FluidStack hovered = JeiModule.getHoveredFluid();
            if (hovered != null && hovered.getFluid() instanceof GTFluid.GTMaterialFluid)
                material = ((GTFluid.GTMaterialFluid) hovered.getFluid()).getMaterial();
        }
        if (material == null) return;
        Molecule molecule = MoleculeData.get(material);
        List<AlloyData.Component> alloy = SusyConfig.enableAlloyTooltips ? AlloyData.get(material) : null;
        if (molecule == null && alloy == null) return;
        if (SusyConfig.moleculeDrawingsShiftOnly && !GuiScreen.isShiftKeyDown()) return;

        net.minecraft.client.gui.GuiScreen screen = net.minecraft.client.Minecraft.getMinecraft().currentScreen;
        if (screen == null) return;
        int width = molecule != null ? panelWidth(molecule) : panelWidth(alloy);
        int rows = molecule != null ? reservedLines(molecule) : reservedLines(alloy);
        int height = rows * 10 + 4;
        int x = event.getX() + event.getWidth() + 10;
        if (x + width + 4 > screen.width) x = event.getX() - width - 10;
        x = Math.max(4, Math.min(x, screen.width - width - 4));
        int y = Math.max(4, Math.min(event.getY(), screen.height - height - 4));
        GuiDraw.drawRect(x - 3, y - 3, width + 6, height + 6, 0xf0100010);
        if (molecule != null) drawMolecule(molecule, x + 2, y + 3, width - 4, height - 6);
        else drawAlloy(alloy, x + 2, y + 3, width - 4, height - 6);
    }

    private static int panelWidth(Molecule molecule) {
        if (molecule.getAtoms().isEmpty()) return 76;
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        for (Atom atom : molecule.getAtoms()) {
            minX = Math.min(minX, atom.getX());
            maxX = Math.max(maxX, atom.getX());
        }
        return Math.min(MAX_DRAW_WIDTH, Math.max(76, (int) Math.ceil((maxX - minX) * 24f + 36f)));
    }

    private static int panelWidth(List<AlloyData.Component> components) {
        double total = 0;
        for (AlloyData.Component component : components) total += Math.max(0L, component.getAmount());
        int labelWidth = 0;
        for (AlloyData.Component component : components) {
            String formula = component.getMaterial().getChemicalFormula();
            String name = formula == null || formula.isEmpty() ? component.getMaterial().toString() : formula;
            String label = String.format(java.util.Locale.ROOT, "%.1f%% %s",
                    total <= 0 ? 0 : component.getAmount() * 100d / total, name);
            labelWidth = Math.max(labelWidth, GuiDraw.getStringWidth(label));
        }
        return Math.min(MAX_DRAW_WIDTH, Math.max(112, 72 + labelWidth));
    }

    private static int reservedLines(Molecule molecule) {
        if (molecule.getAtoms().isEmpty()) return 3;
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (Atom atom : molecule.getAtoms()) {
            minX = Math.min(minX, atom.getX());
            maxX = Math.max(maxX, atom.getX());
            minY = Math.min(minY, atom.getY());
            maxY = Math.max(maxY, atom.getY());
        }
        float spanX = Math.max(1f, maxX - minX), spanY = Math.max(1f, maxY - minY);
        float scale = Math.min(24f, (MAX_DRAW_WIDTH - 12f) / spanX);
        return Math.min(12, Math.max(3, (int) Math.ceil((spanY * scale + 10f) / 10f)));
    }

    private static int reservedLines(List<AlloyData.Component> components) {
        return Math.min(12, Math.max(6, Math.min(11, components.size()) + 1));
    }

    private static int formulaLine(List<String> lines, String formula) {
        if (formula == null || formula.isEmpty()) return -1;
        for (int i = 0; i < lines.size(); i++) {
            String plain = lines.get(i).replaceAll("§.", "").trim();
            if (plain.equals(formula)) return i;
        }
        return -1;
    }

    private static String colorFormula(String formula) {
        if (formula == null) return "";
        StringBuilder colored = new StringBuilder(formula.length() + 16);
        char activeColor = 'f';
        for (int i = 0; i < formula.length();) {
            char c = formula.charAt(i);
            if (Character.isUpperCase(c)) {
                String symbol = String.valueOf(c);
                if (i + 1 < formula.length() && Character.isLowerCase(formula.charAt(i + 1)))
                    symbol += formula.charAt(i + 1);
                activeColor = elementColor(symbol);
                colored.append('\u00a7').append(activeColor).append(symbol);
                i += symbol.length();
            } else {
                if (Character.isDigit(c)) colored.append('\u00a7').append(activeColor);
                colored.append(c);
                i++;
            }
        }
        return colored.append("§r").toString();
    }

    private static char elementColor(String symbol) {
        switch (symbol) {
            case "C":
                return '7';
            case "O":
                return 'c';
            case "N":
                return '9';
            case "H":
                return 'f';
            case "S":
                return 'e';
            case "P":
                return '6';
            case "F":
            case "Cl":
                return 'a';
            case "Br":
                return '4';
            case "I":
                return '5';
            default:
                return 'e';
        }
    }

    private static Material materialFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        MaterialStack itemMaterial = OreDictUnifier.getMaterial(stack);
        if (itemMaterial != null) return itemMaterial.material;

        FluidStack contained = FluidUtil.getFluidContained(stack);
        if (contained == null) return null;
        if (contained.getFluid() instanceof GTFluid.GTMaterialFluid materialFluid)
            return materialFluid.getMaterial();
        return null;
    }

    private static void drawMolecule(Molecule molecule, int x, int y, int maxWidth, int maxHeight) {
        if (molecule.getAtoms().isEmpty() || maxWidth <= 0) return;
        Map<Integer, Point> positions = projectedPositions(molecule);
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (Atom atom : molecule.getAtoms()) {
            Point point = positions.get(atom.getIndex());
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }
        float spanX = Math.max(1f, maxX - minX), spanY = Math.max(1f, maxY - minY);
        float scale = Math.min(24f, Math.min((maxWidth - 12f) / spanX, (maxHeight - 10f) / spanY));
        scale = Math.max(1f, scale);
        int centerX = x + Math.min(maxWidth, (int) (spanX * scale + 8)) / 2;
        int centerY = y + maxHeight / 2;

        for (Bond bond : molecule.getBonds()) {
            Atom a = atomAt(molecule, bond.getA()), b = atomAt(molecule, bond.getB());
            if (a == null || b == null) continue;
            Point pa = positions.get(a.getIndex()), pb = positions.get(b.getIndex());
            int ax = centerX + Math.round((pa.x - (minX + maxX) / 2f) * scale);
            int ay = centerY - Math.round((pa.y - (minY + maxY) / 2f) * scale);
            int bx = centerX + Math.round((pb.x - (minX + maxX) / 2f) * scale);
            int by = centerY - Math.round((pb.y - (minY + maxY) / 2f) * scale);
            String[] styles = bond.getLines();
            for (int line = 0; line < styles.length; line++) {
                float offset = bond.isCentered() ? (line - (styles.length - 1) / 2f) * 2.5f : line * 2.5f;
                drawBondStyle(ax, ay, bx, by, offset, styles[line]);
            }
        }
        for (Atom atom : molecule.getAtoms()) {
            Point point = positions.get(atom.getIndex());
            int ax = centerX + Math.round((point.x - (minX + maxX) / 2f) * scale);
            int ay = centerY - Math.round((point.y - (minY + maxY) / 2f) * scale);
            if (!atom.isInvisible()) drawElement(atom.getElement(), ax, ay);
            if (atom.getAbove() != null) drawElement(atom.getAbove(), ax, ay - 9);
            if (atom.getBelow() != null) drawElement(atom.getBelow(), ax, ay + 7);
            if (atom.getRight() != null) {
                String label = atom.getRight().getDisplaySymbol();
                drawElement(atom.getRight(), ax + GuiDraw.getStringWidth(atom.getElement().getDisplaySymbol()) / 2 + 2,
                        ay);
            }
            if (atom.getLeft() != null) {
                String label = atom.getLeft().getDisplaySymbol();
                drawElement(atom.getLeft(), ax - GuiDraw.getStringWidth(label) -
                        GuiDraw.getStringWidth(atom.getElement().getDisplaySymbol()) / 2 - 2, ay);
            }
        }
        for (Object element : molecule.getContents()) {
            if (element instanceof Parens)
                drawParens((Parens) element, positions, centerX, centerY, minX, maxX, minY, maxY, scale);
            else if (element instanceof Circle)
                drawCircle((Circle) element, positions, centerX, centerY, minX, maxX, minY, maxY, scale);
        }
    }

    private static Map<Integer, Point> projectedPositions(Molecule molecule) {
        Map<Integer, float[]> centers = new HashMap<Integer, float[]>();
        Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
        for (Atom atom : molecule.getAtoms()) {
            int group = atom.getSpinGroup();
            if (group < 0 || group >= molecule.getSpinGroups().size()) continue;
            float[] center = centers.computeIfAbsent(group, ignored -> new float[2]);
            center[0] += atom.getX();
            center[1] += atom.getZ();
            counts.put(group, counts.getOrDefault(group, 0) + 1);
        }
        for (Map.Entry<Integer, float[]> entry : centers.entrySet()) {
            int count = counts.get(entry.getKey());
            entry.getValue()[0] /= count;
            entry.getValue()[1] /= count;
        }
        long now = System.currentTimeMillis();
        Map<Integer, Point> result = new HashMap<Integer, Point>();
        for (Atom atom : molecule.getAtoms()) {
            float px = atom.getX();
            int group = atom.getSpinGroup();
            if (centers.containsKey(group)) {
                float frequency = molecule.getSpinGroups().get(group);
                if (frequency > 0 && Float.isFinite(frequency)) {
                    float[] center = centers.get(group);
                    long period = Math.max(1L, (long) (1000f / frequency));
                    float angle = (now % period) / (float) period * (float) (Math.PI * 2);
                    float dx = atom.getX() - center[0], dz = atom.getZ() - center[1];
                    px = center[0] + dx * (float) Math.cos(angle) + dz * (float) Math.sin(angle);
                }
            }
            result.put(atom.getIndex(), new Point(px, atom.getY()));
        }
        return result;
    }

    private static void drawElement(supersymmetry.client.molecule.Element element, int x, int y) {
        String label = element.getDisplaySymbol();
        int width = GuiDraw.getStringWidth(label);
        GuiDraw.drawString(label, x - width / 2, y - 4, element.getColor() & 0xffffff, true);
    }

    private static void drawBondStyle(int ax, int ay, int bx, int by, float offset, String style) {
        float dx = bx - ax, dy = by - ay;
        float length = (float) Math.max(1, Math.hypot(dx, dy));
        float nx = -dy / length, ny = dx / length;
        int x1 = Math.round(ax + nx * offset), y1 = Math.round(ay + ny * offset);
        int x2 = Math.round(bx + nx * offset), y2 = Math.round(by + ny * offset);
        if ("dotted".equals(style)) {
            int segments = Math.max(1, (int) (length / 4));
            for (int i = 0; i < segments; i += 2) {
                float a = i / (float) segments, b = Math.min(1f, (i + 1f) / segments);
                GuiDraw.drawLine(Math.round(x1 + (x2 - x1) * a), Math.round(y1 + (y2 - y1) * a),
                        Math.round(x1 + (x2 - x1) * b), Math.round(y1 + (y2 - y1) * b), 1f, 0xffdddddd);
            }
        } else if ("thick".equals(style)) {
            GuiDraw.drawLine(x1, y1, x2, y2, 3f, 0xffdddddd);
        } else if ("inward".equals(style) || "outward".equals(style)) {
            for (int i = 1; i < 7; i++) {
                float t = i / 7f;
                float half = ("outward".equals(style) ? t : 1f - t) * 3f;
                int cx = Math.round(x1 + (x2 - x1) * t), cy = Math.round(y1 + (y2 - y1) * t);
                GuiDraw.drawLine(Math.round(cx - nx * half), Math.round(cy - ny * half),
                        Math.round(cx + nx * half), Math.round(cy + ny * half), 1f, 0xffdddddd);
            }
        } else GuiDraw.drawLine(x1, y1, x2, y2, 1f, 0xffdddddd);
    }

    private static void drawParens(Parens parens, Map<Integer, Point> positions, int centerX, int centerY,
                                   float minX, float maxX, float minY, float maxY, float scale) {
        int left = Integer.MAX_VALUE, right = Integer.MIN_VALUE, top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE;
        for (int index : parens.getAtoms()) {
            Point point = positions.get(index);
            if (point == null) continue;
            int px = centerX + Math.round((point.x - (minX + maxX) / 2f) * scale);
            int py = centerY - Math.round((point.y - (minY + maxY) / 2f) * scale);
            left = Math.min(left, px);
            right = Math.max(right, px);
            top = Math.min(top, py);
            bottom = Math.max(bottom, py);
        }
        if (left == Integer.MAX_VALUE) return;
        top -= 6;
        bottom += 6;
        GuiDraw.drawLine(left - 5, top, left - 2, top, 1f, 0xffdddddd);
        GuiDraw.drawLine(left - 5, top, left - 5, bottom, 1f, 0xffdddddd);
        GuiDraw.drawLine(left - 5, bottom, left - 2, bottom, 1f, 0xffdddddd);
        GuiDraw.drawLine(right + 2, top, right + 5, top, 1f, 0xffdddddd);
        GuiDraw.drawLine(right + 5, top, right + 5, bottom, 1f, 0xffdddddd);
        GuiDraw.drawLine(right + 2, bottom, right + 5, bottom, 1f, 0xffdddddd);
        if (!parens.getSup().isEmpty()) GuiDraw.drawString(parens.getSup(), right + 6, top - 2, 0xffffff, true);
        if (!parens.getSub().isEmpty()) GuiDraw.drawString(parens.getSub(), right + 6, bottom - 4, 0xffffff, true);
    }

    private static void drawCircle(Circle circle, Map<Integer, Point> positions, int centerX, int centerY,
                                   float minX, float maxX, float minY, float maxY, float scale) {
        float cx = 0, cy = 0;
        int count = 0;
        for (int index : circle.getAtoms()) {
            Point point = positions.get(index);
            if (point != null) {
                cx += point.x;
                cy += point.y;
                count++;
            }
        }
        if (count == 0) return;
        cx /= count;
        cy /= count;
        int segments = 48;
        int lastX = 0, lastY = 0;
        for (int i = 0; i <= segments; i++) {
            double angle = i * Math.PI * 2 / segments;
            float unitX = (float) Math.cos(angle), unitY = (float) Math.sin(angle);
            float px = cx + circle.getXX() * unitX + circle.getXY() * unitY;
            float py = cy + circle.getYX() * unitX + circle.getYY() * unitY;
            int sx = centerX + Math.round((px - (minX + maxX) / 2f) * scale);
            int sy = centerY - Math.round((py - (minY + maxY) / 2f) * scale);
            if (i > 0) GuiDraw.drawLine(lastX, lastY, sx, sy, 1f, 0xffdddddd);
            lastX = sx;
            lastY = sy;
        }
    }

    private static final class Point {

        final float x, y;

        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private static Atom atomAt(Molecule molecule, int index) {
        for (Atom atom : molecule.getAtoms()) if (atom.getIndex() == index) return atom;
        return null;
    }

    private static void drawAlloy(List<AlloyData.Component> components, int x, int y, int maxWidth, int maxHeight) {
        if (components.isEmpty() || maxWidth < 60) return;
        double total = 0;
        for (AlloyData.Component component : components) total += Math.max(0L, component.getAmount());
        if (total <= 0) return;

        int radius = Math.min(24, (maxHeight - 4) / 2);
        int centerX = x + radius + 2, centerY = y + maxHeight / 2;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        boolean hadDepth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean hadCull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy > radius * radius) continue;
                double angle = Math.atan2(dx, -dy);
                if (angle < 0) angle += Math.PI * 2;
                double fraction = angle / (Math.PI * 2);
                double accumulated = 0;
                for (AlloyData.Component component : components) {
                    accumulated += Math.max(0L, component.getAmount()) / total;
                    if (fraction <= accumulated) {
                        int color = component.getMaterial().getMaterialRGB();
                        addPixel(buffer, centerX + dx, centerY + dy, color);
                        break;
                    }
                }
            }
        }
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        if (hadCull) GlStateManager.enableCull();
        if (hadDepth) GlStateManager.enableDepth();
        GlStateManager.resetColor();

        int shown = Math.min(components.size(), 11);
        int labelX = x + radius * 2 + 8;
        int maxLabelWidth = Math.max(0, maxWidth - radius * 2 - 18);
        int rowY = y + 1;
        for (int i = 0; i < shown; i++) {
            AlloyData.Component component = components.get(i);
            int color = 0xff000000 | (component.getMaterial().getMaterialRGB() & 0xffffff);
            GuiDraw.drawRect(labelX, rowY + 2, 4, 4, color);
            double percent = component.getAmount() * 100d / total;
            String formula = component.getMaterial().getChemicalFormula();
            String name = formula == null || formula.isEmpty() ? component.getMaterial().toString() : formula;
            String label = String.format(java.util.Locale.ROOT, "%.1f%% %s", percent, name);
            label = GuiDraw.fontRenderer.trimStringToWidth(label, maxLabelWidth);
            GuiDraw.drawString(label, labelX + 7, rowY, 0xffffff, true);
            rowY += 10;
        }
        if (shown < components.size()) {
            GuiDraw.drawString("+" + (components.size() - shown) + " more", labelX + 7, rowY, 0xaaaaaa, true);
        }
    }

    private static void addPixel(BufferBuilder buffer, int x, int y, int color) {
        int red = color >> 16 & 0xff, green = color >> 8 & 0xff, blue = color & 0xff;
        double z = 310;
        buffer.pos(x, y, z).color(red, green, blue, 255).endVertex();
        buffer.pos(x + 1, y, z).color(red, green, blue, 255).endVertex();
        buffer.pos(x + 1, y + 1, z).color(red, green, blue, 255).endVertex();
        buffer.pos(x, y + 1, z).color(red, green, blue, 255).endVertex();
    }
}
