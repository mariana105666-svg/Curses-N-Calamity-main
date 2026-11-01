package radon.jujutsu_kaisen.limb;

import net.minecraft.world.entity.HumanoidArm;

public enum Limb {
    RIGHT_ARM(HumanoidArm.RIGHT, ""),
    LEFT_ARM(HumanoidArm.LEFT, ""),
    RIGHT_EXTRA_ARM(HumanoidArm.RIGHT, "right_hand"),
    LEFT_EXTRA_ARM(HumanoidArm.LEFT, "left_hand"),
    RIGHT_LEG(HumanoidArm.RIGHT, ""),
    LEFT_LEG(HumanoidArm.LEFT, "");

    private final HumanoidArm side;
    private final String curio;

    Limb(HumanoidArm side, String curio) {
        this.side = side;
        this.curio = curio;
    }

    public static Limb forArm(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? RIGHT_ARM : LEFT_ARM;
    }

    public static Limb extraForArm(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? RIGHT_EXTRA_ARM : LEFT_EXTRA_ARM;
    }

    public static Limb forLeg(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? RIGHT_LEG : LEFT_LEG;
    }

    public HumanoidArm arm() {
        return this.side;
    }

    public boolean isExtra() {
        return !this.curio.isEmpty();
    }

    public String curio() {
        return this.curio;
    }
}
