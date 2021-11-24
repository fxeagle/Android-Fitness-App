package com.e.easyfitness.fonte;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.e.easyfitness.DAO.record.DAOCardio;
import com.e.easyfitness.DAO.record.DAOFonte;
import com.e.easyfitness.DAO.DAOMachine;
import com.e.easyfitness.DAO.record.DAOStatic;
import com.e.easyfitness.DAO.Machine;
import com.e.easyfitness.DAO.Profile;
import com.e.easyfitness.ProfileViMo;
import com.e.easyfitness.SettingsFragment;
import com.e.easyfitness.enums.DistanceUnit;
import com.e.easyfitness.enums.ExerciseType;
import com.e.easyfitness.enums.WeightUnit;
import com.e.easyfitness.graph.GraphData;
import com.e.easyfitness.MainActivity;
import com.e.easyfitness.R;
import com.e.easyfitness.graph.BarGraph;
import com.e.easyfitness.graph.DateGraph;
import com.e.easyfitness.graph.ZoomType;
import com.e.easyfitness.utils.DateConverter;
import com.e.easyfitness.utils.UnitConverter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class FonteGraphFragment extends Fragment {
    MainActivity mActivity = null;
    ArrayAdapter<String> mAdapterMachine = null;
    //Profile mProfile = null;
    List<String> mMachinesArray = null;
    private String name;
    private int id;
    private Spinner functionList = null;
    private Spinner machineList = null;
    private ZoomType currentZoom = ZoomType.ZOOM_ALL;
    private DateGraph mDateGraph = null;
    private LineChart mLineChart = null;
    private LinearLayout mGraphZoomSelector = null;
    private BarGraph mBarGraph =null;
    private BarChart mBarChart = null;
    private DAOFonte mDbFonte = null;
    private DAOCardio mDbCardio = null;
    private DAOStatic mDbStatic = null;
    private DAOMachine mDbMachine = null;
    private View mFragmentView = null;
    private OnItemSelectedListener onItemSelectedList = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            if (parent.getId() == R.id.filterGraphMachine) {
                updateFunctionSpinner(); // Update functions only when changing exercise
            } else if(parent.getId() == R.id.filterGraphFunction ) {
                saveSharedParams();
            }
            drawGraph();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private OnClickListener onZoomClick = v -> {
        switch (v.getId()) {
            case R.id.allbutton:
                currentZoom = ZoomType.ZOOM_ALL;
                break;
            case R.id.lastweekbutton:
                currentZoom = ZoomType.ZOOM_WEEK;
                break;
            case R.id.lastmonthbutton:
                currentZoom = ZoomType.ZOOM_MONTH;
                break;
            case R.id.lastyearbutton:
                currentZoom = ZoomType.ZOOM_YEAR;
                break;
        }
        mDateGraph.setZoom(currentZoom);
    };

    private ProfileViMo profileViMo;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FonteGraphFragment newInstance(String name, int id) {
        FonteGraphFragment f = new FonteGraphFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_graph, container, false);
        mFragmentView = view;
        functionList = view.findViewById(R.id.filterGraphFunction);
        machineList = view.findViewById(R.id.filterGraphMachine);
        Button allButton = view.findViewById(R.id.allbutton);
        Button lastyearButton = view.findViewById(R.id.lastyearbutton);
        Button lastmonthButton = view.findViewById(R.id.lastmonthbutton);
        Button lastweekButton = view.findViewById(R.id.lastweekbutton);

        /* Initialisation des evenements */
        machineList.setOnItemSelectedListener(onItemSelectedList);
        functionList.setOnItemSelectedListener(onItemSelectedList);

        allButton.setOnClickListener(onZoomClick);
        lastyearButton.setOnClickListener(onZoomClick);
        lastmonthButton.setOnClickListener(onZoomClick);
        lastweekButton.setOnClickListener(onZoomClick);

        /* Initialise le graph */
        mGraphZoomSelector = view.findViewById(R.id.graphZoomSelector);
        mLineChart = view.findViewById(R.id.graphLineChart);
        mDateGraph = new DateGraph(getContext(), mLineChart, getResources().getText(R.string.weightLabel).toString());
        mBarChart = view.findViewById(R.id.graphBarChart);
        mBarGraph = new BarGraph(getContext(), mBarChart, getResources().getText(R.string.weightLabel).toString());

        /* Initialisation de l'historique */
        if (mDbFonte == null) mDbFonte = new DAOFonte(getContext());
        if (mDbCardio == null) mDbCardio = new DAOCardio(getContext());
        if (mDbStatic == null) mDbStatic = new DAOStatic(getContext());
        if (mDbMachine == null) mDbMachine = new DAOMachine(getContext());

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

        if (getProfile() != null) {
            mMachinesArray = new ArrayList<String>(0); //Data are refreshed on show //mDbFonte.getAllMachinesStrList(getProfil());
            // lMachinesArray = prepend(lMachinesArray, "All");
            mAdapterMachine = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item,
                mMachinesArray);
            mAdapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            machineList.setAdapter(mAdapterMachine);
            mDbFonte.closeCursor();
        }

        refreshData();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    public MainActivity getMainActivity() {
        return this.mActivity;
    }

    private void updateFunctionSpinner() {
        if (machineList.getSelectedItem() == null) return;  // List not yet initialized.
        String lMachineStr = machineList.getSelectedItem().toString();
        Machine machine = mDbMachine.getMachine(lMachineStr);
        if (machine == null) return;

        ArrayAdapter<String> adapterFunction = null;
        if (machine.getType() == ExerciseType.STRENGTH ) {
            adapterFunction = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item,
                mActivity.getResources().getStringArray(R.array.graph_functions));
        } else if (machine.getType() == ExerciseType.CARDIO) {
            adapterFunction = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item,
                mActivity.getResources().getStringArray(R.array.graph_cardio_functions));
        } else if (machine.getType() == ExerciseType.ISOMETRIC) {
            adapterFunction = new ArrayAdapter<>( getContext(), android.R.layout.simple_spinner_item,
                mActivity.getResources().getStringArray(R.array.graph_static_functions));
        }
        adapterFunction.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        functionList.setAdapter(adapterFunction);
        if (functionList.getSelectedItemPosition()!=getFunctionListPositionParams()) {
            if (getFunctionListPositionParams() <= (adapterFunction.getCount()-1)) {
                functionList.setSelection(getFunctionListPositionParams());
            }
        }
    }

    private void drawGraph() {

        if (getProfile() == null) return;

        String lMachine = null;
        String lFunction = null;
        int lDAOFunction = 0;

        mLineChart.clear();
        if (machineList.getSelectedItem() == null) {
            return;
        }// Evite les problemes au cas ou il n'y aurait aucune machine d'enregistree
        if (functionList.getSelectedItem() == null) {
            return;
        }

        lMachine = machineList.getSelectedItem().toString();
        lFunction = functionList.getSelectedItem().toString();

        DAOMachine mDbExercise = new DAOMachine(mActivity);
        Machine m = mDbExercise.getMachine(lMachine);
        if (m == null) return;
        ArrayList<Entry> yVals = new ArrayList<>();
        ArrayList<BarEntry> yBarVals = new ArrayList<>();
        String desc = "";

        if (m.getType() == ExerciseType.STRENGTH) {
            if (lFunction.equals(mActivity.getResources().getString(R.string.maxRep1))) {
                lDAOFunction = DAOFonte.MAX1_FCT;
            } else if (lFunction.equals(mActivity.getResources().getString(R.string.maxRep5d))) {
                lDAOFunction = DAOFonte.MAX5_FCT;
            } else if (lFunction.equals(mActivity.getResources().getString(R.string.sum))) {
                lDAOFunction = DAOFonte.SUM_FCT;
            }
            // Recupere les enregistrements
            List<GraphData> valueList = mDbFonte.getBodyBuildingFunctionRecords(getProfile(), lMachine, lDAOFunction);

            if (valueList==null || valueList.size() <= 0) {
                // mLineChart.clear(); Already cleared
                return;
            }

            WeightUnit defaultUnit = SettingsFragment.getDefaultWeightUnit(getMainActivity());

            for (int i = 0; i < valueList.size(); i++) {
                Entry value = null;
                desc = lMachine + "/" + lFunction + "("+ defaultUnit.toString() +")";
                value = new Entry((float) valueList.get(i).getX(), UnitConverter.weightConverter((float) valueList.get(i).getY(), WeightUnit.KG, defaultUnit));//-minDate)/86400000));

                yVals.add(value);
            }

            mBarGraph.getChart().setVisibility(View.GONE);
            mGraphZoomSelector.setVisibility(View.VISIBLE);
            mDateGraph.getChart().setVisibility(View.VISIBLE);
            mDateGraph.setGraphDescription(desc);
            mDateGraph.draw(yVals);
        } else if (m.getType() == ExerciseType.CARDIO) {
            DistanceUnit defaultDistanceUnit = SettingsFragment.getDefaultDistanceUnit(getActivity());

            if (lFunction.equals(mActivity.getResources().getString(R.string.sumDistance))) {
                lDAOFunction = DAOCardio.DISTANCE_FCT;
                desc = lMachine + "/" + lFunction + "("+defaultDistanceUnit.toString()+")";
            } else if (lFunction.equals(mActivity.getResources().getString(R.string.sumDuration))) {
                lDAOFunction = DAOCardio.DURATION_FCT;
                desc = lMachine + "/" + lFunction + "(min)";
            } else if (lFunction.equals(mActivity.getResources().getString(R.string.speed))) {
                lDAOFunction = DAOCardio.SPEED_FCT;
                desc = lMachine + "/" + lFunction + "("+defaultDistanceUnit.toString()+"/h)";
            }

            // Recupere les enregistrements
            List<GraphData> valueList = mDbCardio.getFunctionRecords(getProfile(), lMachine, lDAOFunction);

            if (valueList==null || valueList.size() <= 0) {
                return;
            }

            for (int i = 0; i < valueList.size(); i++) {
                Entry value = null;
                if (lDAOFunction == DAOCardio.DURATION_FCT) {
                    value = new Entry((float) valueList.get(i).getX(), (float) DateConverter.nbMinutes(valueList.get(i).getY()));
                } else if (lDAOFunction == DAOCardio.SPEED_FCT) { // Km/h
                    if ( defaultDistanceUnit == DistanceUnit.MILES)
                        value = new Entry((float) valueList.get(i).getX(), (float) UnitConverter.KmToMiles((float)valueList.get(i).getY()) * (60 * 60 * 1000));
                    else
                        value = new Entry((float) valueList.get(i).getX(), (float) valueList.get(i).getY() * (60 * 60 * 1000));
                } else {
                    if ( defaultDistanceUnit == DistanceUnit.MILES)
                        value = new Entry((float) valueList.get(i).getX(), (float)UnitConverter.KmToMiles((float)valueList.get(i).getY()));
                    else
                        value = new Entry((float) valueList.get(i).getX(), (float)valueList.get(i).getY());
                }
                yVals.add(value);
            }

            mBarGraph.getChart().setVisibility(View.GONE);
            mGraphZoomSelector.setVisibility(View.VISIBLE);
            mDateGraph.getChart().setVisibility(View.VISIBLE);
            mDateGraph.setGraphDescription(desc);
            mDateGraph.draw(yVals);
        } if (m.getType() == ExerciseType.ISOMETRIC) {
            if (lFunction.equals(mActivity.getResources().getString(R.string.maxWeightPerDuration))) {
                lDAOFunction = DAOStatic.MAX_FCT;
            } else if (lFunction.equals(mActivity.getResources().getString(R.string.maxLengthPerDate))) {
                lDAOFunction = DAOStatic.MAX_LENGTH;
            } else if (lFunction.equals(mActivity.getResources().getString(R.string.nbSeriesPerDate))) {
                lDAOFunction = DAOStatic.NBSERIE_FCT;
            }
            desc = lMachine + "/" + lFunction;
            // Recupere les enregistrements
            List<GraphData> valueList = mDbStatic.getStaticFunctionRecords(getProfile(), lMachine, lDAOFunction);

            if (valueList==null || valueList.size() <= 0) {
                // mLineChart.clear(); Already cleared
                return;
            }

            if (lDAOFunction == DAOStatic.MAX_FCT) {
                WeightUnit defaultUnit = SettingsFragment.getDefaultWeightUnit(getMainActivity());

                final ArrayList<String> xAxisLabel = new ArrayList<>();

                for (int i = 0; i < valueList.size(); i++) {
                    BarEntry value = new BarEntry(i, UnitConverter.weightConverter((float) valueList.get(i).getY(), WeightUnit.KG, defaultUnit));
                    xAxisLabel.add(String.valueOf((int) valueList.get(i).getX()));
                    yBarVals.add(value);
                }
                mBarGraph.getChart().setVisibility(View.VISIBLE);
                mGraphZoomSelector.setVisibility(View.GONE);
                mDateGraph.getChart().setVisibility(View.GONE);
                mBarGraph.setGraphDescription(desc);
                mBarGraph.draw(yBarVals, xAxisLabel);

            } else if (lDAOFunction == DAOStatic.NBSERIE_FCT || lDAOFunction == DAOStatic.MAX_LENGTH)  {
                for (int i = 0; i < valueList.size(); i++) {
                    Entry value = new Entry((float) valueList.get(i).getX(), (float) valueList.get(i).getY());
                    yVals.add(value);
                }
                mBarGraph.getChart().setVisibility(View.GONE);
                mGraphZoomSelector.setVisibility(View.VISIBLE);
                mDateGraph.getChart().setVisibility(View.VISIBLE);
                mDateGraph.setGraphDescription(desc);
                mDateGraph.draw(yVals);
            }
        }

        LineChart.LayoutParams layoutParams = mLineChart.getLayoutParams();
        if ( mLineChart.getHeight() > mLineChart.getWidth() ) layoutParams.height = mLineChart.getWidth();
        mLineChart.setLayoutParams(layoutParams);

        BarChart.LayoutParams layoutParamsBar = mBarChart.getLayoutParams();
        if ( mBarChart.getHeight() > mBarChart.getWidth() ) layoutParamsBar.height = mBarChart.getWidth();
        mBarChart.setLayoutParams(layoutParamsBar);
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void refreshData() {
        //View fragmentView = getView();

        if (mFragmentView != null) {
            if (getProfile() != null) {
                //functionList.setOnItemSelectedListener(onItemSelectedList);
                if (mAdapterMachine == null) {
                    mMachinesArray = mDbFonte.getAllMachinesStrList();
                    //Data are refreshed on show
                    mAdapterMachine = new ArrayAdapter<String>(
                        getContext(), android.R.layout.simple_spinner_item,
                        mMachinesArray);
                    mAdapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    machineList.setAdapter(mAdapterMachine);
                } else {
                    /* Initialisation des machines */
                    if (mMachinesArray == null)
                        mMachinesArray = mDbFonte.getAllMachinesStrList();
                    else {
                        mMachinesArray.clear();
                        mMachinesArray.addAll(mDbFonte.getAllMachinesStrList());
                        mAdapterMachine.notifyDataSetChanged();
                        mDbFonte.closeCursor();
                    }
                }

                int position = mAdapterMachine.getPosition(this.getFontesMachine());
                if (position != -1) {
                    if (machineList.getSelectedItemPosition() != position) {
                        machineList.setSelection(position); // Refresh drawing
                    } else {
                        drawGraph();
                    }
                } else {
                    mLineChart.clear();
                }
            }
        }
    }

    private ArrayAdapter<String> getAdapterMachine() {
        ArrayAdapter<String> a;
        mMachinesArray = new ArrayList<String>(0); //Data are refreshed on show //mDbFonte.getAllMachinesStrList(getProfil());
        // lMachinesArray = prepend(lMachinesArray, "All");
        mAdapterMachine = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item,
            mMachinesArray);
        mAdapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        machineList.setAdapter(mAdapterMachine);
        return mAdapterMachine;
    }

    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }

    private String getFontesMachine() {
        return getMainActivity().getCurrentMachine();
    }

    public void saveSharedParams() {
        SharedPreferences sharedPref = this.mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("FunctionListPosition", functionList.getSelectedItemPosition());
        editor.apply();
    }

    public String getSharedParams(String paramName) {
        SharedPreferences sharedPref = this.mActivity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(paramName, "");
    }

    public int getFunctionListPositionParams() {
        SharedPreferences sharedPref = this.mActivity.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt("FunctionListPosition", 0);
    }

}