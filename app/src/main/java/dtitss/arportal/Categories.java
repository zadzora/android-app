package dtitss.arportal;

public class Categories {

    private int id;
    private String categoryName;
    private String imageURL;
    private int count;

    public Categories(int id, String categoryName, String imageURL, int count){
       this.id = id;
       this.categoryName = categoryName;
       this.imageURL = imageURL;
       this.count = count;
    }

    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public int getCount() {
        return count;
    }
}
