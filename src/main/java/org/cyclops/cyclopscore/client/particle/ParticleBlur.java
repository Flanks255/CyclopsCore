package org.cyclops.cyclopscore.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.Reference;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.lwjgl.opengl.GL11;

/**
 * A blurred static fading particle with any possible color.
 * @author rubensworks
 *
 */
public class ParticleBlur extends Particle {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(
			Reference.MOD_ID, Reference.TEXTURE_PATH_PARTICLES + "particle_blur.png");
	private static final int MAX_VIEW_DISTANCE = 30;
	
	private int scaleLife;
	private float originalScale;

	public ParticleBlur(ParticleBlurData data, World world, double x, double y, double z,
						double motionX, double motionY, double motionZ) {
		super(world, x, y, z, 0, 0, 0);
		this.motionX = motionX;
		this.motionY = motionY;
		this.motionZ = motionZ;
		
		this.particleRed = data.getRed();
		this.particleGreen = data.getGreen();
		this.particleBlue = data.getBlue();
		this.particleGravity = 0;
		
		this.originalScale *= data.getScale();
		this.maxAge = (int) ((rand.nextFloat() * 0.33F + 0.66F) * data.getAgeMultiplier());
		this.setSize(0.01F, 0.01F);
		
		this.prevPosX = posX;
		this.prevPosY = posY;
		this.prevPosZ = posZ;
		
		this.scaleLife = (int) (maxAge / 2.5);
		
		validateDistance();
	}
	
	private void validateDistance() {
		LivingEntity renderentity = Minecraft.getInstance().player;
		int visibleDistance = MAX_VIEW_DISTANCE;
		
		if(!Minecraft.getInstance().gameSettings.fancyGraphics) {
			visibleDistance = visibleDistance / 2;
		}

		if(renderentity == null
				|| renderentity.getDistanceSq(posX, posY, posZ) > visibleDistance) {
			maxAge = 0;
		}
	}

    @Override
    public void renderParticle(BufferBuilder worldRenderer, ActiveRenderInfo renderInfo,
							   float f, float f1, float f2, float f3, float f4, float f5) {
		float agescale = (float)age / (float) scaleLife;
		if(agescale > 1F) {
			agescale = 2 - agescale;
		}

		originalScale = originalScale * agescale;

		int oldDrawMode = worldRenderer.getDrawMode();
		VertexFormat oldVertexFormat = worldRenderer.getVertexFormat();
        Tessellator.getInstance().draw();
		GlStateManager.pushMatrix();

		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		RenderHelpers.bindTexture(TEXTURE);

		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 0.75F);

		float f10 = 0.5F * originalScale;
		float f11 = (float)(prevPosX + (posX - prevPosX) * f - interpPosX);
		float f12 = (float)(prevPosY + (posY - prevPosY) * f - interpPosY);
		float f13 = (float)(prevPosZ + (posZ - prevPosZ) * f - interpPosZ);

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		int i = this.getBrightnessForRender(f5);
		int j = i >> 16 & 65535;
		int k = i & 65535;
        worldRenderer.pos(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10, f13 - f3 * f10 - f5 * f10).tex(0, 1).
				color(particleRed, particleGreen, particleBlue, 0.9F).lightmap(j, k).endVertex();
        worldRenderer.pos(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10, f13 - f3 * f10 + f5 * f10).tex(1, 1).
				color(particleRed, particleGreen, particleBlue, 0.9F).lightmap(j, k).endVertex();
        worldRenderer.pos(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10, f13 + f3 * f10 + f5 * f10).tex(1, 0).
				color(particleRed, particleGreen, particleBlue, 0.9F).lightmap(j, k).endVertex();
        worldRenderer.pos(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10, f13 + f3 * f10 - f5 * f10).tex(0, 0).
				color(particleRed, particleGreen, particleBlue, 0.9F).lightmap(j, k).endVertex();

        Tessellator.getInstance().draw();

		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);

		GlStateManager.popMatrix();
		RenderHelpers.bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
		worldRenderer.begin(oldDrawMode, oldVertexFormat);
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (age++ >= maxAge) {
			setExpired();
		}

		motionY -= 0.04D * particleGravity;
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		motionX *= 0.98000001907348633D;
		motionY *= 0.98000001907348633D;
		motionZ *= 0.98000001907348633D;
	}

	/**
	 * Set the gravity for this particle.
	 * @param particleGravity The new gravity
	 */
	public void setGravity(float particleGravity) {
		this.particleGravity = particleGravity;
	}

}
