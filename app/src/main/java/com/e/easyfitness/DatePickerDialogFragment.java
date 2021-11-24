package com.e.easyfitness;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;


public class DatePickerDialogFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener onDateSetListener;

    static public DatePickerDialogFragment newInstance(DatePickerDialog.OnDateSetListener onDateSetListener) {
        DatePickerDialogFragment pickerFragment = new DatePickerDialogFragment();
        pickerFragment.setOnDateSetListener(onDateSetListener);

        //Pass the date in a bundle.
        Bundle bundle = new Bundle();
        pickerFragment.setArguments(bundle);
        return pickerFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        Calendar cal = Calendar.getInstance();
        return new DatePickerDialog(getActivity(),
            onDateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH));
    }

    private void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        this.onDateSetListener = listener;
    }
}