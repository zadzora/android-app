package dtitss.arportal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.arportal.ar.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ListAdapter extends ArrayAdapter<Categories>{

    private Context context;
    private List<Categories> categoriesList;
    private int resource;

    ListAdapter(Context context,int resource,List<Categories> categoriesList) {
        super(context,resource,categoriesList);
        this.context = context;
        this.resource = resource;
        this.categoriesList = categoriesList;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(resource,null,false);

        TextView categoryName = view.findViewById(R.id.categoryName);
        ImageView categoryImage = view.findViewById(R.id.categoryImageURL);
        TextView categoryCount = view.findViewById(R.id.categoryCount);

        Categories categories = categoriesList.get(position);

        categoryName.setText(categories.getCategoryName());
        categoryCount.setText(String.valueOf(categories.getCount()));
        Picasso.with(context).load(categories.getImageURL()).into(categoryImage);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof MainActivity) {
                    String id = String.valueOf(categories.getId());
                    ((MainActivity)context).nextActivity(id);
                }
            }
        });

        return view;
    }
}


