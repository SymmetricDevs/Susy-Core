package supersymmetry.client.renderer.particles;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import codechicken.lib.vec.Vector3;
import gregtech.client.particle.GTBloomParticle;
import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.postprocessing.BloomEffect;
import gregtech.client.shader.postprocessing.BloomType;
import gregtech.client.utils.EffectRenderContext;
import gregtech.common.ConfigHolder;
import supersymmetry.Supersymmetry;

public class SusyParticleRocketFlame extends GTBloomParticle {

    /**
     * Animation frames, played in order over the particle's lifetime. Registered onto the block
     * atlas by {@link supersymmetry.client.ClientProxy#stitchTexture} so that every frame shares one
     * texture binding, keeping all live flames in a single draw call.
     */
    public static final ResourceLocation[] SPRITE_NAMES = {
            new ResourceLocation(Supersymmetry.MODID, "fx/flame1"),
            new ResourceLocation(Supersymmetry.MODID, "fx/flame2"),
            new ResourceLocation(Supersymmetry.MODID, "fx/flame3"),
            new ResourceLocation(Supersymmetry.MODID, "fx/flame4"),
            new ResourceLocation(Supersymmetry.MODID, "fx/flame5") };

    private static final TextureAtlasSprite[] SPRITES = new TextureAtlasSprite[SPRITE_NAMES.length];

    private static TextureAtlasSprite getSprite(int frame) {
        TextureAtlasSprite sprite = SPRITES[frame];
        if (sprite == null || sprite.getIconName().equals("missingno")) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getAtlasSprite(SPRITE_NAMES[frame].toString());
            SPRITES[frame] = sprite;
        }
        return sprite;
    }

    private double motionX;
    private double motionY;
    private double motionZ;
    protected double prevPosX;
    protected double prevPosY;
    protected double prevPosZ;
    private int particleAge;
    private int particleMaxAge;
    private AxisAlignedBB boundingBox;
    protected boolean onGround;
    private World world;

    public SusyParticleRocketFlame(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
                                   double ySpeedIn, double zSpeedIn) {
        super(xCoordIn, yCoordIn, zCoordIn);
        this.prevPosX = xCoordIn;
        this.prevPosY = yCoordIn;
        this.prevPosZ = zCoordIn;
        this.motionX = xSpeedIn;
        this.motionY = ySpeedIn;
        this.motionZ = zSpeedIn;
        this.world = worldIn;
        this.particleMaxAge = 40;
        this.setPosition(xCoordIn, yCoordIn, zCoordIn);
    }

    public void setPosition(double x, double y, double z)
    {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        float f = 1 / 2.0F;
        float f1 = 1;
        this.setBoundingBox(new AxisAlignedBB(x - (double)f, y, z - (double)f, x + (double)f, y + (double)f1, z + (double)f));
    }

    @Override
    public void renderParticle(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
    }

    public void move(double x, double y, double z) {
        double d0 = y;
        double origX = x;
        double origZ = z;

        List<AxisAlignedBB> list = this.world.getCollisionBoxes(null, this.getBoundingBox().expand(x, y, z));

        for (AxisAlignedBB axisalignedbb : list) {
            y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
        }

        this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));

        for (AxisAlignedBB axisalignedbb1 : list) {
            x = axisalignedbb1.calculateXOffset(this.getBoundingBox(), x);
        }

        this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));

        for (AxisAlignedBB axisalignedbb2 : list) {
            z = axisalignedbb2.calculateZOffset(this.getBoundingBox(), z);
        }

        this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));

        this.resetPositionToBB();
        this.onGround = d0 != y && d0 < 0.0D;

        if (origX != x) {
            this.motionX = 0.0D;
        }

        if (origZ != z) {
            this.motionZ = 0.0D;
        }
    }

    protected void resetPositionToBB() {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB bb) {
        this.boundingBox = bb;
    }

    @Override
    protected @Nullable IRenderSetup getBloomRenderSetup() {
        return SETUP;
    }

    @NotNull @Override
    protected BloomType getBloomType() {
        return BloomType.UNREAL;
    }

    @Override
    public void renderBloomEffect(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
        double inX = prevPosX + (posX - prevPosX) * context.partialTicks();
        double inY = prevPosY + (posY - prevPosY) * context.partialTicks();
        double inZ = prevPosZ + (posZ - prevPosZ) * context.partialTicks();

        buffer.setTranslation(inX - context.cameraX(), inY - context.cameraY(), inZ - context.cameraZ());

        // Face camera. Same winding as Particle#renderParticle, which is back-facing towards the
        // camera, so the setup has to disable culling for this quad to be visible.
        Vec3d[] avec3d = new Vec3d[] {
                new Vec3d(-context.rotationX() - context.rotationYZ(),
                        -context.rotationXZ(),
                        -context.rotationZ() - context.rotationXY()),
                new Vec3d(-context.rotationX() + context.rotationYZ(),
                        context.rotationXZ(),
                        -context.rotationZ() + context.rotationXY()),
                new Vec3d(context.rotationX() + context.rotationYZ(),
                        context.rotationXZ(),
                        context.rotationZ() + context.rotationXY()),
                new Vec3d(context.rotationX() - context.rotationYZ(),
                        -context.rotationXZ(),
                        context.rotationZ() - context.rotationXY())};
        for (int i = 0; i < 4; i++) {
            avec3d[i] = avec3d[i].scale(4);
        }
        TextureAtlasSprite sprite = getSprite(getFrame());
        float minU = sprite.getMinU();
        float maxU = sprite.getMaxU();
        float minV = sprite.getMinV();
        float maxV = sprite.getMaxV();

        buffer.pos(avec3d[0].x, avec3d[0].y,  avec3d[0].z).tex(maxU, maxV).endVertex();
        buffer.pos(avec3d[1].x, avec3d[1].y,  avec3d[1].z).tex(maxU, minV).endVertex();
        buffer.pos(avec3d[2].x, avec3d[2].y,  avec3d[2].z).tex(minU, minV).endVertex();
        buffer.pos(avec3d[3].x, avec3d[3].y,  avec3d[3].z).tex(minU, maxV).endVertex();
    }

    /**
     * @return index into {@link #SPRITE_NAMES}, advancing evenly across the particle's lifetime
     */
    private int getFrame() {
        if (particleMaxAge <= 0) return 0;
        int frame = particleAge * SPRITE_NAMES.length / particleMaxAge;
        return Math.min(frame, SPRITE_NAMES.length - 1);
    }

    private static final IRenderSetup SETUP = new IRenderSetup() {

        float lastBrightnessX;
        float lastBrightnessY;

        @Override
        @SideOnly(Side.CLIENT)
        public void preDraw(@NotNull BufferBuilder buffer) {
            BloomEffect.strength = 1f;
            BloomEffect.baseBrightness = (float) ConfigHolder.client.shader.heatEffectBloom.baseBrightness;
            BloomEffect.highBrightnessThreshold = (float) ConfigHolder.client.shader.heatEffectBloom.highBrightnessThreshold;
            BloomEffect.lowBrightnessThreshold = (float) ConfigHolder.client.shader.heatEffectBloom.lowBrightnessThreshold;
            BloomEffect.step = 1;

            this.lastBrightnessX = OpenGlHelper.lastBrightnessX;
            this.lastBrightnessY = OpenGlHelper.lastBrightnessY;
            GlStateManager.color(1, 1, 1, 1);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();

            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void postDraw(@NotNull BufferBuilder buffer) {
            buffer.setTranslation(0, 0, 0);
            Tessellator.getInstance().draw();
            GlStateManager.disableBlend();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }
    };
}
