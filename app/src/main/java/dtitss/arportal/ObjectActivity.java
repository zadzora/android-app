package dtitss.arportal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ObjectActivity extends AppCompatActivity {

    private RecyclerView rcvDownloaded;
    private RecyclerView rcvAvailable;
    private DownloadedAdapter downloadedAdapter;
    private AvailableAdapter availableAdapter;
    private ObjectActivity objectActivity;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;

    private TextView tvEmpty;               // No Data Found pri downloaded modeloch
    private TextView editButtonDownloaded;  // Select button pri downloaded modeloch
    private TextView editButtonAvailable;   // Select button pri available modeloch
    private TextView tvEmptyAva;            // No Data Found pri available modeloch
    private TextView offline;               // Offline mode banner
    private TextView currCategoryName;      // Zobrazenie nazvu kategorie

    private List<Downloaded> downloadedArray = new ArrayList<>();
    private List<Downloaded> modelsByURL = new ArrayList<>();

    private String url = "https://[your.url]/v1.0/model?platform=android&category=";
    private String newURL;
    private int currentCategoryId = 0;
    private String currentCategoryName;

    private DBModels mydb;

    private boolean isOnline = true;

    //Token
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object);


        this.objectActivity = (ObjectActivity) this;

        //Token
        mAuth = FirebaseAuth.getInstance();

        //Create DB
        mydb = new DBModels(this);

        offline = findViewById(R.id.offline); //Offline
        currCategoryName = findViewById(R.id.currCategoryName);

        // Načítanie views
        rcvDownloaded = findViewById(R.id.rcv_downloaded);
        rcvAvailable = findViewById(R.id.rcv_available);
        editButtonDownloaded = findViewById(R.id.editButtonDownloaded);
        editButtonAvailable = findViewById(R.id.editButtonAvailable);
        tvEmpty = findViewById(R.id.tv_empty);
        tvEmptyAva = findViewById(R.id.tv_emptyAva);

        // Key z MainActivity ktorý obsahuje číslo kategórie, to sa následne pridá do URL
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("key");
            currentCategoryId = Integer.valueOf(value);
            currentCategoryName = extras.getString("categoryName");

            url = url + value;
        }

        currCategoryName.setText(currentCategoryName);

        requestQueue = Volley.newRequestQueue(this);
        new NetworkUtil().execute();

        // Layout s recycler view-om pre downloaded modely
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,1);
        rcvDownloaded.setLayoutManager(gridLayoutManager);
        rcvDownloaded.setFocusable(false);
        rcvDownloaded.setNestedScrollingEnabled(false);

        // Layout s recycler view-om pre available modely
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvAvailable.setLayoutManager(linearLayoutManager);
        rcvAvailable.setFocusable(false);
        rcvAvailable.setNestedScrollingEnabled(false);

        //Swipe action
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rcvDownloaded);
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swiperefresh);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                pullToRefresh.setRefreshing(false);
            }
        });
    }


    private void refresh(){
        modelsByURL = new ArrayList<>();
        downloadedArray = new ArrayList<>();

        new NetworkUtil().execute();
    }

    // Prechod na AR activity, po kliknutí na stiahnutý model
    public void goToArActivity(int pos) {
            Intent intent = new Intent(ObjectActivity.this, ArActivity.class);
            intent.putExtra("downloadedObj", (Serializable) downloadedArray);   // Posiela zoznam všetkých stiahnutých modelov
            intent.putExtra("pos", pos);                                        // Pozíciu kliknutého modelu
            startActivity(intent);
    }

// Prenasanie dat: child -> parent
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                Intent intent = new Intent();
//                intent.putExtra("count",mydb.getIntegers("categoryID"));
//
//                setResult(RESULT_OK, intent);
//                finish();
//
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }


    private boolean inDatabase(String modelKey){
        boolean isInDatabase = false;
        List moduleKeys = mydb.getAllModulKeys();

        for (int j = 0; j<moduleKeys.size();j++){
            if(moduleKeys.get(j).equals(modelKey))
                isInDatabase = true;
        }

        return isInDatabase;
    }


    private void showModelsInOffline() {

        ArrayList<Integer> modelIDList = mydb.getIntegers("modelID");
        ArrayList<String> modelNameList = mydb.getStrings("modelName");
        ArrayList<String> modelImageURLList = mydb.getStrings("modelImageURL");
        ArrayList<String> modelKeyList = mydb.getStrings("modelKey");
        ArrayList<Integer> categoryIDList = mydb.getIntegers("categoryID");
        ArrayList<Integer> modelSizeList = mydb.getIntegers("modelSize");


        for(int i = 0;i <mydb.numberOfRows();i++) {
            if (!inDatabase(modelKeyList.get(i))) {
                modelsByURL = new ArrayList<>();
            }else {
                if(categoryIDList.get(i) == currentCategoryId)
                    downloadedArray.add(new Downloaded(modelIDList.get(i), modelNameList.get(i), modelImageURLList.get(i), modelKeyList.get(i), categoryIDList.get(i), modelSizeList.get(i)));
            }

        }

        downloadedAdapter = new DownloadedAdapter(objectActivity, ObjectActivity.this, downloadedArray, modelsByURL, tvEmpty, editButtonDownloaded, mydb);
        rcvDownloaded.setAdapter(downloadedAdapter);

        availableAdapter = new AvailableAdapter(objectActivity, ObjectActivity.this, modelsByURL, downloadedAdapter, tvEmptyAva, mydb, editButtonAvailable);
        rcvAvailable.setAdapter(availableAdapter);
    }

    // Vymazanie modelu potiahnutím do ľava
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAbsoluteAdapterPosition();

            switch (direction){
                case ItemTouchHelper.LEFT:
                    //Delete from database
                    mydb.deleteModel(downloadedArray.get(position).getId());
                    //Add to available to down
                    modelsByURL.add(downloadedArray.get(position));
                    //Delete from device
                    deleteModel(downloadedArray.get(position).getModelKey());

                    //Delete from downloaded [LAST!]
                    downloadedArray.remove(position);

                    if(downloadedArray.size() == 0){
                        tvEmpty.setVisibility(View.VISIBLE);
                    }

                    //Notify change

                    downloadedAdapter.notifyItemRemoved(position);
                    availableAdapter.notifyDataSetChanged();
                    break;
            }
        }

        // Zmena pozadnia a pridanie ikony pri mazaní modelu swipom
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(ObjectActivity.this,R.color.delete_red)) //pridat red
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    //ADD RIGHT
                    //.addBackgroundColor(ContextCompat.getColor(ObjectActivity.this, R.color.my_background))
                    //.addActionIcon(R.drawable.ic_delete)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    // Mazanie modelu
    public void deleteModel(String modelKey) {
        //Check if is already downloaded
        String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Path: " + path + " Size: " + files.length);
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());

        }

        // Vymazenie modelu z databázy
        for(int i = 0; i < downloadedArray.size(); i++){
            if(downloadedArray.get(i).getModelKey() == modelKey)
                mydb.deleteModel(downloadedArray.get(i).getId());
        }

        // Vymazanie modelu z telefónu
        if(files != null)
            for(File file : files) {
                if (file.getName().equals(modelKey)) {
                    System.out.println("DELETING");
                    file.delete();
                }
            }

        if(tvEmptyAva.getVisibility() == View.VISIBLE)
            tvEmptyAva.setVisibility(View.GONE);
    }

    // Stiahnutie modelu, volá sa z AvailableAdapter-u
    public void downloadFileIcon(String url, Downloaded downloaded, ImageView icon, int count){
        DownloadFileFromURL object = new DownloadFileFromURL(this, downloaded, icon, count);
        object.execute(url);
    }


    private void verifyToken(){

        mAuth.signInAnonymously()
                .addOnCompleteListener((Activity) this, new OnCompleteListener<AuthResult>() {
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
                                        newURL = url + "&token=" + task.getResult().getToken();

                                        new GetAsyncData().execute();
                                    }else{
                                        Toast.makeText(objectActivity, "User did not recieve token.",Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInAnonymously:failure", task.getException());
                            Toast.makeText(objectActivity, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //ASYNC CLASSES
    public class GetAsyncData extends AsyncTask<String, String , String  >{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // display a progress dialog for good user experiance
//            progressDialog = new ProgressDialog(ObjectActivity.this);
//            progressDialog.setMessage("Please Wait");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }
        @Override
        protected String doInBackground(String ... params) {

            String result = null;
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
            }

            System.out.println("r " + result);
            return result;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // dismiss the progress dialog after receiving data from API
//            progressDialog.dismiss();
            JSONArray JSarray = null;
            try {
                JSarray = new JSONArray(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                for (int i = 0; i < JSarray.length(); i++) {
                    JSONObject jsonObject = JSarray.getJSONObject(i);

                    int id = Integer.parseInt(jsonObject.getString("id"));
                    String name = jsonObject.getString("name");
                    String imageURL = jsonObject.getString("thumbnail");
                    String modelKey = jsonObject.getString("modelKey");
                    int categoryId = Integer.parseInt(jsonObject.getString("categoryId"));
                    int modelSize = Integer.parseInt(jsonObject.getString("modelSize"));

                    //FOR OFFLINE DB
                    ArrayList<Integer> modelsIDs = mydb.getIntegers("modelID");
                    boolean isDuplicate = false;
                    for (int j = 0; j < mydb.numberOfRows(); j++) {
                        if (modelsIDs.get(j) == (id)) {
                            isDuplicate = true;
                        }
                    }

                    if (!isDuplicate && inDatabase(modelKey))
                        mydb.insertModel(id, name, imageURL, modelKey, categoryId, modelSize);

                    if (!inDatabase(modelKey))
                        modelsByURL.add(new Downloaded(id, name, imageURL, modelKey, categoryId, modelSize));
                    else
                        downloadedArray.add(new Downloaded(id, name, imageURL, modelKey, categoryId, modelSize));

                }
                    System.out.println(modelsByURL);

                    downloadedAdapter = new DownloadedAdapter(objectActivity,ObjectActivity.this,downloadedArray,modelsByURL,tvEmpty, editButtonDownloaded, mydb);
                    rcvDownloaded.setAdapter(downloadedAdapter);

                    availableAdapter = new AvailableAdapter(objectActivity,ObjectActivity.this, modelsByURL, downloadedAdapter, tvEmptyAva, mydb, editButtonAvailable); //adapter alebo array ??
                    rcvAvailable.setAdapter(availableAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class NetworkUtil extends AsyncTask<String, Void, String> {

//        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
//            dialog = new ProgressDialog(ObjectActivity.this);
//            dialog.setMessage("Loading...");
//            dialog.setCancelable(false);
//            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            if (new CheckNetwork(ObjectActivity.this).isNetworkAvailable())
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

            if(isOnline){
                verifyToken();
                offline.setVisibility(View.GONE);
            }else {
                offline.setVisibility(View.VISIBLE);
                showModelsInOffline();
            }
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, Integer, String> {

        private Context context;
        private Downloaded downloaded;
        private ImageView downloadingIcon;
        private Animation connectingAnimation;

        private int count = 0;

        public DownloadFileFromURL(Context context, Downloaded downloaded, ImageView icon, int count) {
            this.context = context;
            this.downloaded = downloaded;
            this.downloadingIcon = icon;
            this.count = count;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (downloadingIcon != null) {
                downloadingIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_loading));
                connectingAnimation = AnimationUtils.loadAnimation(context, R.anim.alpha_scale_animation);
                downloadingIcon.startAnimation(connectingAnimation);
            }
        }

        @Override
        protected String doInBackground(String... sUrl) {

            try {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(sUrl[0]);
                    String title = URLUtil.guessFileName(sUrl[0], null, null);
                    System.out.println(title);
                    System.out.println(sUrl[0]);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    // expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                        return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();
                    // download the file
                    input = connection.getInputStream();
                    String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    output = new FileOutputStream(path + "/" + title);
                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled())
                            return null;
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    }
                    catch (IOException ignored) { }
                    if (connection != null)
                        connection.disconnect();
                }
            }catch (Exception e){
                return e.toString();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

        }

        @Override
        protected void onPostExecute(String result) {

            if (downloadingIcon != null) {
                downloadingIcon.clearAnimation();
                connectingAnimation.cancel();
                connectingAnimation.reset();
            }
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else {
                Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
                downloadedArray.add(downloaded);
                modelsByURL.remove(downloaded);
                availableAdapter.notifyDataSetChanged();
                downloadedAdapter.notifyDataSetChanged();
            }
            if (count <= 1)
                refresh();
        }
    }
}

