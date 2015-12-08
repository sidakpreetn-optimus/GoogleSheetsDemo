package com.android.googlesheetsdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by OPTIMUSDOM ubuntu151 on 6/11/15.
 */
public class EmployeeListAdapter extends RecyclerView.Adapter<EmployeeListAdapter.EmployeeListViewHolder> {

    private Context context;
    private ArrayList<EmployeeModel> employees;

    public EmployeeListAdapter(Context context, ArrayList<EmployeeModel> employees) {
        this.context = context;
        this.employees = employees;
    }

    @Override
    public EmployeeListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new EmployeeListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EmployeeListViewHolder holder, final int position) {

        holder.takingLunch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                employees.get(position).setLunchCheck(isChecked);
            }
        });

        holder.textViewName.setText(employees.get(position).getName());
        holder.textViewCode.setText(String.valueOf(employees.get(position).getEmpCode()));
        holder.takingLunch.setChecked(employees.get(position).isLunchCheck());
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public ArrayList<EmployeeModel> getLunchEmployees() {
        return employees;
    }
    public class EmployeeListViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewCode;
        CheckBox takingLunch;

        public EmployeeListViewHolder(View itemView) {

            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            textViewCode = (TextView) itemView.findViewById(R.id.textViewCode);
            takingLunch = (CheckBox) itemView.findViewById(R.id.checkBoxLunch);

        }
    }
}