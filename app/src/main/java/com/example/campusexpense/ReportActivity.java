package com.example.campusexpense;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import database.DatabaseHelper;
import database.ExpenseEntity;

public class ReportActivity extends AppCompatActivity {
    TextView selectedDate;
    Button datePicker;
    ExpenseEntity expenseEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        selectedDate = findViewById(R.id.selectedDate);
        datePicker = findViewById(R.id.datePicker);
        expenseEntity = new ExpenseEntity();


        // Setting click listener for the date picker button
        datePicker.setOnClickListener(view -> showDatePicker());
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final String[] options = {"Add New Expense","View All Expense", "Report"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
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
            }
        });
        // Set the default date range to the current week
        setDefaultDateRangeToCurrentWeek();
    }

    private void setDefaultDateRangeToCurrentWeek() {
        Calendar calendar = Calendar.getInstance();

        // Set the first day of the week to Monday
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        // Find the first day of the week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        Date startDate = calendar.getTime();

        // Add 6 days to get the last day of the week (Sunday)
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        Date endDate = calendar.getTime();

        // Format the dates as strings
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        // Creating the date range string
        String defaultDateRange = startDateString + " - " + endDateString;

        // Displaying the default date range in the TextView
        selectedDate.setText(defaultDateRange);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Retrieving expenses based on the selected date range
        List<ExpenseEntity> expenses = dbHelper.getAllExpenses();
        // Generating the pie chart with the retrieved expenses
        generatePieChart(expenses);
    }
    private void showDatePicker() {
        // Creating a MaterialDatePicker builder for selecting a date range
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select a date range");
        // Building the date picker dialog
        MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Retrieving the selected start and end dates
            long startDate = selection.first;
            long endDate = selection.second;

            // Formatting the selected dates as strings
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String startDateString = sdf.format(new Date(startDate));
            String endDateString = sdf.format(new Date(endDate));

            // Creating the date range string
            String selectedDateRange = startDateString + " - " + endDateString;

            // Displaying the selected date range in the TextView
            selectedDate.setText(selectedDateRange);

            // Creating an instance of the DatabaseHelper class
            DatabaseHelper dbHelper = new DatabaseHelper(this);

            // Retrieving expenses based on the selected date range
            List<ExpenseEntity> expenses = dbHelper.getAllExpenses();
            // Generating the pie chart with the retrieved expenses
            generatePieChart(expenses);
        });

        // Showing the date picker dialog
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void generatePieChart(List<ExpenseEntity> expenses) {
        PieChart pieChart = findViewById(R.id.pieChart);

        // Filter expenses based on the selected date range
        List<ExpenseEntity> filteredExpenses = filterExpensesByDate(expenses);

        // Create a list of PieEntry to hold the data for the pie chart
        List<PieEntry> entries = new ArrayList<>();

        // Group the expenses by expense type
        Map<String, Double> expenseTypeSumMap = new HashMap<>();
        for (ExpenseEntity expense : filteredExpenses) {
            String expenseType = expense.expenseType;
            double amount = Double.parseDouble(expense.amount);
            if (expenseTypeSumMap.containsKey(expenseType)) {
                double currentSum = expenseTypeSumMap.get(expenseType);
                expenseTypeSumMap.put(expenseType, currentSum + amount);
            } else {
                expenseTypeSumMap.put(expenseType, amount);
            }
        }

        // Add the expense type and sum as PieEntry to the list
        for (Map.Entry<String, Double> entry : expenseTypeSumMap.entrySet()) {
            String expenseType = entry.getKey();
            double sum = entry.getValue();
            entries.add(new PieEntry((float) sum, expenseType));
        }

        // Create a PieDataSet with the entries
        PieDataSet dataSet = new PieDataSet(entries, " ");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setSliceSpace(4f); // Set the space between pie slices

        // Create a PieData object with the dataSet
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        // Set the data to the pie chart and customize the appearance
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleColor(Color.TRANSPARENT);
        pieChart.animateY(1000);

        float totalValue = calculateTotalValue(entries);
        pieChart.setCenterText(String.format(Locale.getDefault(), "%.2f", totalValue));
        pieChart.setCenterTextSize(21f);
        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD); // Set the font style

        // Refresh the chart
        pieChart.invalidate();
    }

    private float calculateTotalValue(List<PieEntry> entries) {
        float totalValue = 0f;
        for (PieEntry entry : entries) {
            totalValue += entry.getValue();
        }
        return totalValue;
    }

    private List<ExpenseEntity> filterExpensesByDate(List<ExpenseEntity> expenses) {
        // Get the selected start and end dates from the TextView
        String selectedDateRange = selectedDate.getText().toString();
        String[] dates = selectedDateRange.split(" - ");
        String startDateString = dates[0];
        String endDateString = dates[1];

        // Parse the start and end dates using SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = sdf.parse(startDateString);
            Date endDate = sdf.parse(endDateString);

            // Filter expenses within the selected date range (including start and end dates)
            List<ExpenseEntity> filteredExpenses = new ArrayList<>();
            for (ExpenseEntity expense : expenses) {
                Date expenseDate = sdf.parse(expense.expenseDate);
                if (expenseDate != null && !(expenseDate.before(startDate) || expenseDate.after(endDate))) {
                    filteredExpenses.add(expense);
                }
            }
            return filteredExpenses;
        } catch (ParseException e) {
            e.printStackTrace();
            // Return an empty list if there's an error parsing the dates
            return new ArrayList<>();
        }


    }

}