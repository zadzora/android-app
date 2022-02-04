package dtitss.arportal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;


public class DBCategories extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Categories.db";
    public static final String MODELS_TABLE_NAME = "categories";

    public DBCategories(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table categories (" +
                "id integer primary key autoincrement, "+
                "categoryID integer," +
                "categoryName," +
                "categoryImageURL," +
                "categoryCount)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS categories");
        onCreate(db);
    }


    public boolean insertModel (Integer categoryID, String categoryName, String categoryImageURL, Integer categoryCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("categoryID", categoryID);
        contentValues.put("categoryName", categoryName);
        contentValues.put("categoryImageURL", categoryImageURL);
        contentValues.put("categoryCount", categoryCount);
        db.insert("categories", null, contentValues);
        return true;
    }

    // Vrátenie array listu požadovaného sĺpca (Integers)
    public  ArrayList<Integer> getIntegers(String table_name) {
        ArrayList<Integer> array_list = new ArrayList<Integer>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from categories", null );

        if (res.moveToFirst()){
            do{
                array_list.add(res.getInt(res.getColumnIndex(table_name)));
            }while(res.moveToNext());
        }
        res.close();

        return array_list;
    }

    // Vrátenie array listu požadovaného sĺpca (Strings)
    public ArrayList<String> getStrings(String table_name) {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from categories", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(table_name)));
            res.moveToNext();
        }
        return array_list;
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, MODELS_TABLE_NAME);
        return numRows;
    }


    public boolean updateModel (Integer categoryID, String categoryName, String categoryImageURL, Integer categoryCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("categoryID", categoryID);
        contentValues.put("categoryName", categoryName);
        contentValues.put("categoryImageURL", categoryImageURL);
        contentValues.put("categoryCount", categoryCount);
        db.update("categories", contentValues, "categoryID = ? ", new String[] { Integer.toString(categoryID) } );
        return true;
    }


    public boolean deleteAllData (){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ MODELS_TABLE_NAME);
        return true;
    }


    public Integer deleteModel (Integer categoryID) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("categories",
                "categoryID = ? ",
                new String[] { Integer.toString(categoryID) });
    }
}