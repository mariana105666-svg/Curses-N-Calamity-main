// package radon.jujutsu_kaisen.client.visual.visual;

// import java.util.UUID;
// import java.util.WeakHashMap;

// import net.minecraft.client.Minecraft;
// import net.minecraft.core.particles.ParticleTypes;
// import net.minecraft.world.effect.MobEffectInstance;
// import net.minecraft.world.entity.LivingEntity;
// import net.minecraft.world.entity.player.Player;
// import radon.jujutsu_kaisen.ability.JJKAbilities;
// import radon.jujutsu_kaisen.ability.idle_transfiguration.IdleTransfiguration;
// import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
// import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
// import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
// import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
// import radon.jujutsu_kaisen.client.visual.ClientVisualHandler;
// import radon.jujutsu_kaisen.client.visual.base.IVisual;
// import radon.jujutsu_kaisen.effect.JJKEffects;
// import radon.jujutsu_kaisen.util.HelperMethods;
// import virtuoel.pehkui.api.ScaleData;
// import virtuoel.pehkui.api.ScaleTypes;

// public class PehkuiVisual implements IVisual {  

//     @Override
//     public boolean isValid(LivingEntity entity, ClientVisualHandler.ClientData data) {
//         Minecraft mc = Minecraft.getInstance();

//         if (mc.player == null) return false;
//             ISorcererData cap = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null); 
//         if (entity instanceof Player && cap != null) {
//             if (cap.hasTrait(Trait.CURSED_WOMB) || cap.hasToggled(JJKAbilities.SHRINK.get())) {
//                 return true;
//             }
//             else {
//                 ScaleData baseScale = ScaleTypes.BASE.getScaleData(entity);
//                 ScaleData baseWidth = ScaleTypes.WIDTH.getScaleData(entity);
//                 if (baseScale.getBaseScale() != baseScale.getScale()) {
//                     baseWidth.resetScale();
//                     baseScale.resetScale();
//                 }
//                 if (baseWidth.getBaseScale() != baseWidth.getScale()) {
//                     baseWidth.resetScale();
//                     baseScale.resetScale();
//                 }
//                 // float currentScale = baseScale.getScale();
//                 // float currentWidth = baseWidth.getScale();
//                 return false;
//             }
//         }
//         return false;
//     }

//     @Override
//     public void tick(LivingEntity entity, ClientVisualHandler.ClientData data) {
//         Minecraft mc = Minecraft.getInstance();

//         if (mc.level == null || mc.player == null) return;
//         ISorcererData cap = entity.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
//         if (cap != null) {
//         float targetScale = 0.7F;
//         float targetWidth = 1.2F;
//         if (cap.checkWombAwakened() ==  true) {
//             targetScale = 1.1F;
//             targetWidth = 1.0F;
//         }
//         ScaleData baseScale = ScaleTypes.BASE.getScaleData(entity);
//         ScaleData baseWidth = ScaleTypes.WIDTH.getScaleData(entity);
//         // float currentScale = baseScale.getScale();
//         // float currentWidth = baseWidth.getScale();
//         if (cap.hasToggled(JJKAbilities.SHRINK.get())) {
//             targetScale = 0.5F;
//             float currentScale = baseScale.getScale();
//             float newScale = currentScale + (targetScale - currentScale) * 0.1F;
//             baseScale.setScale(newScale);

//         }
//         else if (cap.hasTrait(Trait.CURSED_WOMB)) {
//             baseScale.setScale(targetScale);
//             baseWidth.setScale(targetWidth);
//         }
//         }

//     }
// }
