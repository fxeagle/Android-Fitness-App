package com.e.easyfitness.DAO.bodymeasures;

import com.e.easyfitness.enums.Unit;

import java.util.Date;

/* DataBase Object */
public class BodyMeasure {
    // Notez que l'identifiant est un long
    private long mId;
    private Date mDate;
    private int mBodypart_id;
    private float mMeasure;
    private Unit mUnit;
    private long mProfil_id;
    private String mTime;

    public BodyMeasure(long id, Date pDate, int pBodypart_id, float pMeasure, long pProfil_id, Unit pUnit) {
        super();
        mId = id;
        mDate = pDate;
        mBodypart_id = pBodypart_id;
        mMeasure = pMeasure;
        mProfil_id = pProfil_id;
        mUnit=pUnit;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public Date getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }

    public int getBodyPartID() {
        return mBodypart_id;
    }

    public float getBodyMeasure() { return mMeasure; }
    public void setBodyMeasure(float bodyMeasure) { mMeasure=bodyMeasure; }

    public long getProfileID() {
        return mProfil_id;
    }

    public Unit getUnit() { return mUnit; }
    public void setUnit(Unit unit) {  mUnit = unit; }

}