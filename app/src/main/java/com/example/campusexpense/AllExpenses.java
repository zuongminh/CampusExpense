package com.example.campusexpense;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import database.DatabaseHelper;
import database.ExpenseEntity;

public class AllExpenses extends AppCompatActivity {
    private List<ExpenseEntity> allExpense;
    private ArrayAdapter<ExpenseEntity> adapter;
    private ListView listView;
    private TextView selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_expenses);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] options = {"Add New Expense", "View All Expense", "Report"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AllExpenses.this);
                builder.setItems(options, (dialog, item) -> {
                    if (options[item].equals("Add New Expense")) {
                        Intent intent = new Intent(getApplicationContext(), AddNewExpense.class);
                        startActivity(intent);
                    } else if (options[item].equals("View All Expense")) {
                        Intent intent = new Intent(getApplicationContext(), AllExpenses.class);
                        startActivity(intent);
                    } else if (options[item].equals("Report")) {
                        Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                        startActivity(intent);
                    }
                });
                builder.show();

                long currentMillis = MaterialDatePicker.todayInUtcMilliseconds();
                selectedDateRange = new Pair<>(currentMillis, currentMillis);
                updateExpenseList();
            }
        });

        listView = findViewById(R.id.listExpense);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExpenseEntity entry = (ExpenseEntity) parent.getItemAtPosition(position);
                final String[] options = {"Delete", "Update"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AllExpenses.this);
                builder.setItems(options, (dialog, item) -> {
                    if (options[item].equals("Delete")) {
                        // 1. Remove from the ListView
                        allExpense.remove(position);
                        adapter.notifyDataSetChanged();
                        // 2. Remove from the database
                        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
                        dbHelper.deleteExpense(entry.id);
                    } else if (options[item].equals("Update")) {
                        Intent intent = new Intent(getApplicationContext(), AddNewExpense.class);
                        // Pass the expense details as extras in the intent
                        intent.putExtra("expenseId", entry.id);
                        intent.putExtra("expenseName", entry.expenseName);
                        intent.putExtra("expenseType", entry.expenseType);
                        intent.putExtra("expenseAmount", entry.amount);
                        intent.putExtra("expenseDate", entry.expenseDate);
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        });

        selectedDate = findViewById(R.id.selectedDate);
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        allExpense = dbHelper.getAllExpenses();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allExpense);
        listView.setAdapter(adapter);

        Button dateRangeButton = findViewById(R.id.dateRangeButton);
        dateRangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
    }

    private MaterialDatePicker<Pair<Long, Long>> datePicker;
    private Pair<Long, Long> selectedDateRange;
    private Button dateRangeButton;


    private void showDatePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a date range");

        // Set the default date range to the current day
        long currentMillis = MaterialDatePicker.todayInUtcMilliseconds();
        selectedDateRange = new Pair<>(currentMillis, currentMillis);
        builder.setSelection(selectedDateRange);

        datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDateRange = selection;
            updateExpenseList();
        });

        datePicker.show(getSupportFragmentManager(), "datePicker");
    }

    private void updateExpenseList() {
        long startDate = selectedDateRange.first;
        long endDate = selectedDateRange.second;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDateString = sdf.format(new Date(startDate));
        String endDateString = sdf.format(new Date(endDate));

        String selectedDateRangeString = startDateString + " - " + endDateString;
        selectedDate.setText(selectedDateRangeString);

        // Filter the expenses based on the selected date range
        List<ExpenseEntity> filteredExpenses = new ArrayList<>();
        for (ExpenseEntity expense : allExpense) {
            long expenseDate = convertStringToLong(expense.expenseDate);
            if (expenseDate >= startDate && expenseDate <= endDate) {
                filteredExpenses.add(expense);
            }
        }

        // Update the adapter with the filtered expenses
        adapter.clear();
        adapter.addAll(filteredExpenses);
        adapter.notifyDataSetChanged();
    }

    private long convertStringToLong(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }



}