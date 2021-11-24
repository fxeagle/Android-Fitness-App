package com.e.easyfitness;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.e.easyfitness.DAO.CVSManager;
import com.e.easyfitness.DAO.DAOMachine;
import com.e.easyfitness.DAO.DAOProfile;
import com.e.easyfitness.DAO.DatabaseHelper;
import com.e.easyfitness.DAO.Machine;
import com.e.easyfitness.DAO.Profile;
import com.e.easyfitness.DAO.cardio.DAOOldCardio;
import com.e.easyfitness.DAO.cardio.OldCardio;
import com.e.easyfitness.DAO.program.DAOProgram;
import com.e.easyfitness.DAO.record.DAOCardio;
import com.e.easyfitness.DAO.record.DAOFonte;
import com.e.easyfitness.DAO.record.DAORecord;
import com.e.easyfitness.DAO.record.DAOStatic;
import com.e.easyfitness.DAO.record.Record;
import com.e.easyfitness.bodymeasures.BodyPartListFragment;
import com.e.easyfitness.enums.DistanceUnit;
import com.e.easyfitness.enums.ExerciseType;
import com.e.easyfitness.enums.WeightUnit;
import com.e.easyfitness.fonte.FontesPagerFragment;
import com.e.easyfitness.intro.MainIntroActivity;
import com.e.easyfitness.machines.MachineFragment;
import com.e.easyfitness.programs.ProgramListFragment;
import com.e.easyfitness.utils.CustomExceptionHandler;
import com.e.easyfitness.utils.DateConverter;
import com.e.easyfitness.utils.FileChooserDialog;
import com.e.easyfitness.utils.ImageUtil;
import com.e.easyfitness.utils.MusicController;
import com.e.easyfitness.utils.UnitConverter;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    public static String FONTESPAGER = "FontePager";
    public static String WEIGHT = "Weight";
    public static String PROFILE = "Profile";
    public static String BODYTRACKING = "BodyTracking";
    public static String BODYTRACKINGDETAILS = "BodyTrackingDetail";
    public static String ABOUT = "About";
    public static String SETTINGS = "Settings";
    public static String MACHINES = "Machines";
    public static String MACHINESDETAILS = "MachinesDetails";
    public static String WORKOUTS = "Workouts";
    public static String WORKOUTPAGER = "WorkoutPager";
    public static String PREFS_NAME = "prefsfile";
    private final int REQUEST_CODE_INTRO = 111;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    CustomDrawerAdapter mDrawerAdapter;
    List<DrawerItem> dataList;

    /* Fragments */
    private FontesPagerFragment mpFontesPagerFrag = null;
    private WeightFragment mpWeightFrag = null;
    private ProfileFragment mpProfileFrag = null;
    private MachineFragment mpMachineFrag = null;
    private SettingsFragment mpSettingFrag = null;
    private AboutFragment mpAboutFrag = null;
    private BodyPartListFragment mpBodyPartListFrag = null;
    private ProgramListFragment mpWorkoutListFrag;

    private String currentFragmentName = "";
    private DAOProfile mDbProfils = null;
    private Profile mCurrentProfile = null;
    private long mCurrentProfilID = -1;
    private String m_importCVSchosenDir = "";
    private Toolbar top_toolbar = null;

    /* Navigation Drawer */
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private MusicController musicController = new MusicController(this);
    private CircularImageView roundProfile = null;
    private String mCurrentMachine = "";
    private boolean mIntro014Launched = false;
    private boolean mMigrationBD15done = false;
    private long mBackPressed;
    private ProfileViMo profileViMo;

    private PopupMenu.OnMenuItemClickListener onMenuItemClick = item -> {
        switch (item.getItemId()) {
            case R.id.create_newprofil:
                getActivity().CreateNewProfil();
                return true;
            case R.id.change_profil:
                String[] profilListArray = getActivity().mDbProfils.getAllProfil();

                AlertDialog.Builder changeProfilbuilder = new AlertDialog.Builder(getActivity());
                changeProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil))
                    .setItems(profilListArray, (dialog, which) -> {
                        ListView lv = ((AlertDialog) dialog).getListView();
                        Object checkedItem = lv.getAdapter().getItem(which);
                        setCurrentProfil(checkedItem.toString());
                        KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        //Toast.makeText(getApplicationContext(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Toast.LENGTH_LONG).show();
                    });
                changeProfilbuilder.show();
                return true;
            case R.id.delete_profil:
                String[] profildeleteListArray = getActivity().mDbProfils.getAllProfil();

                AlertDialog.Builder deleteProfilbuilder = new AlertDialog.Builder(getActivity());
                deleteProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil_to_delete))
                    .setItems(profildeleteListArray, (dialog, which) -> {
                        ListView lv = ((AlertDialog) dialog).getListView();
                        Object checkedItem = lv.getAdapter().getItem(which);
                        if (getCurrentProfile().getName().equals(checkedItem.toString())) {
                            KToast.errorToast(getActivity(), getActivity().getResources().getText(R.string.impossibleToDeleteProfile).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        } else {
                            Profile profileToDelete = mDbProfils.getProfil(checkedItem.toString());
                            mDbProfils.deleteProfil(profileToDelete);
                            KToast.infoToast(getActivity(), getString(R.string.profileDeleted) + ":" + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        }
                    });
                deleteProfilbuilder.show();
                return true;
            case R.id.rename_profil:
                getActivity().renameProfil();
                return true;
            case R.id.param_profil:
                showFragment(PROFILE);
                return true;
            default:
                return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.e("Starting MainActivity", "Starting MainActivity");

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String dayNightAuto = SP.getString("dayNightAuto", "2");
        int dayNightAutoValue;
        try {
            dayNightAutoValue = Integer.parseInt(dayNightAuto);
        }catch(NumberFormatException e) {
            dayNightAutoValue = 2;
        }
        if(dayNightAutoValue == getResources().getInteger(R.integer.dark_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            SweetAlertDialog.DARK_STYLE=true;
        } else if (dayNightAutoValue == getResources().getInteger(R.integer.light_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            SweetAlertDialog.DARK_STYLE=false;
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    SweetAlertDialog.DARK_STYLE=false; break;
                case Configuration.UI_MODE_NIGHT_YES:
                    SweetAlertDialog.DARK_STYLE=true; break;
                default:
                    SweetAlertDialog.DARK_STYLE=false;
            }
        }

        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {

            /* creation de l'arborescence de l'application */
            File folder = new File(Environment.getExternalStorageDirectory() + "/EasyFitness");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (success) {
                folder = new File(Environment.getExternalStorageDirectory() + "/EasyFitness/crashreport");
                success = folder.mkdir();
            }

            if (folder.exists()) {
                if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                    Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                        Environment.getExternalStorageDirectory() + "/EasyFitness/crashreport"));
                }
            }
        }

        setContentView(R.layout.activity_main);

        top_toolbar = this.findViewById(R.id.actionToolbar);
        setSupportActionBar(top_toolbar);
        top_toolbar.setTitle(getResources().getText(R.string.app_name));

        if (savedInstanceState == null) {
            if (mpFontesPagerFrag == null) mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);
            if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);
            if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);
            if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);
            if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 4);
            if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
            if (mpBodyPartListFrag == null) mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);
            if (mpWorkoutListFrag == null) mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 11);
        } else {
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, FONTESPAGER);
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().getFragment(savedInstanceState, WEIGHT);
            mpProfileFrag = (ProfileFragment) getSupportFragmentManager().getFragment(savedInstanceState, PROFILE);
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, SETTINGS);
            mpAboutFrag = (AboutFragment) getSupportFragmentManager().getFragment(savedInstanceState, ABOUT);
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().getFragment(savedInstanceState, MACHINES);
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().getFragment(savedInstanceState, BODYTRACKING);
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().getFragment(savedInstanceState, WORKOUTS);
        }

        loadPreferences();

        profileViMo = new ViewModelProvider(this).get(ProfileViMo.class);
        profileViMo.getProfile().observe(this, profile -> {
            // Update UI
            setDrawerTitle(profile.getName());
            setPhotoProfile(profile.getPhoto());
            mCurrentProfilID = profile.getId();
            savePreferences();
        });

        DatabaseHelper.renameOldDatabase(this);

        if (DatabaseHelper.DATABASE_VERSION >= 15 && !mMigrationBD15done) {
            DAOOldCardio mDbOldCardio = new DAOOldCardio(this);
            DAOMachine lDAOMachine = new DAOMachine(this);
            if (mDbOldCardio.tableExists()) {
                DAOCardio mDbCardio = new DAOCardio(this);
                List<OldCardio> mList = mDbOldCardio.getAllRecords();
                for (OldCardio record : mList) {
                    String exerciseName = "";
                    Machine m = lDAOMachine.getMachine(record.getExercice());
                    exerciseName = record.getExercice();
                    if (m != null) { // if a machine exists
                        if (m.getType() == ExerciseType.STRENGTH) { // if it is not a Cardio type
                            exerciseName = exerciseName + "-Cardio"; // add a suffix to
                        }
                    }

                    mDbCardio.addCardioRecord(record.getDate(), exerciseName, record.getDistance(), record.getDuration(), record.getProfil().getId(), DistanceUnit.KM, -1);
                }
                mDbOldCardio.dropTable();

                DAORecord daoRecord = new DAORecord(this);
                List<Record> mFonteList = daoRecord.getAllRecords();
                for (Record record : mFonteList) {
                    record.setExerciseType(ExerciseType.STRENGTH);
                    daoRecord.updateRecord(record); // Automatically update record Type
                }
                ArrayList<Machine> machineList = lDAOMachine.getAllMachinesArray();
                for (Machine record : machineList) {
                    lDAOMachine.updateMachine(record); // Reset all the fields on machines.
                }
            }
            mMigrationBD15done = true;
            savePreferences();
        }

        if (savedInstanceState == null) {
            showFragment(FONTESPAGER, false); // Create fragment, do not add to backstack
            currentFragmentName = FONTESPAGER;
        }

        dataList = new ArrayList<>();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        DrawerItem drawerTitleItem = new DrawerItem("TITLE", R.drawable.ic_person_black_24dp, true);

        dataList.add(drawerTitleItem);
        dataList.add(new DrawerItem(this.getResources().getString(R.string.menu_Workout), R.drawable.ic_fitness_center_white_24dp, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.MachinesLabel), R.drawable.ic_gym_bench_50dp, true));
        dataList.add(new DrawerItem("Programs List", R.drawable.ic_exam, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.weightMenuLabel), R.drawable.ic_bathroom_scale_white_50dp, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.bodytracking), R.drawable.ic_ruler_white_50dp, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.SettingLabel), R.drawable.ic_settings_white_24dp, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.AboutLabel), R.drawable.ic_info_outline_white_24dp, true));

        mDrawerAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
            dataList);

        mDrawerList.setAdapter(mDrawerAdapter);

        roundProfile = top_toolbar.findViewById(R.id.imageProfile);

        mDrawerToggle = new ActionBarDrawerToggle(
            this,                  /* host Activity */
            mDrawerLayout,         /* DrawerLayout object */
            top_toolbar,  /* nav drawer icon to replace 'Up' caret */
            R.string.drawer_open, R.string.drawer_close
        );

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        musicController.initView();


        if (!mIntro014Launched) {
            Intent intent = new Intent(this, MainIntroActivity.class);
            startActivityForResult(intent, REQUEST_CODE_INTRO);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first

        if (mIntro014Launched) {
            initActivity();
            initDEBUGdata();
        }

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean bShowMP3 = SP.getBoolean("prefShowMP3", false);
        this.showMP3Toolbar(bShowMP3);
    }

    private void initDEBUGdata() {
        if (BuildConfig.DEBUG_MODE) {
            // do something for a debug build
            DAOFonte lDbFonte = new DAOFonte(this);
            if(lDbFonte.getCount()==0) {
                lDbFonte.addBodyBuildingRecord(DateConverter.dateToDate(2019, 07, 01, 12, 34, 56), "Example 1", 1, 10, 40, WeightUnit.KG, "", this.getCurrentProfile().getId(), -1);
                lDbFonte.addBodyBuildingRecord(DateConverter.dateToDate(2019, 06, 30, 12, 34, 56), "Example 2", 1, 10, UnitConverter.LbstoKg(60), WeightUnit.LBS, "", this.getCurrentProfile().getId(), -1);
            }
            DAOCardio lDbCardio = new DAOCardio(this);
            if(lDbCardio.getCount()==0) {
                lDbCardio.addCardioRecord(DateConverter.dateToDate(2019, 07, 01), "Running Easy Fitness", 1, 10000, this.getCurrentProfile().getId(), DistanceUnit.KM, -1);
                lDbCardio.addCardioRecord(DateConverter.dateToDate(2019, 07, 31), "Cardio Example", UnitConverter.MilesToKm(2), 20000, this.getCurrentProfile().getId(), DistanceUnit.MILES, -1);
            }
            DAOStatic lDbStatic = new DAOStatic(this);
            if(lDbStatic.getCount()==0) {
                lDbStatic.addStaticRecord(DateConverter.dateToDate(2019, 07, 01, 12, 34, 56), "Exercise ISO 1", 1, 50, 40, this.getCurrentProfile().getId(), WeightUnit.KG, "", -1);
                lDbStatic.addStaticRecord(DateConverter.dateToDate(2019, 07, 31, 12, 34, 56), "Exercise ISO 2", 1, 60, UnitConverter.LbstoKg(40), this.getCurrentProfile().getId(), WeightUnit.LBS, "", -1);
            }
            DAOProgram lDbWorkout = new DAOProgram(this);
            if(lDbWorkout.getCount()==0) {
                lDbWorkout.populate();
            }
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        if (getFontesPagerFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, FONTESPAGER, mpFontesPagerFrag);
        if (getWeightFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WEIGHT, mpWeightFrag);
        if (getProfileFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, PROFILE, mpProfileFrag);
        if (getMachineFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, MACHINES, mpMachineFrag);
        if (getAboutFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, ABOUT, mpAboutFrag);
        if (getSettingsFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, SETTINGS, mpSettingFrag);
        if (getBodyPartFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, BODYTRACKING, mpBodyPartListFrag);
        if (getWorkoutListFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WORKOUTS, mpWorkoutListFrag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        // restore the profile picture in case it was overwritten during the menu inflate
        if (mCurrentProfile != null) setPhotoProfile(mCurrentProfile.getPhoto());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.action_profil);

        roundProfile.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.profile_actions, popup.getMenu());
            popup.setOnMenuItemClickListener(onMenuItemClick);
            popup.show();
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void exportDatabase() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            AlertDialog.Builder exportDbBuilder = new AlertDialog.Builder(this);

            exportDbBuilder.setTitle(getActivity().getResources().getText(R.string.export_database));
            exportDbBuilder.setMessage(getActivity().getResources().getText(R.string.export_question) + " " + getCurrentProfile().getName() + "?");

            exportDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
                CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
                if (cvsMan.exportDatabase(getCurrentProfile())) {
                    KToast.successToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.export_success), Gravity.BOTTOM, KToast.LENGTH_LONG);
                } else {
                    KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.export_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
                }

                // Do nothing but close the dialog
                dialog.dismiss();
            });

            exportDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), (dialog, which) -> {
                // Do nothing
                dialog.dismiss();
            });

            AlertDialog exportDbDialog = exportDbBuilder.create();
            exportDbDialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.export_database:
                exportDatabase();
                return true;
            case R.id.import_database:
                // Create DirectoryChooserDialog and register a callback
                FileChooserDialog fileChooserDialog =
                    new FileChooserDialog(this, chosenDir -> {
                        m_importCVSchosenDir = chosenDir;
                        //Toast.makeText(getActivity().getBaseContext(), "Chosen directory: " +
                        //    chosenDir, Toast.LENGTH_LONG).show();
                        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(this.getResources().getString(R.string.global_confirm_question))
                            .setContentText(this.getResources().getString(R.string.import_new_exercise_first))
                            .setConfirmText(this.getResources().getString(R.string.global_yes))
                            .setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();
                                CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
                                if(cvsMan.importDatabase(m_importCVSchosenDir, getCurrentProfile())) {
                                    KToast.successToast(getActivity(), m_importCVSchosenDir + " " + getActivity().getResources().getString(R.string.imported_successfully), Gravity.BOTTOM, KToast.LENGTH_SHORT );
                                } else {
                                    KToast.errorToast(getActivity(), m_importCVSchosenDir + " " + getActivity().getResources().getString(R.string.import_failed), Gravity.BOTTOM, KToast.LENGTH_SHORT );
                                }
                                setCurrentProfil(getCurrentProfile()); // Refresh profile
                            })
                            .setCancelText(this.getResources().getString(R.string.global_no))
                            .show();

                    });

                fileChooserDialog.setFileFilter("csv");
                fileChooserDialog.chooseDirectory(Environment.getExternalStorageDirectory() + "/EasyFitness/export");
                return true;
            case R.id.action_deleteDB:
                AlertDialog.Builder deleteDbBuilder = new AlertDialog.Builder(this);

                deleteDbBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
                deleteDbBuilder.setMessage(getActivity().getResources().getText(R.string.deleteDB_warning));

                // Si oui, supprimer la base de donnee et refaire un Start.
                deleteDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
                    // recupere le premier ID de la liste.
                    List<Profile> lList = mDbProfils.getAllProfils();
                    for (int i = 0; i < lList.size(); i++) {
                        Profile mTempProfile = lList.get(i);
                        mDbProfils.deleteProfil(mTempProfile.getId());
                    }
                    DAOMachine mDbMachines = new DAOMachine(getActivity());
                    // recupere le premier ID de la liste.
                    List<Machine> lList2 = mDbMachines.getAllMachinesArray();
                    for (int i = 0; i < lList2.size(); i++) {
                        Machine mTemp = lList2.get(i);
                        mDbMachines.delete(mTemp.getId());
                    }

                    // redisplay the intro
                    mIntro014Launched=false;

                    // Do nothing but close the dialog
                    dialog.dismiss();

                    finish();
                });

                deleteDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), (dialog, which) -> {
                    // Do nothing
                    dialog.dismiss();
                });

                AlertDialog deleteDbDialog = deleteDbBuilder.create();
                deleteDbDialog.show();

                return true;
            case R.id.action_apropos:
                // Display the fragment as the main content.
                showFragment(ABOUT);
                //getAboutFragment().setHasOptionsMenu(true);
                return true;
            //case android.R.id.home:
            //onBackPressed();
            //	return true;
            case R.id.action_chrono:
                ChronoDialogbox cdd = new ChronoDialogbox(MainActivity.this);
                cdd.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                exportDatabase();
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT);

            }
        }
    }

    public boolean CreateNewProfil() {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(this);

        newProfilBuilder.setTitle(getActivity().getResources().getText(R.string.createProfilTitle));
        newProfilBuilder.setMessage(getActivity().getResources().getText(R.string.createProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (value.isEmpty()) {
                CreateNewProfil();
            } else {
                // Create the new profil
                mDbProfils.addProfil(value);
                // Make it the current.
                setCurrentProfil(value);
            }
        });

        newProfilBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_cancel), (dialog, whichButton) -> {
            if (getCurrentProfile() == null) {
                CreateNewProfil();
            }
        });

        newProfilBuilder.show();

        return true;
    }

    public boolean renameProfil() {
        AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

        newBuilder.setTitle(getActivity().getResources().getText(R.string.renameProfilTitle));
        newBuilder.setMessage(getActivity().getResources().getText(R.string.renameProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(getCurrentProfile().getName());
        newBuilder.setView(input);

        newBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (!value.isEmpty()) {
                // Get current profil
                Profile temp = getCurrentProfile();
                // Rename it
                temp.setName(value);
                // Commit it
                mDbProfils.updateProfile(temp);
                // Make it the current.
                setCurrentProfil(value);
            }
        });

        newBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_cancel), (dialog, whichButton) -> {
        });

        newBuilder.show();

        return true;
    }

    private void setDrawerTitle(String pProfilName) {
        mDrawerAdapter.getItem(0).setTitle(pProfilName);
        mDrawerAdapter.notifyDataSetChanged();
        mDrawerLayout.invalidate();
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void showFragment(String pFragmentName) {
        showFragment(pFragmentName, true);
    }

    private void showFragment(String pFragmentName, boolean addToBackStack) {

        if (currentFragmentName.equals(pFragmentName))
            return; // If this is already the current fragment, do no replace.

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Then show the fragments
        if (pFragmentName.equals(FONTESPAGER)) {
            ft.replace(R.id.fragment_container, getFontesPagerFragment(), FONTESPAGER);
        } else if (pFragmentName.equals(WEIGHT)) {
            ft.replace(R.id.fragment_container, getWeightFragment(), WEIGHT);
        } else if (pFragmentName.equals(SETTINGS)) {
            ft.replace(R.id.fragment_container, getSettingsFragment(), SETTINGS);
        } else if (pFragmentName.equals(MACHINES)) {
            ft.replace(R.id.fragment_container, getMachineFragment(), MACHINES);
        }  else if (pFragmentName.equals(WORKOUTS)) {
            ft.replace(R.id.fragment_container, getWorkoutListFragment(), WORKOUTS);
        } else if (pFragmentName.equals(ABOUT)) {
            ft.replace(R.id.fragment_container, getAboutFragment(), ABOUT);
        } else if (pFragmentName.equals(BODYTRACKING)) {
            ft.replace(R.id.fragment_container, getBodyPartFragment(), BODYTRACKING);
        } else if (pFragmentName.equals(PROFILE)) {
            ft.replace(R.id.fragment_container, getProfileFragment(), PROFILE);
        }
        currentFragmentName = pFragmentName;
        ft.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    public Profile getCurrentProfile() {
        return profileViMo.getProfile().getValue();
    }

    public void setCurrentProfil(String newProfilName) {
        Profile newProfil = this.mDbProfils.getProfil(newProfilName);
        setCurrentProfil(newProfil);
    }

    public void setCurrentProfil(Profile newProfil) {
        profileViMo.setProfile(newProfil);
    }

    private void setPhotoProfile(String path) {
        ImageUtil imgUtil = new ImageUtil();

        // Check if path is pointing to a thumb else create it and use it.
        String thumbPath = imgUtil.getThumbPath(path);
        if (thumbPath != null) {
            ImageUtil.setPic(roundProfile, thumbPath);
            mDrawerAdapter.getItem(0).setImg(thumbPath);
            mDrawerAdapter.notifyDataSetChanged();
            mDrawerLayout.invalidate();
        } else {
            roundProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_person_black_24dp));
            mDrawerAdapter.getItem(0).setImgResID(R.drawable.ic_person_black_24dp);
            mDrawerAdapter.getItem(0).setImg(null); // Img has priority over Resource
            mDrawerAdapter.notifyDataSetChanged();
            mDrawerLayout.invalidate();
        }
    }

    private void savePhotoProfile(String path) {
        mCurrentProfile.setPhoto(path);// Enregistrer sur le profile le path de la photo.
        mDbProfils.updateProfile(mCurrentProfile);
    }

    public String getCurrentMachine() {
        return mCurrentMachine;
    }

    public void setCurrentMachine(String newMachine) {
        mCurrentMachine = newMachine;
    }

    public MainActivity getActivity() {
        return this;
    }

    private void loadPreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mCurrentProfilID = settings.getLong("currentProfil", -1); // return -1 if it doesn't exist
        mIntro014Launched = settings.getBoolean("intro014Launched", false);
        mMigrationBD15done = settings.getBoolean("migrationBD15done", false);
    }

    private void savePreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("currentProfil", mCurrentProfilID);
        editor.putBoolean("intro014Launched", mIntro014Launched);
        editor.putBoolean("migrationBD15done", mMigrationBD15done);
        editor.apply();
    }

    private FontesPagerFragment getFontesPagerFragment() {
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().findFragmentByTag(FONTESPAGER);
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);

        return mpFontesPagerFrag;
    }

    private WeightFragment getWeightFragment() {
        if (mpWeightFrag == null)
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().findFragmentByTag(WEIGHT);
        if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);

        return mpWeightFrag;
    }

    private ProfileFragment getProfileFragment() {
        if (mpProfileFrag == null)
            mpProfileFrag = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE);
        if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);

        return mpProfileFrag;
    }

    private MachineFragment getMachineFragment() {
        if (mpMachineFrag == null)
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().findFragmentByTag(MACHINES);
        if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
        return mpMachineFrag;
    }

    private AboutFragment getAboutFragment() {
        if (mpAboutFrag == null)
            mpAboutFrag = (AboutFragment) getSupportFragmentManager().findFragmentByTag(ABOUT);
        if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 6);

        return mpAboutFrag;
    }

    private BodyPartListFragment getBodyPartFragment() {
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().findFragmentByTag(BODYTRACKING);
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);

        return mpBodyPartListFrag;
    }


    private ProgramListFragment getWorkoutListFragment() {
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().findFragmentByTag(WORKOUTS);
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 10);

        return mpWorkoutListFrag;
    }

    private SettingsFragment getSettingsFragment() {
        if (mpSettingFrag == null)
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS);
        if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);

        return mpSettingFrag;
    }

    public Toolbar getActivityToolbar() {
        return top_toolbar;
    }

    public void restoreToolbar() {
        if (top_toolbar != null) setSupportActionBar(top_toolbar);
    }

    public void showMP3Toolbar(boolean show) {
        Toolbar mp3toolbar = this.findViewById(R.id.musicToolbar);
        if (!show) {
            mp3toolbar.setVisibility(View.GONE);
        } else {
            mp3toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                initActivity();
                mIntro014Launched = true;
                initDEBUGdata();
                this.savePreferences();
            } else {
                // Cancelled the intro. You can then e.g. finish this activity too.
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        int index = getActivity().getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) { // Si on est dans une sous activité
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            super.onBackPressed();
            getActivity().getSupportActionBar().show();
        } else { // Si on est la racine, avec il faut cliquer deux fois
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), R.string.pressBackAgain, Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        }
    }

    public void initActivity() {
        // Initialisation des objets DB
        mDbProfils = new DAOProfile(this.getApplicationContext());

        // Pour la base de donnee profil, il faut toujours qu'il y ai au moins un profil
        mCurrentProfile = mDbProfils.getProfil(mCurrentProfilID);
        if (mCurrentProfile == null) { // au cas ou il y aurait un probleme de synchro
            try {
                List<Profile> lList = mDbProfils.getAllProfils();
                mCurrentProfile = lList.get(0);
            } catch (IndexOutOfBoundsException e) {
                this.CreateNewProfil();
            }
        }

        if (mCurrentProfile != null) setCurrentProfil(mCurrentProfile.getName());

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            selectItem(position);

            // Insert the fragment by replacing any existing fragment
            switch (position) {
                case 0:
                    showFragment(PROFILE);
                    setTitle(getString(R.string.ProfileLabel));
                    break;
                case 1:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.menu_Workout));
                    break;
                case 2:
                    showFragment(MACHINES);
                    setTitle(getResources().getText(R.string.MachinesLabel));
                    break;
                case 3:
                    showFragment(WORKOUTS);
                    setTitle(getString(R.string.workout_list_menu_item));
                    break;
                case 4:
                    showFragment(WEIGHT);
                    setTitle(getResources().getText(R.string.weightMenuLabel));
                    break;
                case 5:
                    showFragment(BODYTRACKING);
                    setTitle(getResources().getText(R.string.bodytracking));
                    break;
                case 6:
                    showFragment(SETTINGS);
                    setTitle(getResources().getText(R.string.SettingLabel));
                    break;
                case 7:
                    showFragment(ABOUT);
                    setTitle(getResources().getText(R.string.AboutLabel));
                    break;
                default:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.FonteLabel));
            }
        }
    }
}