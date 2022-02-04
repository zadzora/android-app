package dtitss.arportal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.arportal.ar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Context mainContext;
    private ListAdapter listAdapter;
    private ListView listView;

    private TextView tvEmpty; //TODO - ak nie su ziadne kategorie
    private TextView offline;

    private String url = "https:/[your.url]/v1.0/category?platform=android";   // URL adresa z ktorej sa kategórie načítavajú, neskôr sa ku nej pridá aj token
    private String newURL;                   // URL adresa aj s tokenom
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;  // Vyskakovacie okno ktoré informuje o tom že sa dáta refreshuju, pri prechode medzi online/offline režime
    private FirebaseAuth mAuth;             // Firebase autentifikácia

    private List<Categories> categoriesURL;
    private ArrayList<Integer> categoryIDS;

    private boolean isOnline = true;        // Boolean operátor ktorý informuje o tom, v akom režime sa nachádzame (offline/online)

    //DATABASE
    private DBCategories categoriesDB;
    private DBModels downdb;                // Túto databázu potrebuje pri zobrazovaní počtu stiahnutých modelov v offline režime


    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle("Categories");
        setContentView(R.layout.activity_main);

        this.mainContext = (Context) this;

        //Create database
        categoriesDB = new DBCategories(this);
        downdb = new DBModels(this);

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();

        tvEmpty = findViewById(R.id.tv_empty);
        offline = findViewById(R.id.offline);
        listView= findViewById(R.id.categoryList);

        categoriesURL = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        new NetworkUtil().execute();       // Skontroluje pripojenie k sieti, a dalej načítava kategórie

        // Refresh pri potiahnutí nadol
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    // Znova načíta dáta
    private void refresh(){
        categoriesURL = new ArrayList<>();
        new NetworkUtil().execute();
    }

    // Asynchrónne kontroluje pripojenie k internetu.
    public class NetworkUtil extends AsyncTask<String, Void, String> {
//        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
//            dialog = new ProgressDialog(MainActivity.this);
//            dialog.setMessage("Loading...");
//            dialog.setCancelable(false);
//            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            if (new CheckNetwork(MainActivity.this).isNetworkAvailable())
                isOnline = true;
            else
                isOnline = false;

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            if (dialog.isShowing())
//                dialog.dismiss();

            // Ak je online, načítava údaje z URL, ak nie tak z databázy
            if(isOnline){
                verifyToken();
                offline.setVisibility(View.GONE);
            }else {
                offline.setVisibility(View.VISIBLE);
                showCategoriesInOffline();
            }
        }
    }


    private void verifyToken(){

        mAuth.signInAnonymously()
                .addOnCompleteListener((Activity) mainContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                @Override
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()){
                                        newURL = url + "&token=" + task.getResult().getToken();     // Pridávanie tokenu do URL adresy

                                        new GetAsyncData().execute();                               // Načítavanie dát z URL
                                    }else{
                                        Toast.makeText(mainContext, "User did not recieve token.",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Asynchrónne načítavanie dát
    public class GetAsyncData extends AsyncTask<String, String , String  >{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog = new ProgressDialog(MainActivity.this);
//            progressDialog.setMessage("Please Wait");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected String doInBackground(String ... params) {

            String result;
            try {
                URL dataURL = new URL(newURL);
                HttpURLConnection conn = (HttpURLConnection) dataURL.openConnection();
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String temp;

                    while ((temp = reader.readLine()) != null) {
                        stringBuilder.append(temp);
                    }
                    result = stringBuilder.toString();
                }else  {
                    result = "error";
                }

            } catch (Exception  e) {
                e.printStackTrace();
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

//            progressDialog.dismiss();

            JSONArray jsonArray;
            try {
                jsonArray = new JSONArray(s);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // z JSON-a berieme id, názov kategórie, náhladový obrázok kategórie, počet modelov v kategórií
                    int id = Integer.parseInt(jsonObject.getString("id"));
                    String categoryName = jsonObject.getString("name");
                    String imageURL = jsonObject.getString("thumbnail");
                    int count = Integer.parseInt(jsonObject.getString("count"));

                    // Kontrola či sa už kategória nachádza v databáze
                    ArrayList<Integer> categoriesIDs = categoriesDB.getIntegers("categoryID");
                    boolean isDuplicate = false;
                    for (int j = 0; j < categoriesDB.numberOfRows(); j++) {
                        if (categoriesIDs.get(j) == (id))
                            isDuplicate = true;
                    }

                    // Pridanie/Update kategórie
                    if (!isDuplicate)
                        categoriesDB.insertModel(id, categoryName, imageURL, count);
                    else
                        categoriesDB.updateModel(id, categoryName, imageURL, count);

                    // Pridanie do listu pre zobrazenie na obrazovke, stále pred načítaním kategórií sa nastavuje na new ArrayList
                    categoriesURL.add(new Categories(id, categoryName, imageURL, count));
                }

                // Vytvorenie ListAdapter-a v ktorom sa budú kategórie zobrazovať
                listAdapter = new ListAdapter(mainContext,R.layout.card_layout,categoriesURL);
                listView.setAdapter(listAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Načítanie a zobrazenie kategórií ak sa nachádame v offline režime
    private void showCategoriesInOffline() {

        ArrayList<Integer> intIdList = categoriesDB.getIntegers("categoryID");
        ArrayList<String> categoryNameList = categoriesDB.getStrings("categoryName");
        ArrayList<String> imageURLList = categoriesDB.getStrings("categoryImageURL");
        ArrayList<Integer> intCountList = categoriesDB.getIntegers("categoryCount");
        ArrayList<Integer> intCategoryIDs = categoriesDB.getIntegers("categoryID");

        for(int i = 0;i <categoriesDB.numberOfRows();i++) {
            categoriesURL.add(new Categories(intIdList.get(i),categoryNameList.get(i),imageURLList.get(i),getCategoryCount(intCategoryIDs.get(i)))); // APP v offline rezime. Pocet iba stiahnutych modelov.
        }

        listAdapter = new ListAdapter(this,R.layout.card_layout,categoriesURL);
        listView.setAdapter(listAdapter);
    }

    // Volá sa z ListAdapter-a, presunutie na ObjectActivity danej kategórie
    void nextActivity(String id){

        Intent i = new Intent(MainActivity.this, ObjectActivity.class);
        i.putExtra("key",id);   // Pridanie id kategórie aby sa vedeli do ObjectActivity načítať správne údaje

        String currCategoryName = null;
        ArrayList<String> categoryNameList = categoriesDB.getStrings("categoryName");
        ArrayList<Integer> intCategoryIDs = categoriesDB.getIntegers("categoryID");
        for(int j =0;j<categoryNameList.size();j++){
            if(intCategoryIDs.get(j) == Integer.parseInt(id)){
                currCategoryName = categoryNameList.get(j);
                break;
            }
        }
        i.putExtra("categoryName",currCategoryName);

        //Ak je kategoria prazdna, neda sa do nej vojst (offline režim)
        if(isOnline){
            startActivityForResult(i, 1);
        }else {
            for (int j = 0; j < intCategoryIDs.size(); j++){
                if (intCategoryIDs.get(j) == Integer.valueOf(id)){
                    if(getCategoryCount(intCategoryIDs.get(j)) == 0)
                        Toast.makeText(this, "Empty category! Cannot Enter.",Toast.LENGTH_SHORT).show();
                    else
                        startActivityForResult(i, 1);
                }
            }
        }
    }

// Prenasanie dat: child -> parent
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(resultCode == RESULT_OK){
//            if(requestCode == 1){
//                if(data!=null){
//                    categoryIDS = data.getIntegerArrayListExtra("count");
//                    System.out.println("BACK: " + categoryIDS);
//                    refresh();
//                }
//            }
//        }
//    }

    // Počet stiahnutých modelov v kategórii (offline režim)
    private int getCategoryCount(int categoryID){
        int count = 0;

        List<Integer> modelsIDs = downdb.getIntegers("categoryID");
        for(int i =0;i<modelsIDs.size();i++){
            if(modelsIDs.get(i) == categoryID)
                count++;
        }
        return count;
    }
}
