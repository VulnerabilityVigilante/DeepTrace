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

public class SuspiciousFileAdapter extends BaseAdapter {
    private Context context;
    private List<File> files;
    // Map to hold checkbox state (position -> selected)
    private Map<Integer, Boolean> selectionMap;

    public SuspiciousFileAdapter(Context context, List<File> files) {
        this.context = context;
        this.files = files;
        selectionMap = new HashMap<>();
        // Initialize all items as unselected
        for (int i = 0; i < files.size(); i++) {
            selectionMap.put(i, false);
        }
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // Returns the list of files that are selected
    public List<File> getSelectedFiles() {
        List<File> selected = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            if (selectionMap.get(i) != null && selectionMap.get(i)) {
                selected.add(files.get(i));
            }
        }
        return selected;
    }

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

        // If this file was imported from an external directory, display its original location.
        if (MainActivity.importedFileOriginalPaths.containsKey(file.getAbsolutePath())) {
            // Use the original external absolute path if available
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



    static class ViewHolder {
        TextView fileNameText;
        CheckBox checkBox;
        TextView sourceText;
    }

}


