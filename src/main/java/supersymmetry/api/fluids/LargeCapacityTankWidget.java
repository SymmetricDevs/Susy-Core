package supersymmetry.api.fluids;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;

/**
 * A {@link TankWidget} that renders the fluid level correctly for very large tanks.
 * <p>
 * GregTech's {@link RenderUtil#drawFluidForGui} computes {@code amount * height / capacity} in int
 * arithmetic, which overflows once {@code amount} exceeds roughly {@code Integer.MAX_VALUE / height}
 * (~36M mB for a 61px tall widget), making the displayed level wrong for most of the fill range.
 * This widget does the scaling in long arithmetic and hands GregTech an already pixel-scaled stack.
 */
public class LargeCapacityTankWidget extends TankWidget {

    private IGuiTexture[] backgroundTextures;
    private IGuiTexture overlay;
    private int syncedCapacity;

    public LargeCapacityTankWidget(IFluidTank fluidTank, int x, int y, int width, int height) {
        super(fluidTank, x, y, width, height);
    }

    @Override
    public TankWidget setBackgroundTexture(IGuiTexture... backgroundTexture) {
        this.backgroundTextures = backgroundTexture;
        return super.setBackgroundTexture(backgroundTexture);
    }

    @Override
    public TankWidget setOverlayTexture(IGuiTexture overlayTexture) {
        this.overlay = overlayTexture;
        return super.setOverlayTexture(overlayTexture);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (isClient) {
            syncedCapacity = fluidTank.getCapacity();
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            // Peek the capacity; the parent keeps its copy private.
            int readerIndex = buffer.readerIndex();
            syncedCapacity = buffer.readVarInt();
            buffer.readerIndex(readerIndex);
        }
        super.readUpdateInfo(id, buffer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTextures != null) {
            for (IGuiTexture texture : backgroundTextures) {
                texture.draw(pos.x, pos.y, size.width, size.height);
            }
        }

        // do not draw fluids if they are handled by JEI - it draws them itself
        if (lastFluidInTank != null && lastFluidInTank.amount > 0 && syncedCapacity > 0 && !gui.isJEIHandled) {
            int drawWidth = size.width - fluidRenderOffset;
            int drawHeight = size.height - fluidRenderOffset;
            // drawFluidForGui shrinks the height by one pixel internally
            int pixelHeight = drawHeight - 1;

            long scaled = (long) lastFluidInTank.amount * pixelHeight / syncedCapacity;
            int pixels = (int) Math.max(1, Math.min(pixelHeight, scaled));

            FluidStack stackToDraw = lastFluidInTank.copy();
            stackToDraw.amount = pixels;

            GlStateManager.disableBlend();
            RenderUtil.drawFluidForGui(stackToDraw, pixelHeight,
                    pos.x + fluidRenderOffset, pos.y + fluidRenderOffset, drawWidth, drawHeight);
            GlStateManager.enableBlend();
        }

        if (overlay != null) {
            overlay.draw(pos.x, pos.y, size.width, size.height);
        }
    }
}
