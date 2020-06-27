package per.pccontrol;


/**
 * Created by UYScuti on 2017/12/22.
 */

public class DrawerListItem {

    public String name;
    private String extra;
    public int imgId;
    private Class action;



    public String getName() {
        return name;
    }
    public void setExtra(String extra){
        this.extra=extra;
    }

    public String getExtra(){
        return extra;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getimgId() {
        return imgId;
    }

    public void setimgId(int id) {
        this.imgId = id;
    }

    public void setAction(Class action){
        this.action=action;
    }

    public Class getAction(){
        return action;
    }
}
