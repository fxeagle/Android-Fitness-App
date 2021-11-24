package com.e.easyfitness.DAO.bodymeasures;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.e.easyfitness.DAO.DAOBase;
import com.e.easyfitness.DAO.Profile;
import com.e.easyfitness.enums.Unit;
import com.e.easyfitness.utils.DateConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DAOBodyMeasure extends DAOBase {

    // Contacts table name
    public static final String TABLE_NAME = "EFbodymeasures";

    public static final String KEY = "_id";
    public static final String BODYPART_ID = "bodypart_id";
    public static final String MEASURE = "mesure";
    public static final String DATE = "date";
    public static final String UNIT = "unit";
    public static final String PROFIL_KEY = "profil_id";

    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" + KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DATE + " DATE, " + BODYPART_ID + " INTEGER, " + MEASURE + " REAL , " + PROFIL_KEY + " INTEGER, " + UNIT + " INTEGER);";

    public static final String TABLE_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    private Profile mProfile = null;
    private Cursor mCursor = null;

    public DAOBodyMeasure(Context context) {
        super(context);
    }

    /**
     * @param pDate           date of the weight measure
     * @param pBodyPartId id of the body part
     * @param pMeasure        body measure
     * @param pProfileId      profil associated with the measure
     */
    public void addBodyMeasure(Date pDate, long pBodyPartId, float pMeasure, long pProfileId, Unit pUnit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues value = new ContentValues();

        // Only one measure pr day, so if one already existing, updates it.
        BodyMeasure existingBodyMeasure = getBodyMeasuresFromDate(pBodyPartId, pDate, pProfileId);
        if (existingBodyMeasure==null) {

            String dateString = DateConverter.dateToDBDateStr(pDate);
            value.put(DAOBodyMeasure.DATE, dateString);
            value.put(DAOBodyMeasure.BODYPART_ID, pBodyPartId);
            value.put(DAOBodyMeasure.MEASURE, pMeasure);
            value.put(DAOBodyMeasure.PROFIL_KEY, pProfileId);
            value.put(DAOBodyMeasure.UNIT, pUnit.ordinal());

            db.insert(DAOBodyMeasure.TABLE_NAME, null, value);

        } else {
            existingBodyMeasure.setBodyMeasure(pMeasure);
            existingBodyMeasure.setUnit(pUnit);

            updateMeasure(existingBodyMeasure);
        }
        db.close(); // Closing database connection
    }

    // Getting single value
    public BodyMeasure getMeasure(long id) {

        SQLiteDatabase db = this.getReadableDatabase();

        mCursor = null;
        mCursor = db.query(TABLE_NAME,
            new String[]{KEY, DATE, BODYPART_ID, MEASURE, PROFIL_KEY, UNIT},
            KEY + "=?",
            new String[]{String.valueOf(id)},
            null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();

        Date date = DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATE)));

        BodyMeasure value = new BodyMeasure(mCursor.getLong(mCursor.getColumnIndex(KEY)),
            date,
            mCursor.getInt(mCursor.getColumnIndex(BODYPART_ID)),
            mCursor.getFloat(mCursor.getColumnIndex(MEASURE)),
            mCursor.getLong(mCursor.getColumnIndex(PROFIL_KEY)),
            Unit.fromInteger(mCursor.getInt(mCursor.getColumnIndex(UNIT)))
        );

        db.close();

        // return value
        return value;
    }

    // Getting All Measures
    public List<BodyMeasure> getMeasuresList(SQLiteDatabase db, String pRequest) {
        List<BodyMeasure> valueList = new ArrayList<>();
        // Select All Query

        mCursor = null;
        mCursor = db.rawQuery(pRequest, null);

        // looping through all rows and adding to list
        if (mCursor.moveToFirst()) {
            do {
                Date date = DateConverter.DBDateStrToDate(mCursor.getString(mCursor.getColumnIndex(DATE)));

                BodyMeasure value = new BodyMeasure(mCursor.getLong(mCursor.getColumnIndex(KEY)),
                    date,
                    mCursor.getInt(mCursor.getColumnIndex(BODYPART_ID)),
                    mCursor.getFloat(mCursor.getColumnIndex(MEASURE)),
                    mCursor.getLong(mCursor.getColumnIndex(PROFIL_KEY)),
                    Unit.fromInteger(mCursor.getInt(mCursor.getColumnIndex(UNIT)))
                );

                // Adding value to list
                valueList.add(value);
            } while (mCursor.moveToNext());
        }

        // return value list
        return valueList;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
     */
    public List<BodyMeasure> getBodyPartMeasuresList(long pBodyPartID, Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";

        // return value list
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
     */
    public List<BodyMeasure> getBodyPartMeasuresListTop4(long pBodyPartID, Profile pProfile) {
        if (pProfile==null) return null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC LIMIT 4;";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    /**
     * Getting All Measures for a specific Profile
     *
     * @param pProfile
     * @return List<BodyMeasure>
     */
    public List<BodyMeasure> getBodyMeasuresList(Profile pProfile) {
        if (pProfile==null) return null;
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    /**
     * Getting All Measures
     * @return List<BodyMeasure>
     */
    public List<BodyMeasure> getAllBodyMeasures() {
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " ORDER BY date(" + DATE + ") DESC";
        return getMeasuresList(getReadableDatabase(), selectQuery);
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfile
     * @return List<BodyMeasure>
     */
    public BodyMeasure getLastBodyMeasures(long pBodyPartID, Profile pProfile) {
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + PROFIL_KEY + "=" + pProfile.getId() + " ORDER BY date(" + DATE + ") DESC";

        List<BodyMeasure> array = getMeasuresList(getReadableDatabase(), selectQuery);
        if (array.size() <= 0) {
            return null;
        }

        // return value list
        return getMeasuresList(getReadableDatabase(), selectQuery).get(0);
    }

    /**
     * Getting All Measures associated to a Body part for a specific Profile
     *
     * @param pBodyPartID
     * @param pProfileId
     * @return List<BodyMeasure>
     */
    public BodyMeasure getBodyMeasuresFromDate(long pBodyPartID, Date pDate, long pProfileId) {
        String dateString = DateConverter.dateToDBDateStr(pDate);

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + BODYPART_ID + "=" + pBodyPartID + " AND " + DATE + "=\"" + dateString + "\" AND " + PROFIL_KEY + "=" + pProfileId + " ORDER BY date(" + DATE + ") DESC";

        List<BodyMeasure> array = getMeasuresList(getReadableDatabase(), selectQuery);
        if (array.size() <= 0) {
            return null;
        }

        // return value list
        return getMeasuresList(getReadableDatabase(), selectQuery).get(0);
    }

    // Updating single value
    public int updateMeasure(BodyMeasure m) {
        return updateMeasure(getWritableDatabase(), m);
    }

    // Updating single value
    public int updateMeasure(SQLiteDatabase db, BodyMeasure m) {
        ContentValues value = new ContentValues();
        String dateString = DateConverter.dateToDBDateStr(m.getDate());
        value.put(DAOBodyMeasure.DATE, dateString);
        value.put(DAOBodyMeasure.BODYPART_ID, m.getBodyPartID());
        value.put(DAOBodyMeasure.MEASURE, m.getBodyMeasure());
        value.put(DAOBodyMeasure.PROFIL_KEY, m.getProfileID());
        value.put(DAOBodyMeasure.UNIT, m.getUnit().ordinal());

        // updating row
        return db.update(TABLE_NAME, value, KEY + " = ?",
            new String[]{String.valueOf(m.getId())});
    }

    // Deleting single Measure
    public void deleteMeasure(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY + " = ?",
            new String[]{String.valueOf(id)});
    }

    // Getting Profils Count
    public int getCount() {
        String countQuery = "SELECT * FROM " + TABLE_NAME;
        open();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int value = cursor.getCount();
        cursor.close();
        close();

        // return count
        return value;
    }

    public void populate() {
        Date date = new Date();
        int poids = 10;

        for (int i = 1; i <= 5; i++) {
            date.setTime(date.getTime() + i * 1000 * 60 * 60 * 24 * 2);
            //addBodyMeasure(date, (float) i, mProfile);
        }
    }
}
