package dtitss.arportal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import com.arportal.ar.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class  DownloadedAdapter extends RecyclerView.Adapter<DownloadedAdapter.DownloadeObjectsViewHolder> {

    private Context context;
    private DownloadedViewModel downloadedViewModel;
    private ObjectActivity objectActivity;

    private List<Downloaded> mListDownloaded;
    private List<Downloaded> availableList;
    private ArrayList<Downloaded> selectList = new ArrayList<>();
    private ArrayList<DownloadeObjectsViewHolder> holders = new ArrayList<>();//call in the constructor make it a class variable so it can accessed globally;

    private boolean isEnable = false;
    private boolean isSelectAll = false;
    private boolean isButtonClicked = false;

    private TextView tvEmpty;
    private TextView editButton;

    //Databases
    private DBModels downdb;


    public DownloadedAdapter(ObjectActivity objectActivity, Context context, List<Downloaded> downloadedList,List<Downloaded> availableList, TextView tvEmpty, TextView editButton, DBModels mydb){
        this.context = context;
        this.mListDownloaded = downloadedList;
        this.tvEmpty = tvEmpty;
        this.editButton = editButton;
        this.downdb = mydb;
        this.availableList = availableList;
        this.objectActivity = objectActivity;

        if (mListDownloaded.size() < 1)
            tvEmpty.setVisibility(View.VISIBLE);
    }


    @NonNull
    @Override
    public DownloadeObjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.downloaded_layout,parent,false);
        downloadedViewModel = ViewModelProviders.of((FragmentActivity) context).get(DownloadedViewModel.class);

        return new DownloadeObjectsViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DownloadeObjectsViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Downloaded downloaded = mListDownloaded.get(position);
        if (downloaded == null)
            return;

        if (mListDownloaded.size() >= 1)
            tvEmpty.setVisibility(View.GONE);

        holders.add(holder);    //To have control off all holders

        Picasso.with(context).load(downloaded.getImageURL()).into(holder.downloadedImage);
        holder.downloadedName.setText(downloaded.getName());


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isButtonClicked) {
                    editButton.setTextColor(Color.parseColor("#0098FD"));
                    isButtonClicked = true;

                    //Show edit options
                    for (int i = 0; i < holders.size();i++){
                        DownloadeObjectsViewHolder hold = holders.get(i);

                        hold.ivCheckBox.setVisibility(View.VISIBLE);
                        hold.ARIcon.setVisibility(View.GONE);
                    }
                }else {
                    if(selectList.size() == 0){
                        isEnable = false;
                        isSelectAll = false;
                        //selectList.clear();
                        for (int i = 0; i < holders.size(); i++) {
                            DownloadeObjectsViewHolder hold = holders.get(i);

                            hold.ivCheckBox.setVisibility(View.GONE);
                            hold.ARIcon.setVisibility(View.VISIBLE);
                        }
                        isButtonClicked = false;
                        editButton.setTextColor(Color.parseColor("#5CAAAF"));

                        downloadedViewModel.getText().observe((LifecycleOwner) context
                                , new Observer<String>() {
                                    @Override
                                    public void onChanged(String s) {
                                        //actionMode.finish();
                                    }
                                });

                        notifyDataSetChanged();
                    }

                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isButtonClicked) {  //Select
                    if (!isEnable) {
                        //When action mode is not enable
                        ActionMode.Callback callback = new ActionMode.Callback() {
                            @Override
                            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                                //Initialize menu inflater
                                MenuInflater menuInflater = actionMode.getMenuInflater();
                                menuInflater.inflate(R.menu.menu, menu);

                                return true;
                            }

                            @Override
                            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                                //When action mode is prepare
                                isEnable = true;
                                ClickItem(holder);

                                //Observer on get text method
                                downloadedViewModel.getText().observe((LifecycleOwner) context
                                        , new Observer<String>() {
                                            @Override
                                            public void onChanged(String s) {
                                                //When text change
                                                actionMode.setTitle(String.format("%s Selected", s));
                                            }
                                        });
                                return true;
                            }

                            @Override
                            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                                //When click on action mode item
                                int id = menuItem.getItemId();

                                switch (id) {
                                    case R.id.menu_delete:
                                        for (Downloaded s : selectList) {
                                            availableList.add(s);

                                            //Delete from device storage/Database/List
                                            objectActivity.deleteModel(s.getModelKey());
                                            downdb.deleteModel(s.getId());
                                            mListDownloaded.remove(s);
                                        }
                                        if (mListDownloaded.size() == 0)
                                            tvEmpty.setVisibility(View.VISIBLE);

                                        actionMode.finish();
                                        break;
                                    case R.id.menu_select_all:
                                        if (selectList.size() == mListDownloaded.size()) {
                                            isSelectAll = false;
                                            selectList.clear();
                                        } else {
                                            isSelectAll = true;
                                            selectList.clear();
                                            selectList.addAll(mListDownloaded);
                                        }
                                        downloadedViewModel.setText(String.valueOf(selectList.size()));
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
                                for (int i = 0; i < holders.size(); i++) {
                                    DownloadeObjectsViewHolder v = holders.get(i);

                                    v.ivCheckBox.setVisibility(View.GONE);
                                    v.ARIcon.setVisibility(View.VISIBLE);
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
                }else {
                    //Go to AR SCREEN
                    if (context instanceof ObjectActivity)
                        ((ObjectActivity)context).goToArActivity(position); // Pozícia kliknutého modelu (ten sa vyberie v liste na AR obrazovke)
                }
            }
        });

        if(isSelectAll){
            holder.ivCheckBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check));
        }else {
            //all value unselected
            holder.ivCheckBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_uncheck));
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void ClickItem(DownloadeObjectsViewHolder holder) {
        //Get selected item value

        Downloaded s = mListDownloaded.get(holder.getAbsoluteAdapterPosition());

        if (holder.ivCheckBox.getDrawable().getConstantState().equals(context.getResources().getDrawable(R.drawable.ic_uncheck).getConstantState())) {
            //When item not selected
            holder.ivCheckBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check));
            selectList.add(s);
        } else {
            //When item selected
            holder.ivCheckBox.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_uncheck));
            selectList.remove(s);
        }
        downloadedViewModel.setText(String.valueOf(selectList.size()));
    }


    public void notifyMe(){
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        if (mListDownloaded != null)
            return mListDownloaded.size();

        return 0;
    }


    public class DownloadeObjectsViewHolder extends RecyclerView.ViewHolder {

        private TextView downloadedName;
        private ImageView downloadedImage;
        private ImageView ivCheckBox;
        private ImageView ARIcon;


        public DownloadeObjectsViewHolder(@NonNull View itemView) {
            super(itemView);

            downloadedImage = itemView.findViewById(R.id.downloadedImage);
            downloadedName = itemView.findViewById(R.id.downloadedTitle);
            ivCheckBox = itemView.findViewById(R.id.iv_check_box);
            ARIcon = itemView.findViewById(R.id.ARIcon);
        }
    }
}
