package dtitss.arportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.arportal.ar.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class ArAdapter extends ArrayAdapter<Downloaded>{

    private Context context;
    private int resource;
    private ArActivity arActivity;

    private List<Downloaded> downloadedList;
    private List<View> allViews = new ArrayList<>();
    private int pos;


    public ArAdapter(@NonNull Context context, int resource, List<Downloaded> downloadedList, ArActivity arActivity, int pos) {
        super(context, resource,downloadedList);
        this.context = context;
        this.resource = resource;
        this.downloadedList = downloadedList;
        this.arActivity = arActivity;
        this.pos = pos;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource,null,false);

        ImageView downloadedImage = view.findViewById(R.id.arImage);
        LinearLayout linearLayout = view.findViewById(R.id.layout_id);

        allViews.add(view);                                             // Pre identifikáciu ktorý model je vybraný.
        arActivity.showModel(downloadedList.get(pos).getModelKey());    // Zobrazenie toho modelu na ktorý používateľ klikol. Použivateľ môže zobraziť iba stiahnuté modely.

        if(position == pos)
            view.setBackground(context.getResources().getDrawable(R.drawable.ar_items_bg_active));

        //Icon of downloaded image
        Picasso.with(context).load(downloadedList.get(position).getImageURL()).into(downloadedImage);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for(int i=0;i < allViews.size();i++){
                    allViews.get(i).setBackground(context.getResources().getDrawable(R.drawable.ar_items_bg));
                }
                allViews.clear();
                allViews.add(view);
                view.setBackground(context.getResources().getDrawable(R.drawable.ar_items_bg_active));
                arActivity.showModel(downloadedList.get(position).getModelKey());
            }
        });

        return view;
    }
}


