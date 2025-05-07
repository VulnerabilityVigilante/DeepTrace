package edu.utsa.cs3443.deepTrace.adapters;
import edu.utsa.cs3443.deepTrace.activities.MainActivity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.utsa.cs3443.deepTrace.R;
import edu.utsa.cs3443.deepTrace.models.Settings;

/**
 * Displays a list of suspicious files with checkboxes for selection.
 */
public class SuspiciousFileAdapter extends BaseAdapter {
    private Context context;
    private List<File> files;
    // Map to hold checkbox state (position -> selected)
    private Map<Integer, Boolean> selectionMap;

    /**
     * Constructs a new {@code SuspiciousFileAdapter}.
     * @param context The current context (usually the activity).
     * @param files A list of suspicious {@link File} objects to display.
     */
    public SuspiciousFileAdapter(Context context, List<File> files) {
        this.context = context;
        this.files = files;
        selectionMap = new HashMap<>();

        for (int i = 0; i < files.size(); i++) {
            selectionMap.put(i, false);
        }
    }

    /**
     * Returns the number of files in the list.
     * @return the total number of suspicious files.
     */
    @Override
    public int getCount() {
        return files.size();
    }

    /**
     * Returns the file object at the specified position.
     * @param position the index in the list.
     * @return the {@link File} object at the given position.
     */
    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    /**
     * Returns the unique ID for the item at the given position.
     * @param position the item index.
     * @return the position itself as the ID.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Retrieves the list of files that are currently selected via checkboxes.
     * @return a list of selected {@link File} objects.
     */
    public List<File> getSelectedFiles() {
        List<File> selected = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            if (selectionMap.get(i) != null && selectionMap.get(i)) {
                selected.add(files.get(i));
            }
        }
        return selected;
    }

    /**
     * Generates the view for each list item. Handles view recycling and checkbox state management.
     * @param position The position of the item within the list.
     * @param convertView A recycled view to reuse, if available.
     * @param parent The parent view group.
     * @return A fully populated view for the list item.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_suspicious_file, parent, false);
            holder = new ViewHolder();
            holder.fileNameText = convertView.findViewById(R.id.fileName);
            holder.checkBox = convertView.findViewById(R.id.fileCheckBox);
            holder.sourceText = convertView.findViewById(R.id.fileSource);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        File file = files.get(position);
        holder.fileNameText.setText(file.getName());
        holder.fileNameText.setTextSize(Settings.getFontSize());

        // Display the original location of files from external directories.
        if (MainActivity.importedFileOriginalPaths.containsKey(file.getAbsolutePath())) {
            // if available use the original external absolute path
            holder.sourceText.setText("Source: " + MainActivity.importedFileOriginalPaths.get(file.getAbsolutePath()));
        } else {
            // Otherwise, use the file's own absolute path
            holder.sourceText.setText("Source: " + file.getAbsolutePath());
        }
        holder.sourceText.setTextSize(Settings.getFontSize());


        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectionMap.get(position));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectionMap.put(position, isChecked);
            }
        });
        return convertView;
    }


    /**
     * Internal view holder class to optimize view lookup for list items.
     */
    static class ViewHolder {
        TextView fileNameText;
        CheckBox checkBox;
        TextView sourceText;
    }

}


