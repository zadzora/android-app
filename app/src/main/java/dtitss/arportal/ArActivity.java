package dtitss.arportal;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.arportal.ar.R;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import java.util.List;
import java.util.Objects;


public class ArActivity extends AppCompatActivity {

    // object of ArFragment Class
    private ArFragment arCam;
    private List<Downloaded> downloadedList;
    private ListView listView;
    private ArAdapter arAdapter;
    private ArActivity arActivity;
    private int pos = 0;
    private ImageView closeButton;
    private int clickNo = 0;


    public static boolean checkSystemSupport(Activity activity) {
        // checking whether the API version of the running Android >= 24
        // that means Android Nougat 7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();
            // checking whether the OpenGL version >= 3.0
            if (Double.parseDouble(openGlVersion) >= 3.0) {
                return true;
            } else {
                Toast.makeText(activity, "App needs OpenGl Version 3.0 or later", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
        } else {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_layout);

        listView= findViewById(R.id.modelsList);
        this.arActivity = (ArActivity)this;

        closeButton = findViewById(R.id.exitButton);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            pos = extras.getInt("pos");
            downloadedList = (List<Downloaded>) extras.getSerializable("downloadedObj");
        }

        arAdapter = new ArAdapter(this,R.layout.ar_item_layout,downloadedList, this.arActivity,pos);
        listView.setAdapter(arAdapter);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //for(int i = 0; i<anchors.size(); i++){
                //    anchors.get(i).detach();
                //}
                finish();
            }
        });

        showModel(downloadedList.get(pos).getModelKey());
    }


    public void showModel(String modelkey){
        if (checkSystemSupport(this)) {

            String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+ "/" + modelkey;

            // ArFragment is linked up with its respective id used in the activity_main.xml
            arCam = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.ArCamera);
            arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                clickNo++;
                // the 3d model comes to the scene only
                // when clickNo is one that means once

                //if (clickNo == 1) {
                Anchor anchor = hitResult.createAnchor();
                ModelRenderable.builder()
                        .setSource(this, Uri.parse(path))
                        .setIsFilamentGltf(true)
                        .build()
                        .thenAccept(modelRenderable -> addModel(anchor, modelRenderable))
                        .exceptionally(throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Something is not right" + throwable.getMessage()).show();
                            return null;
                        });
               // }
            });
        } else {
            return;
        }
    }


    private void addModel(Anchor anchor, ModelRenderable modelRenderable) {

        // Creating a AnchorNode with a specific anchor
        AnchorNode anchorNode = new AnchorNode(anchor);

        // attaching the anchorNode with the ArFragment
        anchorNode.setParent(arCam.getArSceneView().getScene());

        // attaching the anchorNode with the TransformableNode
        TransformableNode model = new TransformableNode(arCam.getTransformationSystem());


        model.getScaleController().setMinScale(0.01f);
        model.getScaleController().setMaxScale(2.0f);

        model.setParent(anchorNode);

        // attaching the 3d model with the TransformableNode
        // that is already attached with the node
        model.setRenderable(modelRenderable);
        model.select();
    }


    private void removeAnchorNode(AnchorNode nodeToremove) {
        //Remove an anchor node
        if (nodeToremove != null) {
            arCam.getArSceneView().getScene().removeChild(nodeToremove);

            nodeToremove.getAnchor().detach();
            nodeToremove.setParent(null);
            nodeToremove = null;
            Toast.makeText(ArActivity.this, "Test Delete - anchorNode removed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ArActivity.this, "Test Delete - markAnchorNode was null", Toast.LENGTH_SHORT).show();
        }
    }

}
