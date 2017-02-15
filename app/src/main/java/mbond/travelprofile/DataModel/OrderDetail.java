package mbond.travelprofile.DataModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OrderDetail {

    @SerializedName("orderid")
    @Expose
    private String orderid;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("src1")
    @Expose
    private String src1;
    @SerializedName("src2")
    @Expose
    private String src2;
    @SerializedName("dest1")
    @Expose
    private String dest1;
    @SerializedName("dest2")
    @Expose
    private String dest2;
    @SerializedName("cost")
    @Expose
    private String cost;


    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getSrc1() {
        return src1;
    }

    public void setSrc1(String src1) {
        this.src1 = src1;
    }

    public String getSrc2() {
        return src2;
    }

    public void setSrc2(String src2) {
        this.src2 = src2;
    }

    public String getDest1() {
        return dest1;
    }

    public void setDest1(String dest1) {
        this.dest1 = dest1;
    }

    public String getDest2() {
        return dest2;
    }

    public void setDest2(String dest2) {
        this.dest2 = dest2;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

}