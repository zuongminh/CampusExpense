package com.example.campusexpense;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

import database.DatabaseHelper;
import database.ExpenseEntity;

public class AddNewExpense extends AppCompatActivity {
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        public EditText editText;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker.
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it.
            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            editText.setText(day + "/" + (month + 1) + "/" + year);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_expense);
        EditText editTextExpenseDate = findViewById(R.id.editTextText2);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] options = {"Add New Expense", "View All Expense", "Report"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddNewExpense.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
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
                    }
                });
                builder.show();
            }
        });

        editTextExpenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.editText = editTextExpenseDate;
                datePicker.show(getSupportFragmentManager(), "datePicker");
            }
        });
        editTextExpenseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePicker = new DatePickerFragment();
                datePicker.editText = editTextExpenseDate;
                datePicker.show(getSupportFragmentManager(), "datePicker");
            }
        });

        // Auto-populate expense date with the current date
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String currentDate = day + "/" + (month + 1) + "/" + year;
        editTextExpenseDate.setText(currentDate);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText expenseNameControl = findViewById(R.id.editTextText);
                String expenseName = expenseNameControl.getText().toString();
                Spinner expenseTypeControl = findViewById(R.id.spinner);
                String expenseType = expenseTypeControl.getSelectedItem().toString();
                EditText expenseAmountControl = findViewById(R.id.editTextText3);
                String expenseAmount = expenseAmountControl.getText().toString();
                EditText expenseDateControl = findViewById(R.id.editTextText2);
                String expenseDate = expenseDateControl.getText().toString();


                if (expenseType.equals("Other")) {
                    // Handle the "Other" case here (e.g., show a dialog to enter a new expense type)
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewExpense.this);
                    builder.setTitle("New Expense Type");
                    builder.setMessage("Enter a new expense type:");

                    final EditText input = new EditText(AddNewExpense.this);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newExpenseType = input.getText().toString();
                            // Save the new expense type or perform other actions
                            saveExpense(expenseName, newExpenseType, expenseAmount, expenseDate);
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                } else {
                    // The selected expense type is not "Other"
                    saveExpense(expenseName, expenseType, expenseAmount, expenseDate);
                }

            }
        });
        Intent intent = getIntent();
        if (intent.hasExtra("expenseId")) {
            // Retrieve the expense details
            int expenseId = intent.getIntExtra("expenseId", 0);
            String expenseName = intent.getStringExtra("expenseName");
            String expenseType = intent.getStringExtra("expenseType");
            String expenseAmount = intent.getStringExtra("expenseAmount");
            String expenseDate = intent.getStringExtra("expenseDate");

            // Populate the fields with the expense details
            EditText expenseNameControl = findViewById(R.id.editTextText);
            expenseNameControl.setText(expenseName);

            Spinner expenseTypeControl = findViewById(R.id.spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.expenseType, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            expenseTypeControl.setAdapter(adapter);
            int position = adapter.getPosition(expenseType);
            expenseTypeControl.setSelection(position);

            EditText expenseAmountControl = findViewById(R.id.editTextText3);
            expenseAmountControl.setText(expenseAmount);

            EditText expenseDateControl = findViewById(R.id.editTextText2);
            expenseDateControl.setText(expenseDate);
        }
    }

        private void saveExpense(String expenseName, String expenseType, String expenseAmount, String expenseDate) {
            Intent intent = getIntent();
            if (intent.hasExtra("expenseId")) {
                // Update existing expense
                int expenseId = intent.getIntExtra("expenseId", 0);

                ExpenseEntity expense = new ExpenseEntity();
                expense.id = expenseId;
                expense.expenseName = expenseName;
                expense.amount = expenseAmount;
                expense.expenseType = expenseType;
                expense.expenseDate = expenseDate;

                DatabaseHelper dbHelper = new DatabaseHelper(getApplication());
                long id = dbHelper.updateExpense(expense);

                Toast.makeText(getApplication(), "Expense updated", Toast.LENGTH_LONG).show();
            } else {
                // Insert new expense
                ExpenseEntity expense = new ExpenseEntity();
                expense.expenseName = expenseName;
                expense.amount = expenseAmount;
                expense.expenseType = expenseType;
                expense.expenseDate = expenseDate;

                DatabaseHelper dbHelper = new DatabaseHelper(getApplication());
                long id = dbHelper.insertExpense(expense);

                Toast.makeText(getApplication(), "Expense saved", Toast.LENGTH_LONG).show();
            }

            Intent intents = new Intent(getApplicationContext(), AllExpenses.class);
            startActivity(intents);
        }
}