package dtitss.arportal;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;


public class DBModels extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DownloadedModels.db";
    public static final String MODELS_TABLE_NAME = "models";
    public static final String MODELS_COLUMN_MODELKEY = "modelKey";


    public DBModels(Context context) {
        super(context, DATABASE_NAME , null, 2);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table models (" +
                "id integer primary key autoincrement, "+
                "modelID integer," +
                "modelName," +
                "modelImageURL," +
                "modelKey," +
                "categoryID integer," +
                "modelSize integer)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS models");
        onCreate(db);
    }


    public boolean insertModel (Integer modelID, String modelName, String modelImageURL, String modelKey, Integer categoryID, Integer modelSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("modelID", modelID);
        contentValues.put("modelName", modelName);
        contentValues.put("modelImageURL", modelImageURL);
        contentValues.put("modelKey", modelKey);
        contentValues.put("categoryID", categoryID);
        contentValues.put("modelSize", modelSize);
        db.insert("models", null, contentValues);
        return true;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, MODELS_TABLE_NAME);
        return numRows;
    }


    public boolean updateModel (Integer id, Integer modelID, String modelName, String modelImageURL, String modelKey, Integer categoryID, Integer modelSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("modelID", modelID);
        contentValues.put("modelName", modelName);
        contentValues.put("modelImageURL", modelImageURL);
        contentValues.put("modelKey", modelKey);
        contentValues.put("categoryID", categoryID);
        contentValues.put("modelSize", modelSize);
        db.update("models", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }


    @SuppressLint("Range")
    public  ArrayList<Integer> getIntegers(String table_name) {
        ArrayList<Integer> array_list = new ArrayList<Integer>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from models", null );

        if (res.moveToFirst()){
            do{
                array_list.add(res.getInt(res.getColumnIndex(table_name)));
            }while(res.moveToNext());
        }
        res.close();

        return array_list;
    }


    @SuppressLint("Range")
    public ArrayList<String> getStrings(String table_name) {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from models", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(table_name)));
            res.moveToNext();
        }
        return array_list;
    }


    public boolean deleteAllData (){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ MODELS_TABLE_NAME);
        return true;
    }


    public Integer deleteModel (Integer modelid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("models",
                "modelID = ? ",
                new String[] { Integer.toString(modelid) });
    }


    public ArrayList<String> getAllModulKeys() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from models", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(MODELS_COLUMN_MODELKEY)));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<Integer> getAllModelIds() {
        ArrayList<Integer> array_list = new ArrayList<Integer>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from models", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getInt(res.getColumnIndex("modelID")));
            res.moveToNext();
        }
        return array_list;
    }
}