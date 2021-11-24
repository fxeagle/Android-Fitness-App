package com.e.easyfitness.utils;


import android.R.layout;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.e.easyfitness.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooserDialog {
    private boolean m_isNewFolderEnabled = false;
    private boolean m_displayFolderOnly = false;
    private String m_fileFilter = "*";
    private String m_sdcardDirectory = "";
    private Context m_context;
    private TextView m_titleView;

    private String m_dir = "";
    private List<String> m_subdirs = null;
    private ChosenFileListener m_chosenFileListener = null;
    private ArrayAdapter<String> m_listAdapter = null;

    public FileChooserDialog(Context context, ChosenFileListener chosenDirectoryListener) {
        m_context = context;
        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_chosenFileListener = chosenDirectoryListener;

        try {
            m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
        } catch (IOException ignored) {
        }
    }

    public boolean getNewFolderEnabled() {
        return m_isNewFolderEnabled;
    }

    /*
     * setNewFolderEnabled() - enable/disable new folder button
     */
    public void setNewFolderEnabled(boolean isNewFolderEnabled) {
        m_isNewFolderEnabled = isNewFolderEnabled;
    }

    public boolean getDisplayFolderOnly() {
        return m_displayFolderOnly;
    }

    /*
     * setDisplayFolderOnly() - display Folder only
     */
    public void setDisplayFolderOnly(boolean displayFolderOnly) {
        m_displayFolderOnly = displayFolderOnly;
    }

    public String getFileFilter() {
        return m_fileFilter;
    }

    /*
     * setFileFilter() - allow to filter file without specified extensions
     */
    public void setFileFilter(String fileFilter) {
        m_fileFilter = fileFilter;
    }

    public void resetFileFilter(String fileFilter) {
        m_fileFilter = fileFilter;
    }

    public void chooseDirectory() {
        // Initial directory is sdcard directory
        chooseDirectory(m_sdcardDirectory);
    }

    ///////////////////////////////////////////////////////////////////////
    // chooseDirectory() - load directory chooser dialog for initial
    // default sdcard directory
    ///////////////////////////////////////////////////////////////////////

    public void chooseDirectory(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            dir = m_sdcardDirectory;
        }

        try {
            dir = new File(dir).getCanonicalPath();
        } catch (IOException ioe) {
            return;
        }

        m_dir = dir;
        m_subdirs = getDirectories(dir);

        class DirectoryOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int item) {

                if (((AlertDialog) dialog).getListView().getAdapter().getItem(item).toString().substring(0, 1).equals("/")) {
                    // Navigate into the sub-directory
                    m_dir += ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                    ((AlertDialog) dialog).getListView().smoothScrollToPositionFromTop(0, 0, 0);// Back on top of the ListView
                    updateDirectory();
                } else if (((AlertDialog) dialog).getListView().getAdapter().getItem(item).toString().equals("..")) {
                    // Navigate back to an upper directory
                    m_dir = new File(m_dir).getParent();
                    ((AlertDialog) dialog).getListView().smoothScrollToPositionFromTop(0, 0, 0);//.scrollTo(0, 0);//.smoothScrollToPosition(0);
                    updateDirectory();
                } else {
                    // Current directory chosen
                    if (m_chosenFileListener != null) {
                        // Call registered listener supplied with the chosen directory
                        m_chosenFileListener.onChosenFile(m_dir + "/" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item).toString());
                        dialog.dismiss();
                    }
                }
            }
        }

        AlertDialog.Builder dialogBuilder =
            createDirectoryChooserDialog(dir, m_subdirs, new DirectoryOnClickListener());

/*
        dialogBuilder.setPositiveButton("OK", (dialog, which) -> {
            // Current directory chosen
            if (m_chosenFileListener != null) {
                // Call registered listener supplied with the chosen directory
                m_chosenFileListener.onChosenFile(m_dir);
            }
        });
*/

        dialogBuilder.setNegativeButton("Cancel", null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        // Show directory chooser dialog
        dirsDialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // chooseDirectory(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    ////////////////////////////////////////////////////////////////////////////////

    private boolean createSubDir(String newDir) {
        File newDirFile = new File(newDir);
        if (!newDirFile.exists()) {
            return newDirFile.mkdir();
        }

        return false;
    }

    public List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<>();

        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            if (dir.length() > 1) dirs.add("..");

            for (File file : dirFile.listFiles()) {
                if (file.isDirectory()) // Get Directories
                {
                    dirs.add("/" + file.getName());
                } else // Get files
                {
                    if (!this.m_displayFolderOnly)
                        if (isInFilter(file.getName())) {
                            dirs.add(file.getName());
                        }
                }
            }
        } catch (Exception ignored) {
        }

        Collections.sort(dirs, String::compareTo);

        return dirs;
    }

    /*
     * return only files with full path of a specific folder
     */
    public List<String> getFiles(String dir) {
        List<String> dirs = new ArrayList<>();

        try {
            File dirFile = new File(dir);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                return dirs;
            }

            for (File file : dirFile.listFiles()) {
                if (!file.isDirectory()) // Don't get Directories
                {
                    if (!this.m_displayFolderOnly)
                        if (isInFilter(file.getName())) {
                            dirs.add(file.getName());
                        }
                }
            }
        } catch (Exception ignored) {
        }

        Collections.sort(dirs, String::compareTo);

        return dirs;
    }

    /*
     * return true if file is allowed
     */
    private boolean isInFilter(String fileName) {
        boolean ret = false;
        String extension = "";

        // recupere l'extension du fichier
        extension = getExtension(fileName);

        // verifie si l'extension est prise en compte
        if (this.m_fileFilter.contains("*"))
            return true;
        if (this.m_fileFilter.contains(extension))
            return true;

        return ret;
    }

    private String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
                                                             DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);

        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        LinearLayout titleLayout = new LinearLayout(m_context);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        m_titleView = new TextView(m_context);
        m_titleView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        m_titleView.setTextAppearance(m_context, android.R.style.TextAppearance_DeviceDefault_Medium);
        m_titleView.setTextColor(m_context.getResources().getColor(android.R.color.black));
        m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        m_titleView.setText(title);

        Button newDirButton = new Button(m_context);
        newDirButton.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        newDirButton.setText("New folder");
        newDirButton.setOnClickListener(v -> {
            final EditText input = new EditText(m_context);

            // Show new folder name input dialog
            new AlertDialog.Builder(m_context).
                setTitle("New folder name").
                setView(input).setPositiveButton(m_context.getResources().getText(R.string.global_ok), (dialog, whichButton) -> {
                Editable newDir = input.getText();
                String newDirName = newDir.toString();
                // Create new directory
                if (createSubDir(m_dir + "/" + newDirName)) {
                    // Navigate into the new directory
                    m_dir += "/" + newDirName;
                    updateDirectory();
                } else {
                    Toast.makeText(
                        m_context, m_context.getResources().getText(R.string.failedtocreatefolder) + " " + newDirName, Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton(m_context.getResources().getText(R.string.global_cancel), null).show();
        });

        if (!m_isNewFolderEnabled) {
            newDirButton.setVisibility(View.GONE);
        }

        titleLayout.addView(m_titleView);
        titleLayout.addView(newDirButton);

        dialogBuilder.setCustomTitle(titleLayout);

        m_listAdapter = createListAdapter(listItems);

        dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);

        return dialogBuilder;
    }

    private void updateDirectory() {
        m_subdirs.clear();
        m_subdirs.addAll(getDirectories(m_dir));
        m_titleView.setText(m_dir);
        m_listAdapter.notifyDataSetChanged();
        //m_titleView.getContext().layout.select_dialog_item;

    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(m_context,
            layout.simple_list_item_1, android.R.id.text1, items) //.select_dialog_item
        {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView) {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                    tv.setTextAppearance(m_context, android.R.style.TextAppearance_DeviceDefault_Small);
                }
                return v;
            }
        };
    }

    //////////////////////////////////////////////////////
    // Callback interface for selected directory
    //////////////////////////////////////////////////////
    public interface ChosenFileListener {
        void onChosenFile(String chosenDir);
    }
}
