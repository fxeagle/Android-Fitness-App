package com.e.easyfitness.DAO.bodymeasures;

import com.e.easyfitness.R;
import com.e.easyfitness.enums.Unit;
import com.e.easyfitness.enums.UnitType;

/* DataBase Object */
public class BodyPartExtensions {
    public static final int ABDOMINAUX = 0;
    public static final int ADDUCTEURS = 1;
    public static final int BICEPS = 2;
    public static final int TRICEPS = 3;
    public static final int DELTOIDS = 4;
    public static final int MOLLETS = 5;
    public static final int PECTORAUX = 6;
    public static final int DORSEAUX = 7;
    public static final int QUADRICEPS = 8;
    public static final int ISCHIOJAMBIERS = 9;
    public static final int LEFTBICEPS = 10;
    public static final int RIGHTBICEPS = 11;
    public static final int LEFTTHIGH = 12;
    public static final int RIGHTTHIGH = 13;
    public static final int LEFTCALVES = 14;
    public static final int RIGHTCALVES = 15;
    public static final int WAIST = 16;
    public static final int NECK = 17;
    public static final int BEHIND = 18;
    public static final int WEIGHT = 19;
    public static final int FAT = 20;
    public static final int BONES = 21;
    public static final int WATER = 22;
    public static final int MUSCLES = 23;
    public static final int TRAPEZIUS = 24;
    public static final int OBLIQUES = 25;
    public static final int SHOULDERS = 26;

    public static final int TYPE_MUSCLE = 0;
    public static final int TYPE_WEIGHT = 1;

    public static int getBodyStringID(int pBodyID) {
        switch (pBodyID) {
            case ABDOMINAUX:
                return R.string.abdominaux;
            case ADDUCTEURS:
                return R.string.adducteurs;
            case BICEPS:
                return R.string.biceps;
            case TRICEPS:
                return R.string.triceps;
            case DELTOIDS:
                return R.string.deltoids;
            case MOLLETS:
                return R.string.mollets;
            case PECTORAUX:
                return R.string.pectoraux;
            case DORSEAUX:
                return R.string.dorseaux;
            case QUADRICEPS:
                return R.string.quadriceps;
            case ISCHIOJAMBIERS:
                return R.string.ischio_jambiers;
            case LEFTBICEPS:
                return R.string.left_arm;
            case RIGHTBICEPS:
                return R.string.right_arm;
            case LEFTTHIGH:
                return R.string.left_thigh;
            case RIGHTTHIGH:
                return R.string.right_thigh;
            case LEFTCALVES:
                return R.string.left_calves;
            case RIGHTCALVES:
                return R.string.right_calves;
            case WAIST:
                return R.string.waist;
            case NECK:
                return R.string.neck;
            case TRAPEZIUS:
                return R.string.trapezius;
            case OBLIQUES:
                return R.string.obliques;
            case SHOULDERS:
                return R.string.shoulders;
            case BEHIND:
                return R.string.behind;
            case WEIGHT:
                return R.string.weightLabel;
            case FAT:
                return R.string.fatLabel;
            case BONES:
                return R.string.bonesLabel;
            case WATER:
                return R.string.waterLabel;
            case MUSCLES:
                return R.string.musclesLabel;
        }

        return -1;
    }

    public static int getBodyLogoID(int pBodyID) {
        switch (pBodyID) {
            case ABDOMINAUX:
                return R.drawable.ic_chest;
            case ADDUCTEURS:
                return R.drawable.ic_leg;
            case BICEPS:
                return R.drawable.ic_arm;
            case TRICEPS:
                return R.drawable.ic_arm;
            case DELTOIDS:
                return R.drawable.ic_chest;
            case MOLLETS:
                return R.drawable.ic_leg;
            case PECTORAUX:
                return R.drawable.ic_chest_measure;
            case DORSEAUX:
                return R.drawable.ic_chest;
            case QUADRICEPS:
                return R.drawable.ic_leg;
            case ISCHIOJAMBIERS:
                return R.drawable.ic_leg;
            case LEFTBICEPS:
                return R.drawable.ic_arm_measure;
            case RIGHTBICEPS:
                return R.drawable.ic_arm_measure;
            case LEFTTHIGH:
                return R.drawable.ic_tight_measure;
            case RIGHTTHIGH:
                return R.drawable.ic_tight_measure;
            case LEFTCALVES:
                return R.drawable.ic_calve_measure;
            case RIGHTCALVES:
                return R.drawable.ic_calve_measure;
            case WAIST:
                return R.drawable.ic_waist_measure;
            case NECK:
                return R.drawable.ic_neck;
            case BEHIND:
                return R.drawable.ic_buttock_measure;
            case TRAPEZIUS:
                return R.drawable.ic_neck;
            case OBLIQUES:
                return R.string.obliques;
            case SHOULDERS:
                return R.drawable.ic_neck;
        }

        return -1;
    }

    public static UnitType getUnitType(int pBodyID) {
        switch (pBodyID) {
            case WEIGHT:
                return UnitType.WEIGHT;
            case FAT:
            case BONES:
            case WATER:
            case MUSCLES:
                return UnitType.PERCENTAGE;
            default:
                return UnitType.SIZE;
        }
    }
}