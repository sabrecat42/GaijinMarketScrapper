import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemInfo implements Serializable
{
    private final HashMap<String, ArrayList<String>> map;

    public ItemInfo() {
        this.map = new HashMap<>();
    }

    public void putItem(String hashName, String name, String type, String iconAddress) {
        ArrayList<String> attributesList = new ArrayList<>(3);
        attributesList.add(0, name);
        attributesList.add(1, type);
        attributesList.add(2, iconAddress);

        HashMap<String, ArrayList<String>> thisMap = new HashMap<>();
        this.map.put(hashName, attributesList);
    }

    public void putItem(String hashName, ArrayList<String> attributesList) {
        this.map.put(hashName, attributesList);
    }

    public HashMap<String, ArrayList<String>> getMap() {
        return map;
    }
}