package dtitss.arportal;

import java.io.Serializable;

public class Downloaded implements Serializable {

    private int id;
    private String name;
    private String imageURL;
    private String modelKey;
    private int categoryId;
    private int modelSize;

    public Downloaded(int id,String name,String imageURL, String modelKey, int categoryId, int modelSize){
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.modelKey = modelKey;
        this.categoryId = categoryId;
        this.modelKey = modelKey;
        this.modelSize = modelSize;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getModelKey() {
        return modelKey;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getModelSize() {
        return modelSize;
    }
}
