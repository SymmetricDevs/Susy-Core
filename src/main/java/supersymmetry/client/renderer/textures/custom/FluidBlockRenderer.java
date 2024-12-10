package supersymmetry.client.renderer.textures.custom;

import codechicken.lib.texture.TextureUtils;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FluidBlockRenderer {

    public static final FluidBlockRenderer INSTANCE = new FluidBlockRenderer();
    private static final float ALPHA = 1;

    public FluidBlockRenderer() {
    }

    private static Vec3d v3d(Vec3i pos) { // TODO: remove this?
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int getPackedLightmapCoords(IBlockAccess world, BlockPos pos) {
        int lightThis = world.getCombinedLight(pos, 0);
        int lightUp = world.getCombinedLight(pos.up(), 0);
        int lightThisBase = lightThis & 255;
        int lightUpBase = lightUp & 255;
        int lightThisExt = lightThis >> 16 & 255;
        int lightUpExt = lightUp >> 16 & 255;
        return (Math.max(lightThisBase, lightUpBase)) |
                ((Math.max(lightThisExt, lightUpExt)) << 16);
    }

    // TODO is there a better way to do this???
    private static int off(EnumFacing facing, int index) {
        byte x = switch (facing) {
            case NORTH -> 0b0001;
            case EAST -> 0b0111;
            case SOUTH -> 0b1110;
            case WEST -> 0b1000;
            default -> 0;
        };
        return ((x >> index) & 1);
    }

    public void renderFluidFace(IBlockAccess world, BlockPos pos, EnumFacing facing, double fluidHeight, FluidStack stack, BufferBuilder buffer, Vec3d translation) {

        // local variables
        Fluid fluid = stack.getFluid();
        TextureAtlasSprite sprite = TextureUtils.getTexture(fluid.getStill(stack));

        int color = fluid.getColor(stack);
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        double d0 = translation.x;
        double d1 = translation.y;
        double d2 = translation.z;

        int packedLightmapCoords = getPackedLightmapCoords(world, pos);
        int lightmapX = packedLightmapCoords >> 16 & 65535;
        int lightmapY = packedLightmapCoords & 65535;

        double minU, maxU, minV, maxV;

        // :rendering:
        switch (facing) {
            case NORTH, EAST, SOUTH, WEST:
                minU = sprite.getMinU();
                maxU = sprite.getMaxU();
                minV = sprite.getMinV();
                maxV = sprite.getInterpolatedV(fluidHeight * 16);

                buffer.pos(d0 + off(facing, 0), d1 + 0.0D, d2 + off(facing, 1)).color(red, green, blue, ALPHA).tex(minU, minV).lightmap(lightmapX, lightmapY).endVertex();
                buffer.pos(d0 + off(facing, 2), d1 + 0.0D, d2 + off(facing, 3)).color(red, green, blue, ALPHA).tex(maxU, minV).lightmap(lightmapX, lightmapY).endVertex();
                buffer.pos(d0 + off(facing, 2), d1 + fluidHeight, d2 + off(facing, 3)).color(red, green, blue, ALPHA).tex(maxU, maxV).lightmap(lightmapX, lightmapY).endVertex();
                buffer.pos(d0 + off(facing, 0), d1 + fluidHeight, d2 + off(facing, 1)).color(red, green, blue, ALPHA).tex(minU, maxV).lightmap(lightmapX, lightmapY).endVertex();

                break;
            case DOWN, UP:
                minU = sprite.getMinU();
                maxU = sprite.getMaxU();
                minV = sprite.getMinV();
                maxV = sprite.getMaxV();

                double height = facing == EnumFacing.DOWN ? -0.001 : fluidHeight; // 0.001 or 0?

                buffer.pos(d0 + 0.0D, d1 + height, d2 + 1.0D).color(red, green, blue, ALPHA).tex(minU, maxV).lightmap(lightmapX, lightmapY).endVertex();
                buffer.pos(d0 + 0.0D, d1 + height, d2 + 0.0D).color(red, green, blue, ALPHA).tex(minU, minV).lightmap(lightmapX, lightmapY).endVertex();
                buffer.pos(d0 + 1.0D, d1 + height, d2 + 0.0D).color(red, green, blue, ALPHA).tex(maxU, minV).lightmap(lightmapX, lightmapY).endVertex();
                buffer.pos(d0 + 1.0D, d1 + height, d2 + 1.0D).color(red, green, blue, ALPHA).tex(maxU, maxV).lightmap(lightmapX, lightmapY).endVertex();

                break;
        }
    }

    public void renderFluidCube(IBlockAccess world, BlockPos pos, double fluidHeight, FluidStack stack, BufferBuilder buffer, Vec3d translation) {
        for (EnumFacing facing : EnumFacing.values()) {
            renderFluidFace(world, pos, facing, fluidHeight, stack, buffer, translation);
        }
        postFluidRender(buffer);
    }

    public void postFluidRender(BufferBuilder buffer) {
        Vec3d cameraPos = ActiveRenderInfo.getCameraPosition();
        buffer.sortVertexData((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
    }

    // TODO: un-fuck this
    public void renderFluidTank(IBlockAccess world, AxisAlignedBB aabb, double fluidHeight, FluidStack stack, BufferBuilder buffer, Vec3d translation) {

        BlockPos wnd = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
        BlockPos esu = new BlockPos(aabb.maxX, aabb.maxY, aabb.maxZ);

        int xInt = esu.getX() - wnd.getX();
        int yInt = esu.getY() - wnd.getY();
        int zInt = esu.getZ() - wnd.getZ();

        int yMax = MathHelper.floor(fluidHeight * (yInt + 1));
        double reminder = fluidHeight * (yInt + 1) - yMax;

        int i, j;

        for (i = 0; i <= xInt; i++) {
            for (j = 0; j <= zInt; j++) {
                // Down
                renderFluidFace(world, wnd.add(i, 0, j), EnumFacing.DOWN, 0, stack, buffer, translation.add(i, 0, j));

                // Up
                renderFluidFace(world, wnd.add(i, yMax, j), EnumFacing.UP, reminder, stack, buffer, translation.add(i, yMax, j));
            }
        }

        // West & east
        for (j = 0; j <= zInt; j++) {
            for (i = 0; i < yMax; i++) {
                renderFluidFace(world, wnd.add(0, i, j), EnumFacing.WEST, 1, stack, buffer, translation.add(0, i, j));
                renderFluidFace(world, wnd.add(xInt, i, j), EnumFacing.EAST, 1, stack, buffer, translation.add(xInt, i, j));
            }
            renderFluidFace(world, wnd.add(0, yMax, j), EnumFacing.WEST, reminder, stack, buffer, translation.add(0, yMax, j));
            renderFluidFace(world, wnd.add(xInt, yMax, j), EnumFacing.EAST, reminder, stack, buffer, translation.add(xInt, yMax, j));
        }

        // South & north
        for (j = 0; j <= xInt; j++) {
            for (i = 0; i < yMax; i++) {
                renderFluidFace(world, wnd.add(j, i, zInt), EnumFacing.SOUTH, 1, stack, buffer, translation.add(j, i, zInt));
                renderFluidFace(world, wnd.add(j, i, 0), EnumFacing.NORTH, 1, stack, buffer, translation.add(j, i, 0));
            }
            renderFluidFace(world, wnd.add(j, yMax, zInt), EnumFacing.SOUTH, reminder, stack, buffer, translation.add(j, yMax, zInt));
            renderFluidFace(world, wnd.add(j, yMax, 0), EnumFacing.NORTH, reminder, stack, buffer, translation.add(j, yMax, 0));
        }

        postFluidRender(buffer);
    }
}
