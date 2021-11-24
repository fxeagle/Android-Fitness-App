package com.e.easyfitness;


import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.e.easyfitness.DAO.DAOProfile;
import com.e.easyfitness.DAO.DAOProfileWeight;
import com.e.easyfitness.DAO.Profile;
import com.e.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.e.easyfitness.DAO.bodymeasures.BodyPart;
import com.e.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.e.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.e.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.e.easyfitness.bodymeasures.BodyPartDetailsFragment;
import com.e.easyfitness.enums.Unit;
import com.e.easyfitness.graph.MiniDateGraph;
import com.e.easyfitness.utils.DateConverter;
import com.e.easyfitness.utils.UnitConverter;
import com.e.easyfitness.views.EditableInputView;
import com.e.easyfitness.views.EditableInputViewWithDate;
import com.e.easyfitness.utils.Gender;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.lifecycle.ViewModelProvider;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class WeightFragment extends Fragment {
    MainActivity mActivity = null;
    private TextView weightEdit = null;
    private TextView fatEdit = null;
    private TextView musclesEdit = null;
    private TextView waterEdit = null;
    private TextView imcText = null;
    private TextView imcRank = null;
    private TextView ffmiText = null;
    private TextView ffmiRank = null;
    private TextView rfmText = null;
    private TextView rfmRank = null;

    private LineChart mWeightLineChart;
    private LineChart mFatLineChart;
    private LineChart mMusclesLineChart;
    private LineChart mWaterLineChart;


    private DAOProfileWeight mWeightDb = null;
    private DAOBodyMeasure mDbBodyMeasure = null;
    private DAOBodyPart mDbBodyPart;
    private DAOProfile mDb = null;


    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ValueEditorDialogbox editorDialogbox;
            switch (view.getId()) {
                case R.id.weightInput:
                    BodyMeasure lastWeightValue = mDbBodyMeasure.getLastBodyMeasures(weightBobyPart.getId(), getProfile());
                    if (lastWeightValue == null) {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", 0, SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit());
                    } else {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", lastWeightValue.getBodyMeasure(), lastWeightValue.getUnit());
                    }
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                            Unit unit = Unit.fromString(editorDialogbox.getUnit());
                            mDbBodyMeasure.addBodyMeasure(date, BodyPartExtensions.WEIGHT, value, getProfile().getId(), unit);
                            refreshData();
                        }
                    });
                    editorDialogbox.show();
                    break;
                case R.id.fatInput:
                    BodyMeasure lastFatValue = mDbBodyMeasure.getLastBodyMeasures(fatBobyPart.getId(), getProfile());
                    if (lastFatValue == null) {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", 0, Unit.PERCENTAGE);
                    } else {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", lastFatValue.getBodyMeasure(), Unit.PERCENTAGE);
                    }
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                            mDbBodyMeasure.addBodyMeasure(date, BodyPartExtensions.FAT, value, getProfile().getId(), Unit.PERCENTAGE);
                            refreshData();
                        }
                    });
                    editorDialogbox.setOnCancelListener(null);
                    editorDialogbox.show();
                    break;
                case R.id.musclesInput:
                    BodyMeasure lastMusclesValue = mDbBodyMeasure.getLastBodyMeasures(musclesBobyPart.getId(), getProfile());
                    if (lastMusclesValue == null) {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", 0, Unit.PERCENTAGE);
                    } else {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", lastMusclesValue.getBodyMeasure(), Unit.PERCENTAGE);
                    }
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                            mDbBodyMeasure.addBodyMeasure(date, BodyPartExtensions.MUSCLES, value, getProfile().getId(), Unit.PERCENTAGE);
                            refreshData();
                        }
                    });
                    editorDialogbox.setOnCancelListener(null);
                    editorDialogbox.show();
                    break;
                case R.id.waterInput:
                    BodyMeasure lastWaterValue = mDbBodyMeasure.getLastBodyMeasures(waterBobyPart.getId(), getProfile());
                    if (lastWaterValue == null) {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", 0, Unit.PERCENTAGE);
                    } else {
                        editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", lastWaterValue.getBodyMeasure(), Unit.PERCENTAGE);
                    }
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                            mDbBodyMeasure.addBodyMeasure(date, BodyPartExtensions.WATER, value, getProfile().getId(), Unit.PERCENTAGE);
                            refreshData();
                        }
                    });
                    editorDialogbox.setOnCancelListener(null);
                    editorDialogbox.show();
                    break;
            }
        }
    };

    private AdapterView.OnClickListener showDetailsFragment = v -> {
        int bodyPartID = BodyPartExtensions.WEIGHT;
        switch (v.getId()) {
            case R.id.weightGraph:
            case R.id.weightDetailsButton:
                bodyPartID = BodyPartExtensions.WEIGHT;
                break;
            case R.id.fatGraph:
            case R.id.fatDetailsButton:
                bodyPartID = BodyPartExtensions.FAT;
                break;
            case R.id.musclesGraph:
            case R.id.musclesDetailsButton:
                bodyPartID = BodyPartExtensions.MUSCLES;
                break;
            case R.id.waterGraph:
            case R.id.waterDetailsButton:
                bodyPartID = BodyPartExtensions.WATER;
                break;
        }

        BodyPartDetailsFragment fragment = BodyPartDetailsFragment.newInstance(bodyPartID, false);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, MainActivity.BODYTRACKINGDETAILS);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    };

    private OnClickListener showHelp = v -> {
        switch (v.getId()) {
            case R.id.imcHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(R.string.BMI_dialog_title)
                    .setContentText(getString(R.string.BMI_formula))
                    .setConfirmText(getResources().getText(R.string.global_ok).toString())
                    .showCancelButton(true)
                    .show();
                break;
            case R.id.ffmiHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(R.string.FFMI_dialog_title)
                    .setContentText(getString(R.string.FFMI_formula))
                    .setConfirmText(getResources().getText(R.string.global_ok).toString())
                    .showCancelButton(true)
                    .show();
                break;
            case R.id.rfmHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(R.string.RFM_dialog_title)
                    .setContentText(getString(R.string.RFM_female_formula) +
                        getString(R.string.RFM_male_formula))
                    .setConfirmText(getResources().getText(R.string.global_ok).toString())
                    .showCancelButton(true)
                    .show();
                break;
        }
    };

    private MiniDateGraph mWeightGraph;
    private MiniDateGraph mFatGraph;
    private MiniDateGraph mMusclesGraph;
    private MiniDateGraph mWaterGraph;
    private BodyPart weightBobyPart;
    private BodyPart fatBobyPart;
    private BodyPart musclesBobyPart;
    private BodyPart waterBobyPart;

    private ProfileViMo profileViMo;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static WeightFragment newInstance(String name, int id) {
        WeightFragment f = new WeightFragment();

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
        View view = inflater.inflate(R.layout.tab_weight, container, false);

        /* Views Initialisation */
        weightEdit = view.findViewById(R.id.weightInput);
        fatEdit = view.findViewById(R.id.fatInput);
        musclesEdit = view.findViewById(R.id.musclesInput);
        waterEdit = view.findViewById(R.id.waterInput);
        Button weightDetailsButton = view.findViewById(R.id.weightDetailsButton);
        Button fatDetailsButton = view.findViewById(R.id.fatDetailsButton);
        Button musclesDetailsButton = view.findViewById(R.id.musclesDetailsButton);
        Button waterDetailsButton = view.findViewById(R.id.waterDetailsButton);
        imcText = view.findViewById(R.id.imcValue);
        imcRank = view.findViewById(R.id.imcViewText);
        ffmiText = view.findViewById(R.id.ffmiValue);
        ffmiRank = view.findViewById(R.id.ffmiViewText);
        rfmText = view.findViewById(R.id.rfmValue);
        rfmRank = view.findViewById(R.id.rfmViewText);

        ImageButton ffmiHelpButton = view.findViewById(R.id.ffmiHelp);
        ImageButton imcHelpButton = view.findViewById(R.id.imcHelp);
        ImageButton rfmHelpButton = view.findViewById(R.id.rfmHelp);

        /* Initialisation des evenements */
        weightEdit.setOnClickListener(mOnClickListener);
        fatEdit.setOnClickListener(mOnClickListener);
        musclesEdit.setOnClickListener(mOnClickListener);
        waterEdit.setOnClickListener(mOnClickListener);
        imcHelpButton.setOnClickListener(showHelp);
        ffmiHelpButton.setOnClickListener(showHelp);
        rfmHelpButton.setOnClickListener(showHelp);
        weightDetailsButton.setOnClickListener(showDetailsFragment);
        fatDetailsButton.setOnClickListener(showDetailsFragment);
        musclesDetailsButton.setOnClickListener(showDetailsFragment);
        waterDetailsButton.setOnClickListener(showDetailsFragment);

        mWeightDb = new DAOProfileWeight(view.getContext());
        mDbBodyPart = new DAOBodyPart(view.getContext());
        mDbBodyMeasure = new DAOBodyMeasure(view.getContext());

        mWeightLineChart = view.findViewById(R.id.weightGraph);
        mWeightGraph = new MiniDateGraph(getContext(), mWeightLineChart, "");
        mWeightGraph.getChart().setOnClickListener(showDetailsFragment);
        weightBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.WEIGHT);

        mFatLineChart = view.findViewById(R.id.fatGraph);
        mFatGraph = new MiniDateGraph(getContext(), mFatLineChart, "");
        mFatGraph.getChart().setOnClickListener(showDetailsFragment);
        fatBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.FAT);

        mMusclesLineChart = view.findViewById(R.id.musclesGraph);
        mMusclesGraph = new MiniDateGraph(getContext(), mMusclesLineChart, "");
        mMusclesGraph.getChart().setOnClickListener(showDetailsFragment);
        musclesBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.MUSCLES);

        mWaterLineChart = view.findViewById(R.id.waterGraph);
        mWaterGraph = new MiniDateGraph(getContext(), mWaterLineChart, "");
        mWaterGraph.getChart().setOnClickListener(showDetailsFragment);
        waterBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.WATER);

        profileViMo = new ViewModelProvider(requireActivity()).get(ProfileViMo.class);
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        profileViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            // Update the UI, in this case, a TextView.
            refreshData();
        });

        return view;
    }


    private void DrawGraph() {
        if (getView()==null) return;
        getView().post(() -> {
            if ( weightBobyPart == null ) return;

            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(weightBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mWeightLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if (valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), UnitConverter.weightConverter(valueList.get(i).getBodyMeasure(),valueList.get(i).getUnit(), Unit.KG));
                    yVals.add(value);
                }

                mWeightGraph.draw(yVals);
            }

        });

        getView().post(() -> {
            if (fatBobyPart==null) return;

            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(fatBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mFatLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if ( valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure());
                    yVals.add(value);
                }

                mFatGraph.draw(yVals);
            }
        });
        getView().post(() -> {
            if (musclesBobyPart==null) return;
            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(musclesBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mMusclesLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if ( valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure());
                    yVals.add(value);
                }

                mMusclesGraph.draw(yVals);
            }
        });

        getView().post(() -> {
            if (waterBobyPart==null) return;
            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(waterBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mWaterLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if ( valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure());
                    yVals.add(value);
                }

                mWaterGraph.draw(yVals);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    public String getName() {
        return getArguments().getString("name");
    }

    /**
     * @param weight in kg
     * @param size   in cm
     * @return
     */
    private float calculateImc(float weight, int size) {
        float imc = 0;

        if (size == 0) return 0;

        imc = (float) (weight / (size / 100.0 * size / 100.0));

        return imc;
    }

    /**
     * @param imc
     * @return text associated with imc value
     */
    private String getImcText(float imc) {
        if (imc < 18.5) {
            return getString(R.string.underweight);
        } else if (imc < 25) {
            return getString(R.string.normal);
        } else if (imc < 30) {
            return getString(R.string.overweight);
        } else {
            return getString(R.string.obese);
        }
    }

    private float calculateRfm(float waistCirc, int sex, int size) {
        float rfm = 0;

        if (waistCirc == 0) return 0;

        return 0;
    }

    /**
     * @param rfm index
     * @return text associated with Rfm value
     */
    private String getRfmText(float rfm) {
        if (rfm < 18.5) {
            return "underweight";
        } else if (rfm < 25) {
            return "normal";
        } else if (rfm < 30) {
            return "overweight";
        } else {
            return "obese";
        }
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private double calculateFfmi(float weight, int size, float bodyFat) {
        double ffmi = 0;

        if (bodyFat == 0) return 0;

        ffmi = weight * (1-(bodyFat/100)) / (size/ 100.0*size/ 100.0);

        return ffmi;
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private double calculateNormalizedFfmi(float weight, int size, float bodyFat) {
        double ffmi = 0;

        if (bodyFat == 0) return 0;

        ffmi = weight * (1-(bodyFat/100)) / (size*size) + 6.1*(1.8-size);

        return ffmi;
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average           *
     * 20 - 21: above average     *
     * 22: excellent              *
     * 23 – 25: superior          *
     * 26 – 27: scores considered suspicious but still attainable naturally     */
    private String getFfmiTextForMen(double ffmi) {
        if (ffmi < 17) {
            return "below average";
        } else if (ffmi < 19) {
            return "average";
        } else if (ffmi < 21) {
            return "above average";
        }else if (ffmi < 23) {
            return "excellent";
        }else if (ffmi < 25) {
            return "superior";
        }else if (ffmi < 27) {
            return "suspicious";
        } else {
            return "very suspicious";
        }
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average     *
     * 20 - 21: above average     *
     * 22: excellent     *
     * 23 – 25: superior     *
     * 26 – 27: scores considered suspicious but still attainable naturally     */
    private String getFfmiTextForWomen(double ffmi) {
        if (ffmi < 14) {
            return "below average";
        } else if (ffmi < 16) {
            return "average";
        } else if (ffmi < 18) {
            return "above average";
        }else if (ffmi < 20) {
            return "excellent";
        }else if (ffmi < 22) {
            return "superior";
        }else if (ffmi < 24) {
            return "suspicious";
        } else {
            return "very suspicious";
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                BodyMeasure lastWeightValue = null;
                BodyMeasure lastWaterValue = null;
                BodyMeasure lastFatValue = null;
                BodyMeasure lastMusclesValue = null;

                lastWeightValue = mDbBodyMeasure.getLastBodyMeasures(weightBobyPart.getId(), getProfile());
                lastWaterValue = mDbBodyMeasure.getLastBodyMeasures(waterBobyPart.getId(), getProfile());
                lastFatValue = mDbBodyMeasure.getLastBodyMeasures(fatBobyPart.getId(), getProfile());
                lastMusclesValue = mDbBodyMeasure.getLastBodyMeasures(musclesBobyPart.getId(), getProfile());

                if (lastWeightValue != null) {
                    String editText = String.format("%.1f", lastWeightValue.getBodyMeasure()) + lastWeightValue.getUnit().toString();

                    weightEdit.setText(editText);
                    // update IMC
                    int size = getProfile().getSize();
                    if (size == 0) {
                        imcText.setText("-");
                        imcRank.setText(R.string.no_size_available);
                        ffmiText.setText("-");
                        ffmiRank.setText(R.string.no_size_available);
                    } else {
                        float imcValue = calculateImc(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure(),lastWeightValue.getUnit(),Unit.KG), size);
                        imcText.setText(String.format("%.1f", imcValue));
                        imcRank.setText(getImcText(imcValue));
                        if (lastFatValue!=null) {
                            double ffmiValue = calculateFfmi(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure(),lastWeightValue.getUnit(),Unit.KG), size, lastFatValue.getBodyMeasure() );
                            ffmiText.setText(String.format("%.1f", ffmiValue));
                            if(getProfile().getGender()== Gender.FEMALE)
                                ffmiRank.setText(getFfmiTextForWomen(ffmiValue));
                            else if(getProfile().getGender()== Gender.MALE)
                                ffmiRank.setText(getFfmiTextForMen(ffmiValue));
                            else if(getProfile().getGender()== Gender.OTHER)
                                ffmiRank.setText(getFfmiTextForMen(ffmiValue));
                            else
                                ffmiRank.setText("no gender defined");
                        } else {
                            ffmiText.setText("-");
                            ffmiRank.setText(R.string.no_fat_available);
                        }

                    }
                } else {
                    weightEdit.setText("-");
                    imcText.setText("-");
                    imcRank.setText(R.string.no_weight_available);
                    ffmiText.setText("-");
                    ffmiRank.setText(R.string.no_weight_available);
                }

                if (lastWaterValue != null) {
                    String editText = String.format("%.1f", lastWaterValue.getBodyMeasure()) + lastWaterValue.getUnit().toString();
                    waterEdit.setText(editText);
                }
                else
                    waterEdit.setText("-");

                if (lastFatValue != null) {
                    String editText = String.format("%.1f", lastFatValue.getBodyMeasure()) + lastFatValue.getUnit().toString();
                    fatEdit.setText(editText);
                } else
                    fatEdit.setText("-");

                if (lastMusclesValue != null) {
                    String editText = String.format("%.1f", lastMusclesValue.getBodyMeasure()) + lastMusclesValue.getUnit().toString();
                    musclesEdit.setText(editText);
                }
                else
                    musclesEdit.setText("-");


                DrawGraph();
            }
        }
    }

    private Profile getProfile() {
        return profileViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }
}