package com.e.easyfitness.machines;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.e.easyfitness.DAO.DAOMachine;
import com.e.easyfitness.DAO.DAOProfile;
import com.e.easyfitness.DAO.Machine;
import com.e.easyfitness.DAO.Profile;
import com.e.easyfitness.DAO.record.DAORecord;
import com.e.easyfitness.DAO.record.Record;
import com.e.easyfitness.MainActivity;
import com.e.easyfitness.R;
import com.e.easyfitness.fonte.FonteHistoryFragment;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

public class ExerciseDetailsPager extends Fragment {
    Toolbar top_toolbar = null;
    long machineIdArg = 0;
    long machineProfilIdArg = 0;
    FragmentPagerItemAdapter pagerAdapter = null;
    ViewPager mViewPager = null;
    SmartTabLayout viewPagerTab = null;
    ImageButton deleteButton = null;
    ImageButton saveButton = null;
    MaterialFavoriteButton favoriteButton = null;
    Machine machine = null;
    boolean isFavorite = false;
    boolean toBeSaved = false;
    DAOMachine mDbMachine = null;
    DAORecord mDbRecord = null;
    private String name;
    private int id;
    private View.OnClickListener onClickToolbarItem = v -> {
        // Handle presses on the action bar items
        switch (v.getId()) {
            case R.id.deleteButton:
                deleteMachine();
                break;
            default:
                getActivity().onBackPressed();
        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ExerciseDetailsPager newInstance(long machineId, long machineProfile) {
        ExerciseDetailsPager f = new ExerciseDetailsPager();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("machineID", machineId);
        args.putLong("machineProfile", machineProfile);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.exercise_pager, container, false);

        // Locate the viewpager in activity_main.xml
        mViewPager = view.findViewById(R.id.pager);

        if (mViewPager.getAdapter() == null) {

            Bundle args = this.getArguments();
            machineIdArg = args.getLong("machineID");
            machineProfilIdArg = args.getLong("machineProfile");

            pagerAdapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                .add(getString(R.string.MachineLabel), MachineDetailsFragment.class, args)
                .add(getString(R.string.HistoryLabel), FonteHistoryFragment.class, args)
                .create());

            mViewPager.setAdapter(pagerAdapter);

            viewPagerTab = view.findViewById(R.id.viewpagertab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    Fragment frag1 = pagerAdapter.getPage(position);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        mDbRecord = new DAORecord(getContext());
        mDbMachine = new DAOMachine(getContext());
        machine = mDbMachine.getMachine(machineIdArg);

        ((MainActivity) getActivity()).getActivityToolbar().setVisibility(View.GONE);
        top_toolbar = view.findViewById(R.id.actionToolbarMachine);
        top_toolbar.setNavigationIcon(R.drawable.ic_back);
        top_toolbar.setNavigationOnClickListener(onClickToolbarItem);

        deleteButton = view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(onClickToolbarItem);
        favoriteButton = view.findViewById(R.id.favButton);
        favoriteButton.setOnClickListener(v -> {
            MaterialFavoriteButton mFav = (MaterialFavoriteButton) v;
            boolean t = mFav.isFavorite();
            mFav.setFavoriteAnimated(!t);
            isFavorite = !t;
            saveMachine();
        });
        favoriteButton.setFavorite(machine.getFavorite());

        return view;
    }

    private void saveMachine() {
        final Machine newMachine = getExerciseFragment().getMachine();

        // Si le nom n'a pas ete modifie.
        newMachine.setFavorite(favoriteButton.isFavorite());

        getExerciseFragment().requestForSave();
    }

    private void deleteMachine() {
        // afficher un message d'alerte
        AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

        deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
        deleteDialogBuilder.setMessage(getActivity().getResources().getText(R.string.deleteMachine_confirm_text));

        // Si oui, supprimer la base de donnee et refaire un Start.
        deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), (dialog, which) -> {
            // Suppress the machine
            mDbMachine.delete(machine);
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
        DAORecord mDbRecord = new DAORecord(getContext());
        DAOProfile mDbProfil = new DAOProfile(getContext());

        Profile lProfile = mDbProfil.getProfil(this.machineProfilIdArg);

        List<Record> listRecords = mDbRecord.getAllRecordByMachineStrArray(lProfile, machine.getName());
        for (Record record : listRecords) {
            mDbRecord.deleteRecord(record.getId());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.machine_details_menu, menu);

        MenuItem item = menu.findItem(R.id.saveButton);
        item.setVisible(toBeSaved);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public MachineDetailsFragment getExerciseFragment() {
        MachineDetailsFragment mpExerciseFrag;
        mpExerciseFrag = (MachineDetailsFragment) pagerAdapter.getPage(0);
        return mpExerciseFrag;
    }

    public FonteHistoryFragment getHistoricFragment() {
        FonteHistoryFragment mpHistoryFrag;
        mpHistoryFrag = (FonteHistoryFragment) pagerAdapter.getPage(1);
        return mpHistoryFrag;
    }

    public ViewPager getViewPager() {
        return (ViewPager) getView().findViewById(R.id.pager);
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) (getView().findViewById(R.id.pager))).getAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            // rafraichit le fragment courant

            if (getViewPagerAdapter() != null) {
                // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
                // Il faut donc que cela soit pris en compte dans le refresh des fragments.
                Fragment frag1;
                for (int i = 0; i < 3; i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }
            }
        }
    }
}