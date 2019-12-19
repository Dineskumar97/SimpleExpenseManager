package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;



import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import android.content.ContentValues;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistantMemoryTransactionDAO extends SQLiteOpenHelper implements TransactionDAO {

    public static final String DATABASE_NAME = "170141X.db";
    public static final String EXPENSE_COLUMN_ID = "ID";
    public static final String EXPENSE_COLUMN_NO = "accountNo";
    public static final String EXPENSE_COLUMN_DATE = "Date";
    public static final String EXPENSE_COLUMN_TYPE = "Type";
    public static final String EXPENSE_COLUMN_AMOUNT = "Amount";

    private List<Transaction> transactions;

    public PersistantMemoryTransactionDAO(Context context) {
        super(context, DATABASE_NAME , null,1);
        transactions = new LinkedList<>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // TODO Auto-generated method stub


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS tbltrans");
        onCreate(db);
    }


    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        Transaction transaction = new Transaction(date, accountNo, expenseType, amount);
        String accountNumber = transaction.getAccountNo();
        Date dates = transaction.getDate();

        byte[] byteDate = dates.toString().getBytes();
        ExpenseType types = transaction.getExpenseType();
        String strType = types.toString();
        byte[] byteType = toString().getBytes();
        Double amounts = transaction.getAmount();

        Calendar c = Calendar.getInstance();
        //System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        Log.d("Date",formattedDate);
        byte[] timeStamp = formattedDate.getBytes();


        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo", accountNo);
        contentValues.put("Amount", amounts);
        contentValues.put("Type",strType);
        contentValues.put("Date", byteDate);


        db.insert("tbltrans", null, contentValues);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        transactions.clear();
        Log.d("creation","starting");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( " select * from tbltrans", null );

        res.moveToFirst();

        while(res.isAfterLast() == false){

            String accountNo = res.getString(res.getColumnIndex(EXPENSE_COLUMN_NO));
            Double amount = res.getDouble(res.getColumnIndex(EXPENSE_COLUMN_AMOUNT));
            String transType = res.getString(res.getColumnIndex(EXPENSE_COLUMN_TYPE));

            ExpenseType type = ExpenseType.valueOf(transType);
            byte[] date = res.getBlob(res.getColumnIndex(EXPENSE_COLUMN_DATE));


            String str = new String(date, StandardCharsets.UTF_8);
            Log.d("loadedDate",str);

            Date finalDate;
            try {


                SimpleDateFormat inputFormat = new SimpleDateFormat("E MMM dd yyyy HH:mm:ss 'GMT'z", Locale.ENGLISH);
                finalDate = inputFormat.parse(str);
                transactions.add(new Transaction(finalDate,accountNo,type,amount));
                Log.d("creation","success");
            }catch (java.text.ParseException e){
                Log.d("creation","failed");
                Calendar cal = Calendar.getInstance();

                finalDate = cal.getTime();
                transactions.add(new Transaction(finalDate,accountNo,type,amount));

            }


            res.moveToNext();
        }
        return transactions;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        int size = transactions.size();
        if (size <= limit) {
            return transactions;
        }
        // return the last <code>limit</code> number of transaction logs
        return transactions.subList(size - limit, size);
    }




}


