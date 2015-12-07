package com.android.googlesheetsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by OPTIMUSDOM ubuntu151 on 10/11/15.
 */
public class EmployeesLunchActivity extends AppCompatActivity {

    private static ArrayList<EmployeeModel> employees;
    private EmployeeListAdapter adapter;
    private RecyclerView recyclerList;
    private Button buttonCommit;
    // just send this lunchUsers list to script as parameters
    private static ArrayList<Object> lunchUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_employee_lunch);
        recyclerList = (RecyclerView) findViewById(R.id.listEmployees);
        recyclerList.setHasFixedSize(true);
        recyclerList.setLayoutManager(new LinearLayoutManager(this));
        buttonCommit = (Button) findViewById(R.id.buttonCommit);
        setupInit();
    }

    private void setupInit() {
        Intent i = getIntent();
        employees = i.getParcelableArrayListExtra("EmployeesList");
        adapter = new EmployeeListAdapter(this, employees);
        recyclerList.setAdapter(adapter);
        buttonCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getTotalUsersForLunch() == 0) {
                    Toast.makeText(getApplicationContext(), "No employees selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialogClass().show(getSupportFragmentManager(), "Confirmation");
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public static ArrayList<Object> getLunchUsersList() {
        return lunchUsers;
    }

    public static int getTotalUsersForLunch() {
        lunchUsers = new ArrayList();
        int counter = 0;
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).isLunchCheck()) {
                counter++;
                lunchUsers.add(i + 2);
            }
        }
        lunchUsers.size();
        return counter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sort_action) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
