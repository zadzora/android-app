package dtitss.arportal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.arportal.ar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;


public class AvailableAdapter extends RecyclerView.Adapter<AvailableAdapter.AvailableObjectsViewHolder> {

    private Context context;
    private AvailableViewModel availableViewModel;
    private ObjectActivity objectActivity;
    private DownloadedAdapter downloadedAdapter;

    private List<Downloaded> mListAvailable;    // List všetkých modelov
    private ArrayList<AvailableAdapter.AvailableObjectsViewHolder> holders = new ArrayList<>();//call in the constructor make it a class variable so it can accessed globally;
    private ArrayList<Downloaded> selectList = new ArrayList<>(); // List vybraných modelov

    private boolean isEnable = false;
    private boolean isSelectAll = false;
    private boolean isButtonClicked = false;    // Boolean tlačidla "Select"

    private String newURL;                      // URL adresa odkial sa modely stahujú

    private TextView tvEmpty;                   // Zobrazenie hlášky "No Data Found" ak nie su k dispozícií žiadne dalšie modely
    private TextView editButton;                // Tlačidlo "Select"

    //Token
    private FirebaseAuth mAuth;
    //DB
    private DBModels downdb;


    public AvailableAdapter(ObjectActivity objectActivity, Context context, List<Downloaded> availableList
                            , DownloadedAdapter downloadedAdapter, TextView tvEmpty, DBModels mydb
                            , TextView editButton) {
        this.context = context;
        this.mListAvailable = availableList;
        this.tvEmpty = tvEmpty;
        this.downloadedAdapter = downloadedAdapter;
        this.objectActivity = objectActivity;
        this.downdb = mydb;
        this.editButton = editButton;

        if (mListAvailable.size() < 1)
            tvEmpty.setVisibility(View.VISIBLE);
        else
            tvEmpty.setVisibility(View.GONE);
    }


    @NonNull
    @Override
    public AvailableObjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_layout,parent,false);
        availableViewModel = ViewModelProviders.of((FragmentActivity) context).get(AvailableViewModel.class);

        return new AvailableObjectsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AvailableObjectsViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        Downloaded available = mListAvailable.get(position);

        if (available == null)
            return;

        holders.add(holder);        // List yzo všetkými holders.

        Picasso.with(context).load(available.getImageURL()).into(holder.availableImage);
        holder.availableName.setText(available.getName());


        editButton.setOnClickListener(new View.OnClickListener() {  //Button "Select"

            @Override
            public void onClick(View v) {
                if (!isButtonClicked) {
                    editButton.setTextColor(Color.parseColor("#0098FD"));
                    isButtonClicked = true;

                    //Show edit options
                    for (int i = 0; i < holders.size();i++){        // Nastavenie prázdnej ikony pre všetky modely
                        AvailableAdapter.AvailableObjectsViewHolder hold = holders.get(i);

                        hold.checkBox.setVisibility(View.VISIBLE);
                        hold.availableIcon.setVisibility(View.GONE);
                    }
                }else {
                    if(selectList.size() == 0){
                        isEnable = false;
                        isSelectAll = false;
                        //selectList.clear();

                        for (int i = 0; i < holders.size(); i++) {      // Nastavenie "download" ikony pre všetky modely
                            AvailableAdapter.AvailableObjectsViewHolder hold = holders.get(i);

                            hold.checkBox.setVisibility(View.GONE);
                            hold.availableIcon.setVisibility(View.VISIBLE);
                        }
                        isButtonClicked = false;
                        editButton.setTextColor(Color.parseColor("#5CAAAF"));

                        availableViewModel.getText().observe((LifecycleOwner) context
                                , new Observer<String>() {
                                    @Override
                                    public void onChanged(String s) {
                                        //ActionMode actionMode = actionModeClicked;
                                        //actionMode.finish();
                                    }
                                });

                        notifyDataSetChanged();
                    }
                }
            }
        });

        holder.availableIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isEnable) {
                    //Select menu click on icon
                    ClickItem(holder);
                } else {
                    ImageView icon = holder.availableIcon;
                    Downloaded s = mListAvailable.get(holder.getAbsoluteAdapterPosition());

                    String downPath = "https://[your.url]/v1.0/model/download/" + s.getModelKey();
                    verifyToken(downPath, s, icon, 1); //Overi token, popripade vygeneruje novy, dalej sa pustaju async funkcie na kontrolu internetu a stiahnutie dat
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isButtonClicked) {
                    if (!isEnable) {

                        ActionMode.Callback callback = new ActionMode.Callback() {
                            @Override
                            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                                //Initialize menu inflater
                                MenuInflater menuInflater = actionMode.getMenuInflater();
                                menuInflater.inflate(R.menu.menuava, menu);

                                return true;
                            }

                            @Override
                            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                                //When action mode is prepare
                                isEnable = true;
                                ClickItem(holder);

                                availableViewModel.getText().observe((LifecycleOwner) context,  // Menu pre vybrané položky
                                        new Observer<String>() {
                                            @Override
                                            public void onChanged(String s) {
                                                //When text change
                                                actionMode.setTitle(String.format("%s Selected", s));
                                                //if(s.equals("0"))
                                                //    actionMode.finish();
                                            }
                                        });

                                return true;
                            }

                            @Override
                            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                                int id = menuItem.getItemId();
                                switch (id) {
                                    case R.id.menu_download:    // Ikona na stiahnutie vybraných modelov
                                        int listCount = selectList.size();
                                        for (Downloaded s : selectList) {
                                            downloadedAdapter.notifyMe();

                                            ImageView icon = holder.availableIcon;
                                            String downPath = "https://[your.url]/v1.0/model/download/" + s.getModelKey();  // Pridanie do URL nazov súboru
                                            verifyToken(downPath, s, icon, listCount);     //Overi token, popripade vygeneruje novy, dalej sa pustaju async funkcie na kontrolu internetu a stiahnutie dat

                                            if (listCount != 0)
                                                    listCount--;
                                        }
                                        if (mListAvailable.size() == 0)
                                            tvEmpty.setVisibility(View.VISIBLE);

                                        actionMode.finish();
                                        break;
                                    case R.id.menu_select_all:  // Ikona na výber všetkých modelov
                                        if (selectList.size() == mListAvailable.size()) {
                                            isSelectAll = false;
                                            selectList.clear();
                                        } else {
                                            isSelectAll = true;
                                            selectList.clear();
                                            selectList.addAll(mListAvailable);
                                        }

                                        availableViewModel.setText(String.valueOf(selectList.size()));
                                        notifyDataSetChanged();
                                        break;
                                }
                                return true;
                            }

                            @Override
                            public void onDestroyActionMode(ActionMode actionMode) {
                                isEnable = false;
                                isSelectAll = false;
                                selectList.clear();

                                for (int i = 0; i < holders.size();i++){
                                    AvailableAdapter.AvailableObjectsViewHolder hold = holders.get(i);

                                    hold.checkBox.setVisibility(View.GONE);
                                    hold.availableIcon.setVisibility(View.VISIBLE);
                                }
                                isButtonClicked = false;
                                editButton.setTextColor(Color.parseColor("#5CAAAF"));

                                notifyDataSetChanged();
                            }
                        };
                        ((AppCompatActivity) view.getContext()).startActionMode(callback);
                    } else {
                        ClickItem(holder);
                    }
                }else{
                    Log.d("Edit","Cannot edit!");
                }
            }
        });

        if(isSelectAll){
            holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check));
        }else {
            //all value unselected
            holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_uncheck));
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }


    private void ClickItem(AvailableAdapter.AvailableObjectsViewHolder holder) {
        //Get selected item value
        Downloaded s = mListAvailable.get(holder.getAbsoluteAdapterPosition());

        if (holder.checkBox.getDrawable().getConstantState().equals(context.getResources().getDrawable(R.drawable.ic_uncheck).getConstantState())) {
            //When item not selected
            holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check));
            selectList.add(s);
        } else {
            //When item selected
            holder.checkBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_uncheck));
            selectList.remove(s);
        }
        availableViewModel.setText(String.valueOf(selectList.size()));
    }

    private void verifyToken(String url, Downloaded s, ImageView icon, int count){

        mAuth.signInAnonymously()
                .addOnCompleteListener((Activity) context,new OnCompleteListener<AuthResult>() {
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
                                        newURL = url + "?token=" + task.getResult().getToken(); // Pridanie tokenu do URL adresy
                                        downdb.insertModel(s.getId(),s.getName(), s.getImageURL(), s.getModelKey(), s.getCategoryId(), s.getModelSize());     // Pridanie modelu do databázy stiahnutých modelov

                                        objectActivity.downloadFileIcon(newURL, s, icon, count);   // Stahovanie modelov do zariadenia cez funkciu v classe ObjectActivity
                                    }else{
                                        Toast.makeText(context, "User did not recieve token.",Toast.LENGTH_SHORT).show();

                                        return;
                                    }
                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInAnonymously:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public int getItemCount() {
        if (mListAvailable != null)
            return mListAvailable.size();

        return 0;
    }


    public class AvailableObjectsViewHolder extends RecyclerView.ViewHolder {

        private ImageView availableImage;
        private TextView availableName;
        private ImageView availableIcon;
        private ImageView checkBox;

        public AvailableObjectsViewHolder(@NonNull View itemView) {
            super(itemView);

            availableImage = itemView.findViewById(R.id.availableImage);
            availableName = itemView.findViewById(R.id.availableTitle);
            availableIcon = itemView.findViewById(R.id.availableIcon);
            checkBox = itemView.findViewById(R.id.iv_check_box);

        }
    }
}
