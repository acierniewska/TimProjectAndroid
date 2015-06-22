package pl.edu.wat.dresscodeapp;

import java.io.Serializable;
import java.util.List;


public class Clothes implements Serializable {
    private String clothesType;
    private String clothesDesc;
    private List<String> clothesTags;
    private String mainColour;
    private byte[] clothesPic;


    public String getClothesType() {
        return clothesType;
    }

    public void setClothesType(String clothesType) {
        this.clothesType = clothesType;
    }

    public String getClothesDesc() {
        return clothesDesc;
    }

    public void setClothesDesc(String clothesDesc) {
        this.clothesDesc = clothesDesc;
    }

    public List<String> getClothesTags() {
        return clothesTags;
    }

    public void setClothesTags(List<String> clothesTags) {
        this.clothesTags = clothesTags;
    }

    public String getMainColour() {
        return mainColour;
    }

    public void setMainColour(String mainColour) {
        this.mainColour = mainColour;
    }

    public byte[] getClothesPic() {
        return clothesPic;
    }

    public void setClothesPic(byte[] clothesPic) {
        this.clothesPic = clothesPic;
    }
}
