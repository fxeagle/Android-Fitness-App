package com.e.easyfitness.bodymeasures;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.e.easyfitness.BtnClickListener;
import com.e.easyfitness.DAO.Profile;
import com.e.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.e.easyfitness.DAO.bodymeasures.BodyPart;
import com.e.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.e.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.e.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.e.easyfitness.MainActivity;
import com.e.easyfitness.ProfileViMo;
import com.e.easyfitness.R;
import com.e.easyfitness.SettingsFragment;
import com.e.easyfitness.ValueEditorDialogbox;
import com.e.easyfitness.enums.Unit;
import com.e.easyfitness.enums.UnitType;
import com.e.easyfitness.utils.DateConverter;
import com.e.easyfitness.utils.UnitConverter;
import com.e.easyfitness.views.EditableInputView;
import com.e.easyfitness.utils.ExpandedListView;
import com.e.easyfitness.views.GraphView;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BodyPartDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private TextView addButton = null;
    private EditableInputView nameEdit = null;
    private ExpandedListView measureList = null;
    private Toolbar bodyToolbar = null;
    private GraphView mDateGraph = null;
    private DAOBodyMeasure mBodyMeasureDb = null;
    private DAOBodyPart mDbBodyPart;
    private BodyPart mInitialBodyPart;
    private String mCurrentPhotoPath = null;

    private BtnClickListener itemClickDeleteRecord = view -> {
        switch (view.getId()) {
            case R.id.deleteButton:
                showDeleteDialog((long)view.getTag());
                break;
            case R.id.editButton:
                showEditDialog((long)view.getTag());
                break;

        }
    };
    private OnClickListener onClickAddMeasure = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ValueEditorDialogbox editorDialogbox;
            BodyMeasure lastBodyMeasure = mBodyMeasureDb.getLastBodyMeasures(mInitialBodyPart.getId(), getProfile());
            double lastValue;
            if (lastBodyMeasure == null) {
                lastValue=0;
            } else {
                lastValue=lastBodyMeasure.getBodyMeasure();
            }
            Unit unitDef = getValidUnit(lastBodyMeasure);

            editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", lastValue, unitDef);
            editorDialogbox.setTitle(R.string.AddLabel);
            editorDialogbox.setPositiveButton(R.string.AddLabel);
            editorDialogbox.setOnDismissListener(dialog -> {
                if (!editorDialogbox.isCancelled()) {
                    Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                    float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                    Unit unit = Unit.fromString(editorDialogbox.getUnit());
                    mBodyMeasureDb.addBodyMeasure(date, mInitialBodyPart.getId(), value, getProfile().getId(), unit);
                    refreshData();
                }
            });
            editorDialogbox.show();
        }
    };
    private ProfileViMo profileViMo;

    private OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {

        // Get the cursor, positioned to the corresponding row in the result set
        //Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        final long selectedID = id;

        String[] profilListArray = new String[1]; // un seul choix
        profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);

        AlertDialog.Builder itemActionBuilder = new AlertDialog.Builder(getActivity());
        itemActionBuilder.setTitle("").setItems(profilListArray, (dialog, which) -> {

            switch (which) {
                // Delete
                case 0:
                    mBodyMeasureDb.deleteMeasure(selectedID);
                    refreshData();
                    KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.removedid).toString() + " " + selectedID, Gravity.BOTTOM, KToast.LENGTH_SHORT);
                    break;
                default:
            }
        });
        itemActionBuilder.show();

        return true;
    };

    private ImageButton deleteButton;
    private EditableInputView.OnTextChangedListener onTextChangeListener = this::requestForSave;
    private TextView editDate;
    private EditText editText;
    private ImageView bodyPartImageView;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static BodyPartDetailsFragment newInstance(long bodyPartID, boolean showInput) {
        BodyPartDetailsFragment f = new BodyPartDetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("bodyPartID", bodyPartID);
        args.putBoolean("showInput", showInput);
        f.setArguments(args);

        return f;
    }

    private View.OnClickListener onClickToolbarItem = v -> {
        // Handle presses on the action bar items
        switch (v.getId()) {
            case R.id.deleteButton:
                delete();
                break;
        }
    };

    private void delete() {
        // afficher un message d'alerte
        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

        deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
        deleteDialogBuilder.setMessage(getActivity().getResources().getText(R.string.delete_bodypart_confirm));

        // Si oui, supprimer la base de donnee et refaire un Start.
        deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), (dialog, which) -> {
            // Suppress the machine
            mDbBodyPart.delete(mInitialBodyPart.getId());
            // Suppress the associated Fontes records
            deleteRecordsAssociatedToMachine();
            getActivity().onBackPressed();
        });

        deleteDialogBuilder.setNegativeButton(this.getResources().getString(R.string.global_no), (dialog, which) -> {
            // Do nothing
            dialog.dismiss();
        });

        AlertDialog deleteDialog = deleteDialogBuilder.create();
        deleteDialog.show();
    }

    private void deleteRecordsAssociatedToMachine() {
        DAOBodyMeasure mDbBodyMeasure = new DAOBodyMeasure(getContext());

        Profile lProfile = getProfile();

        List<BodyMeasure> listBodyMeasure = mDbBodyMeasure.getBodyPartMeasuresList(mInitialBodyPart.getId(), lProfile);
        for (BodyMeasure record : listBodyMeasure) {
            mDbBodyMeasure.deleteMeasure(record.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bodytracking_details_fragment, container, false);

        mDbBodyPart = new DAOBodyPart(getContext());

        addButton = view.findViewById(R.id.buttonAdd);
        nameEdit = view.findViewById(R.id.BODYPART_NAME);
        measureList = view.findViewById(R.id.listWeightProfil);
        bodyToolbar = view.findViewById(R.id.bodyTrackingDetailsToolbar);
        bodyPartImageView = view.findViewById(R.id.BODYPART_LOGO);
        CardView nameCardView = view.findViewById(R.id.nameCardView);
        mDateGraph = view.findViewById(R.id.bodymeasureChart);

        /* Initialisation BodyPart */
        long bodyPartID = getArguments().getLong("bodyPartID", 0);
        mInitialBodyPart = mDbBodyPart.getBodyPart(bodyPartID);

        // Hide Values Input if needed.
        /*if (!getArguments().getBoolean("showInput", true)) {
            addButton.setVisibility(View.GONE);
        } else {
            addButton.setVisibility(View.VISIBLE);
        }*/

        if (mInitialBodyPart.getBodyPartResKey()!=-1) {
            bodyPartImageView.setVisibility(View.VISIBLE);
            bodyPartImageView.setImageDrawable(mInitialBodyPart.getPicture(getContext()));
        } else {
            bodyPartImageView.setImageDrawable(null); // Remove the image, Custom is not managed yet
            bodyPartImageView.setVisibility(View.GONE);
        }

        /* Initialisation des boutons */
        if (mInitialBodyPart.getType()==BodyPartExtensions.TYPE_WEIGHT) {
            nameEdit.ActivateDialog(false);
        }
        nameEdit.setOnTextChangeListener(onTextChangeListener);
        addButton.setOnClickListener(onClickAddMeasure);
        measureList.setOnItemLongClickListener(itemlongclickDeleteRecord);

        /* Initialisation des evenements */
        mBodyMeasureDb = new DAOBodyMeasure(view.getContext());

        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);

        nameEdit.setText(mInitialBodyPart.getName(getContext()));
        bodyToolbar.setNavigationIcon(R.drawable.ic_back);
        bodyToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(onClickToolbarItem);
        if(mInitialBodyPart.getType()== BodyPartExtensions.TYPE_WEIGHT) {
            deleteButton.setVisibility(View.GONE); // Weight bodypart should not be deleted.
        }

        profileViMo = new ViewModelProvider(requireActivity()).get(ProfileViMo.class);
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        profileViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            // Update the UI, in this case, a TextView.
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    private void DrawGraph(List<BodyMeasure> valueList) {

        // Recupere les enregistrements
        if (valueList.size() < 1) {
            mDateGraph.clear();
            return;
        }

        ArrayList<Entry> yVals = new ArrayList<>();

        float minBodyMeasure = -1;

        for (int i = valueList.size() - 1; i >= 0; i--) {
            float normalizedMeasure;
            switch (valueList.get(i).getUnit().getUnitType()){
                case WEIGHT:
                    normalizedMeasure =UnitConverter.weightConverter(valueList.get(i).getBodyMeasure(),valueList.get(i).getUnit(),SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit());
                    break;
                case SIZE:
                    normalizedMeasure =UnitConverter.sizeConverter(valueList.get(i).getBodyMeasure(),valueList.get(i).getUnit(),SettingsFragment.getDefaultSizeUnit(getActivity()));
                    break;
                default:
                    normalizedMeasure=valueList.get(i).getBodyMeasure();
            }

            Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), normalizedMeasure);
            yVals.add(value);
            if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();
        }

        mDateGraph.draw(yVals);
    }

    /*  */
    private void FillRecordTable(List<BodyMeasure> valueList) {
        Cursor oldCursor = null;

        if (valueList.isEmpty()) {
            //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
            measureList.setAdapter(null);
        } else {
            // ...
            if (measureList.getAdapter() == null) {
                BodyMeasureCursorAdapter mTableAdapter = new BodyMeasureCursorAdapter(getActivity(), mBodyMeasureDb.getCursor(), 0, itemClickDeleteRecord);
                measureList.setAdapter(mTableAdapter);
            } else {
                oldCursor = ((BodyMeasureCursorAdapter) measureList.getAdapter()).swapCursor(mBodyMeasureDb.getCursor());
                if (oldCursor != null)
                    oldCursor.close();
            }
        }
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                List<BodyMeasure> valueList = mBodyMeasureDb.getBodyPartMeasuresList(mInitialBodyPart.getId(), getProfile());
                DrawGraph(valueList);
                // update table
                FillRecordTable(valueList);
            }
        }
    }

    private void showDeleteDialog(final long idToDelete) {

        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    mBodyMeasureDb.deleteMeasure(idToDelete);
                    refreshData();
                    Toast.makeText(getActivity(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT)
                        .show();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getText(R.string.DeleteRecordDialog)).setPositiveButton(getResources().getText(R.string.global_yes), dialogClickListener)
            .setNegativeButton(getResources().getText(R.string.global_no), dialogClickListener).show();
    }

    private void showEditDialog(final long idToEdit) {
        BodyMeasure bodyMeasure = mBodyMeasureDb.getMeasure(idToEdit);

        ValueEditorDialogbox editorDialogbox = new ValueEditorDialogbox(getActivity(), bodyMeasure.getDate(), "", bodyMeasure.getBodyMeasure(), getValidUnit(bodyMeasure));
        editorDialogbox.setOnDismissListener(dialog -> {
            if (!editorDialogbox.isCancelled()) {
                Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                Unit unit = Unit.fromString(editorDialogbox.getUnit());

                BodyMeasure updatedBodyMeasure = new BodyMeasure(bodyMeasure.getId(), date, bodyMeasure.getBodyPartID(), value, bodyMeasure.getProfileID(), unit);
                int i = mBodyMeasureDb.updateMeasure(updatedBodyMeasure);
                refreshData();
            }
        });
        editorDialogbox.setOnCancelListener(null);

        editorDialogbox.show();
    }


    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }

    private void requestForSave(View view) {
        boolean toUpdate = false;

        // Save all the fields in the Profile
        switch (view.getId()) {
            case R.id.BODYPART_NAME:
                mInitialBodyPart.setCustomName(nameEdit.getText());
                toUpdate = true;
                break;
            case R.id.BODYPART_LOGO:
                //TODO if it has been deleted, remove the CustomPicture
                mInitialBodyPart.setCustomPicture(mCurrentPhotoPath);
                toUpdate = true;
                break;
        }

        if (toUpdate) {
            mDbBodyPart.update(mInitialBodyPart);
            KToast.infoToast(getActivity(), mInitialBodyPart.getCustomName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Date date = DateConverter.dateToDate(year, month, dayOfMonth);
        if (editDate != null)
            editDate.setText(DateConverter.dateToLocalDateStr(date, getContext()));
    }

    private Unit getValidUnit(BodyMeasure lastBodyMeasure) {
        UnitType unitType = BodyPartExtensions.getUnitType(mInitialBodyPart.getBodyPartResKey());
        if (lastBodyMeasure != null) {
            if (unitType!=lastBodyMeasure.getUnit().getUnitType())
            {
                lastBodyMeasure=null;
            }
        }

        Unit unitDef=Unit.UNITLESS;
        if (lastBodyMeasure == null) {
            switch (unitType) {
                case WEIGHT:
                    unitDef = SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit();
                    break;
                case SIZE:
                    unitDef = SettingsFragment.getDefaultSizeUnit(getActivity());
                    break;
                case PERCENTAGE:
                    unitDef = Unit.PERCENTAGE;
                    break;
            }
        }
        else {
            unitDef=lastBodyMeasure.getUnit();
        }
        return unitDef;
    }
}