package database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import database.DatabaseHelper.ExpenseEntry;

public class ExpenseEntity {
    public int id;
    public String expenseName;
    public String expenseDate;
    public String expenseType;
    public String amount;

    public ExpenseEntity() {
    }

    public ExpenseEntity(int id, String expenseName, String expenseDate, String expenseType, String amount) {
        this.id = id;
        this.expenseName = expenseName;
        this.expenseDate = expenseDate;
        this.expenseType = expenseType;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return expenseType + "\n" + expenseName + "\n" + amount + "\n" + expenseDate;
    }

    public static List<ExpenseEntity> getExpenses(DatabaseHelper dbHelper, Long startDate, Long endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = ExpenseEntry.COLUMN_NAME_DATE + " BETWEEN ? AND ?";
        String[] selectionArgs = {String.valueOf(startDate), String.valueOf(endDate)};

        Cursor results = db.query(
                ExpenseEntry.TABLE_NAME,
                new String[]{ExpenseEntry._ID, ExpenseEntry.COLUMN_NAME_EXPENSENAME, ExpenseEntry.COLUMN_NAME_AMOUNT, ExpenseEntry.COLUMN_NAME_TYPE, ExpenseEntry.COLUMN_NAME_DATE},
                selection,
                selectionArgs,
                null,
                null,
                ExpenseEntry.COLUMN_NAME_DATE
        );

        results.moveToFirst();
        List<ExpenseEntity> expenses = new ArrayList<>();
        while (!results.isAfterLast()) {
            int id = results.getInt(results.getColumnIndexOrThrow(ExpenseEntry._ID));
            String name = results.getString(results.getColumnIndexOrThrow(ExpenseEntry.COLUMN_NAME_EXPENSENAME));
            String amount = results.getString(results.getColumnIndexOrThrow(ExpenseEntry.COLUMN_NAME_AMOUNT));
            String type = results.getString(results.getColumnIndexOrThrow(ExpenseEntry.COLUMN_NAME_TYPE));
            String date = results.getString(results.getColumnIndexOrThrow(ExpenseEntry.COLUMN_NAME_DATE));

            ExpenseEntity expense = new ExpenseEntity(id, name, date, type, amount);
            expenses.add(expense);

            results.moveToNext();
        }
        results.close();
        return expenses;
    }
}