package com.e.easyfitness;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.e.easyfitness.DAO.record.DAORecord;
import com.e.easyfitness.DAO.record.Record;
import com.e.easyfitness.enums.DisplayType;
import com.e.easyfitness.enums.DistanceUnit;
import com.e.easyfitness.enums.WeightUnit;
import com.e.easyfitness.utils.UnitConverter;
import com.e.easyfitness.views.WorkoutValuesInputView;

public class RecordEditorDialogbox extends Dialog implements View.OnClickListener {

    private final boolean mShowRestTime;
    private Activity mActivity;
    public Dialog d;
    private WorkoutValuesInputView mWorkoutValuesInput;
    private Record mRecord;
    private boolean mCancelled=false;

    public RecordEditorDialogbox(Activity a, Record record) {
        super(a);
        this.mActivity = a;
        mRecord=record;
        mShowRestTime=false;
    }

    public RecordEditorDialogbox(Activity a, Record record, boolean showRestTime) {
        super(a);
        this.mActivity = a;
        mRecord=record;
        mShowRestTime = showRestTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_record_editor);
        this.setCanceledOnTouchOutside(false);

        Button updateButton = findViewById(R.id.btn_update);
        Button cancelButton = findViewById(R.id.btn_cancel);
        mWorkoutValuesInput = findViewById(R.id.EditorWorkoutValuesInput);

        mWorkoutValuesInput.setRecord(mRecord);

        mWorkoutValuesInput.setShowRestTime(mShowRestTime);

        updateButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_cancel) {
            mCancelled=true;
            cancel();
        } else if (v.getId() == R.id.btn_update) {
            // update record
            DAORecord daoRecord = new DAORecord(mActivity.getBaseContext());
            switch (mRecord.getExerciseType()) {
                case CARDIO:
                    float distance = mWorkoutValuesInput.getDistanceValue();
                    if (mWorkoutValuesInput.getDistanceUnit()== DistanceUnit.MILES) {
                        distance = UnitConverter.MilesToKm(distance); // Always convert to KG
                    }
                    mRecord.setDuration(mWorkoutValuesInput.getDurationValue());
                    mRecord.setDistance(distance);
                    mRecord.setDistanceUnit(mWorkoutValuesInput.getDistanceUnit());
                    break;
                case ISOMETRIC:
                    float tmpPoids = mWorkoutValuesInput.getWeightValue();
                    tmpPoids = UnitConverter.weightConverter(tmpPoids,mWorkoutValuesInput.getWeightUnit(), WeightUnit.KG); // Always convert to KG

                    mRecord.setSets(mWorkoutValuesInput.getSets());
                    mRecord.setSeconds(mWorkoutValuesInput.getSeconds());
                    mRecord.setWeight(tmpPoids);
                    mRecord.setWeightUnit(mWorkoutValuesInput.getWeightUnit());
                    break;
                case STRENGTH:
                    float tmpWeight = mWorkoutValuesInput.getWeightValue();
                    tmpPoids = UnitConverter.weightConverter(tmpWeight,mWorkoutValuesInput.getWeightUnit(), WeightUnit.KG); // Always convert to KG
                    mRecord.setSets(mWorkoutValuesInput.getSets());
                    mRecord.setReps(mWorkoutValuesInput.getReps());
                    mRecord.setWeight(tmpPoids);
                    mRecord.setWeightUnit(mWorkoutValuesInput.getWeightUnit());
                    break;
            }
            if (mShowRestTime) {
                if (mWorkoutValuesInput.isRestTimeActivated()) {
                    mRecord.setRestTime(mWorkoutValuesInput.getRestTime());
                } else {
                    mRecord.setRestTime(0);
                }
            }
            daoRecord.updateRecord(mRecord);
            mCancelled = false;
            dismiss();
        }
    }

    public boolean isCancelled() { return mCancelled; };

}