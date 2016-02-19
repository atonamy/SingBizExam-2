package com.singbiz.singbizexam2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by archie on 5/2/16.
 */
class DataImporter extends AsyncTask<InputStream, Integer, Boolean> {

    public static final int TOTAL_RECORDS = 50000;
    public static final int PUBLISH_PROGRESS_IN = 250;
    private static final int COLUMNS_SIZE = 3;
    private static final int TOTAL_CATEGORIES = 5;
    private static final int TOTAL_CATEGORIES_SHIFT = 1;
    private final static String PRODUCT_INSERT_QUERY = createInsert(SingBizDatabase.TABLE_PRODUCTS,
            new String[]{SingBizDatabase.KEY_PRODUCT_ID, SingBizDatabase.KEY_PRODUCT_REFERENCE,
                    SingBizDatabase.KEY_PRODUCT_NAME,
                    SingBizDatabase.KEY_PRODUCT_PRICE,
                    SingBizDatabase.KEY_PRODUCT_QUANTITY,
                    SingBizDatabase.KEY_PRODUCT_DISCOUNT});
    private final static String CATEGORY_INSERT_QUERY = createInsert(SingBizDatabase.TABLE_CATEGORIES,
            new String[]{SingBizDatabase.KEY_CATEGORY_TYPE, SingBizDatabase.KEY_CATEGORY_PRODUCT_REFERENCE});

    public interface Events {
        public void onProgress(int progress, int round);
        public void Finished(boolean done);
    }

    private Events importEvents = null;

    public void setEvents(Events events) {
        importEvents = events;
    }
    public void setContext(Context context) {
        currentContext = context;
    }
    private Context currentContext;


    public static String createInsert(final String tableName, final String[] columnNames) {
        if (tableName == null || columnNames == null || columnNames.length == 0) {
            throw new IllegalArgumentException();
        }
        final StringBuilder s = new StringBuilder();
        s.append("INSERT INTO ").append(tableName).append(" (");
        for (String column : columnNames) {
            s.append(column).append(" ,");
        }
        int length = s.length();
        s.delete(length - 2, length);
        s.append(") VALUES( ");
        for (int i = 0; i < columnNames.length; i++) {
            s.append(" ?" + (i+1) + " ,");
        }
        length = s.length();
        s.delete(length - 2, length);
        s.append(");");
        return s.toString();
    }

    /**
     * Before starting background thread
     * Show Progress
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * Import data in background thread
     * */

    @Override
    protected Boolean doInBackground(InputStream... streams) {

        if(currentContext == null || streams.length < 1)
            return false;

        final SingBizDatabase[] db_helper = new SingBizDatabase[] {new SingBizDatabase(currentContext) /*, new SingBizDatabase(currentContext)}*/};
        final SQLiteDatabase[] db =  new SQLiteDatabase[] { db_helper[0].getWritableDatabase()/*, db_helper[1].getWritableDatabase()*/};
        final SQLiteStatement product_statement = db[0].compileStatement(PRODUCT_INSERT_QUERY);
        final SQLiteStatement category_statement = db[0].compileStatement(CATEGORY_INSERT_QUERY);

        Random categoryGenerator = new Random();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        boolean first_line = true;
        int count = 0;
        boolean success = false;

        try {
            Set<String> references = new HashSet<>();
            br = new BufferedReader(new InputStreamReader(streams[0], "UTF-8"));
            db[0].beginTransaction();
            while ((line = br.readLine()) != null) {

                if(first_line) {
                    first_line = false;
                    continue;
                }

                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                if(data.length != COLUMNS_SIZE)
                    return false;

                product_statement.clearBindings();
                product_statement.bindLong(1, ++count);
                product_statement.bindString(2, data[0]);
                product_statement.bindString(3, data[1]);
                product_statement.bindDouble(4, Double.parseDouble(data[2]));
                product_statement.bindNull(5);
                product_statement.bindNull(6);
                product_statement.execute();

                category_statement.clearBindings();
                category_statement.bindLong(1, categoryGenerator.nextInt(TOTAL_CATEGORIES) + TOTAL_CATEGORIES_SHIFT);
                category_statement.bindLong(2, count);
                category_statement.execute();

                if(!references.contains(data[0]))
                    references.add(data[0]);

                if(count % PUBLISH_PROGRESS_IN == 0)
                    publishProgress(count, 1);
            }
            db[0].setTransactionSuccessful();
            db[0].endTransaction();

            /*Iterator<String> i = references.iterator();
            count = 0;
            db[1].beginTransaction();
            for(String reference : references) {
                int category = categoryGenerator.nextInt(TOTAL_CATEGORIES) + TOTAL_CATEGORIES_SHIFT;
                category_statement.clearBindings();
                category_statement.bindLong(1, category);
                category_statement.bindString(2, reference);
                category_statement.bindNull(3);
                category_statement.bindNull(4);
                category_statement.execute();

                if(count++ % PUBLISH_PROGRESS_IN == 0)
                    publishProgress(count, 2);
            }
            db[1].setTransactionSuccessful();
            db[1].endTransaction();*/
            success = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(Exception e)  {
            e.printStackTrace();
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            category_statement.close();
            product_statement.close();
            db[0].close();
            db_helper[0].close();
            //db[1].close();
            //db_helper[1].close();
        }

        return success;
    }


    /**
     * Updating progress bar
     * */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        // setting progress percentage
        if(importEvents != null)
            importEvents.onProgress(progress[0], progress[1]);
    }

    /**
     * After completing background task
     * Dismiss the progress
     * **/
    @Override
    protected void onPostExecute(Boolean result) {
        // dismiss the dialog after the file was downloaded
        if(importEvents != null)
            importEvents.Finished(result);

    }

}
