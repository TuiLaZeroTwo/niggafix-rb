/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import co.uk.hexeption.utils.OutlineUtils;
import net.ccbluex.liquidbounce.features.module.modules.render.*;
import net.ccbluex.liquidbounce.utils.client.ClientUtils;
import net.ccbluex.liquidbounce.utils.attack.EntityUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

import static net.ccbluex.liquidbounce.utils.client.MinecraftInstance.mc;
import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.*;

@Mixin(RendererLivingEntity.class)
@SideOnly(Side.CLIENT)
public abstract class MixinRendererLivingEntity extends MixinRender {

    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"))
    private <T extends EntityLivingBase> void injectChamsPre(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = Chams.INSTANCE;
        final ESP esp = ESP.INSTANCE;
        boolean shouldRender = chams.handleEvents() && chams.getTargets() && EntityUtils.INSTANCE.isSelected(entity, false) || esp.handleEvents() && esp.shouldRender(entity) && esp.getMode().equals("Gaussian");

        if (shouldRender) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1f, -1000000F);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("RETURN"))
    private <T extends EntityLivingBase> void injectChamsPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo callbackInfo) {
        final Chams chams = Chams.INSTANCE;
        final ESP esp = ESP.INSTANCE;
        boolean shouldRender = chams.handleEvents() && chams.getTargets() && EntityUtils.INSTANCE.isSelected(entity, false) || esp.handleEvents() && esp.shouldRender(entity) && esp.getMode().equals("Gaussian");

        if (shouldRender) {
            glPolygonOffset(1f, 1000000F);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
    }

    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void canRenderName(T entity, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (NameTags.INSTANCE.shouldRenderNameTags(entity)) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    /**
     * @author CCBlueX
     */
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private <T extends EntityLivingBase> void renderModel(T p_renderModel_1_, float p_renderModel_2_, float p_renderModel_3_, float p_renderModel_4_, float p_renderModel_5_, float p_renderModel_6_, float p_renderModel_7_, CallbackInfo ci) {
        boolean visible = !p_renderModel_1_.isInvisible();
        final TrueSight trueSight = TrueSight.INSTANCE;
        boolean semiVisible = !visible && (!p_renderModel_1_.isInvisibleToPlayer(mc.thePlayer) || (trueSight.handleEvents() && trueSight.getEntities()));

        if (visible || semiVisible) {
            final ESP esp = ESP.INSTANCE;
            boolean shouldRenderGaussianESP = esp.handleEvents() && esp.shouldRender(p_renderModel_1_) && esp.getMode().equals("Gaussian");

            if (!shouldRenderGaussianESP) {
                if (!bindEntityTexture(p_renderModel_1_)) {
                    return;
                }
            } else {
                bindTexture(0);
                RenderUtils.INSTANCE.glColor(esp.getColor(p_renderModel_1_));
            }

            if (semiVisible) {
                pushMatrix();
                color(1f, 1f, 1f, 0.3F);
                depthMask(false);
                glEnable(GL_BLEND);
                blendFunc(770, 771);
                alphaFunc(516, 0.003921569F);
            }

            if (esp.handleEvents() && esp.shouldRender(p_renderModel_1_)) {
                boolean fancyGraphics = mc.gameSettings.fancyGraphics;
                mc.gameSettings.fancyGraphics = false;

                float gamma = mc.gameSettings.gammaSetting;
                mc.gameSettings.gammaSetting = 100000F;

                switch (esp.getMode().toLowerCase()) {
                    case "wireframe":
                        glPushMatrix();
                        glPushAttrib(GL_ALL_ATTRIB_BITS);
                        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                        glDisable(GL_TEXTURE_2D);
                        glDisable(GL_LIGHTING);
                        glDisable(GL_DEPTH_TEST);
                        glEnable(GL_LINE_SMOOTH);
                        glEnable(GL_BLEND);
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                        RenderUtils.INSTANCE.glColor(esp.getColor(p_renderModel_1_));
                        glLineWidth(esp.getWireframeWidth());
                        mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                        glPopAttrib();
                        glPopMatrix();
                        break;
                    case "outline":
                        ClientUtils.INSTANCE.disableFastRender();
                        resetColor();

                        final Color color = esp.getColor(p_renderModel_1_);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderOne(esp.getOutlineWidth());
                        mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderTwo();
                        mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderThree();
                        mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderFour(color);
                        mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                        OutlineUtils.setColor(color);
                        OutlineUtils.renderFive();
                        OutlineUtils.setColor(Color.WHITE);
                }
                mc.gameSettings.fancyGraphics = fancyGraphics;
                mc.gameSettings.gammaSetting = gamma;
            }

            mainModel.render(p_renderModel_1_, p_renderModel_2_, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);

            if (shouldRenderGaussianESP) {
                resetColor();
            }

            if (semiVisible) {
                disableBlend();
                alphaFunc(516, 0.1F);
                popMatrix();
                depthMask(true);
            }
        }

        ci.cancel();
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "HEAD"))
    private void injectFreeLookPitchPreMovePrevention(CallbackInfo ci) {
        FreeLook.INSTANCE.restoreOriginalRotation();
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At(value = "TAIL"))
    private void injectFreeLookPitchPostMovePrevention(CallbackInfo ci) {
        FreeLook.INSTANCE.useModifiedRotation();
    }
}