package radon.jujutsu_kaisen.client;

import javax.annotation.Nullable;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class PoseTarget {
    public Float xRot, yRot, zRot;
    public Float x, y, z;

    public PoseTarget() {}

    public PoseTarget(float xRot, float yRot, float zRot, float x, float y, float z) { 
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }

    public PoseTarget withRotation(float xRot, float yRot, float zRot) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
        return this;
    }

    public PoseTarget withPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    private static float blend(@Nullable Float base, @Nullable Float target, float alpha) {
    if (base == null && target == null) return 0f;
    if (base == null) return target;
    if (target == null) return base;
    return Mth.lerp(alpha, base, target);
}


    public PoseTarget applyTo(PoseTarget base, float alpha) {
        if (base == null) {
            return this;
        }

        return new PoseTarget(
            blend(base.xRot, this.xRot, alpha),
            blend(base.yRot, this.yRot, alpha),
            blend(base.zRot, this.zRot, alpha),
            blend(base.x,    this.x,    alpha),
            blend(base.y,    this.y,    alpha),
            blend(base.z,    this.z,    alpha)
        );
    }

}
