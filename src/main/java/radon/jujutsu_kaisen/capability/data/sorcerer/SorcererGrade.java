package radon.jujutsu_kaisen.capability.data.sorcerer;


import java.io.ObjectInputFilter.Config;

import net.minecraft.network.chat.Component;
import radon.jujutsu_kaisen.JujutsuKaisen;
import radon.jujutsu_kaisen.config.ConfigHolder;

public enum SorcererGrade {
    GRADE_4(ConfigHolder.SERVER.Grade4Exp.get().floatValue()),
    GRADE_3(ConfigHolder.SERVER.Grade3Exp.get().floatValue()),
    SEMI_GRADE_2(ConfigHolder.SERVER.SemiGrade2Exp.get().floatValue()),
    GRADE_2(ConfigHolder.SERVER.Grade2Exp.get().floatValue()),
    SEMI_GRADE_1(ConfigHolder.SERVER.SemiGrade1Exp.get().floatValue()),
    GRADE_1(ConfigHolder.SERVER.Grade1Exp.get().floatValue()),
    //SPECIAL_GRADE_1(ConfigHolder.SERVER.SpecialGrade1Exp.get().floatValue()),
    SPECIAL_GRADE(ConfigHolder.SERVER.SpecialGradeExp.get().floatValue());

    private final float required;

    SorcererGrade(float required) {
        this.required = required;
    }

    public float getRequiredExperience() {
        return this.required;
    }

    public Component getName() {
        return Component.translatable(String.format("grade.%s.%s", JujutsuKaisen.MOD_ID, this.name().toLowerCase()));
    }

    public SorcererGrade getNext() {
        SorcererGrade[] grades = SorcererGrade.values();
        int index = this.ordinal();
        if (index < grades.length - 1) {
            return grades[index + 1];
        } else {
            return grades[index];
        }
    }
}
