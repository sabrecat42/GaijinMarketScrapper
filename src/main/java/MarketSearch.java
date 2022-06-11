import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;

class MarketSearch extends AssetApi
{
    public MarketSearch() {
        super();
        this.apiUri.queryParam("action", "cln_market_search");
        this.apiUri.queryParam("token", token);
        this.apiUri.queryParam("appid", "1165");
        this.apiUri.queryParam("skip", "0");
        this.apiUri.queryParam("count", "100");
        this.apiUri.queryParam("text", "");
        this.apiUri.queryParam("language", "en_US");
        this.apiUri.queryParam("sort", "");
    }

    public void setSortParam(String sortParam) {
        this.apiUri.replaceQueryParam("sort", sortParam);
    }

    public void setTextParam(String textParam) {
        this.apiUri.replaceQueryParam("text", textParam);
    }

    public String getJSON() {
        try {
            Document doc = Jsoup.connect(this.apiUri.toUriString()).ignoreContentType(true).post();
            Elements responseBody = doc.select("body");
            return responseBody.text();

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public ItemInfo parseItemIds() {
        ItemInfo itemsInfo = new ItemInfo();
        JSONObject jsonObject = new JSONObject(getJSON());

        JSONArray jsonArray = jsonObject
                .getJSONObject("response")
                .getJSONArray("assets");
        for (Object eachArrayItem : jsonArray) {
            JSONObject jsonItem = (JSONObject) eachArrayItem;
            // extract all item info from each
            String hashName = (String) jsonItem.get("hash_name");
            String name = (String) jsonItem.get("name");
            String type = jsonItem.getJSONArray("tags").getString(0);
            String iconAddress = (String) jsonItem.get("icon");

            itemsInfo.putItem(hashName, name, type, iconAddress);
            //itemIds.add((String) jsonItem.get("hash_name"));
        }
        return itemsInfo;
    }

    public ItemInfo parseFirstItemId() throws Exception {
        ItemInfo itemInfo = new ItemInfo();
        JSONObject jsonObject = new JSONObject(getJSON());

        JSONArray jsonArray = jsonObject
                .getJSONObject("response")
                .getJSONArray("assets");
        JSONObject jsonItem = (JSONObject) jsonArray.get(0);
        String hashName = (String) jsonItem.get("hash_name");
        String name = (String) jsonItem.get("name");
        String type = jsonItem.getJSONArray("tags").getString(0);
        String iconAddress = (String) jsonItem.get("icon");

        itemInfo.putItem(hashName, name, type, iconAddress);
        return itemInfo;
    }

    private ItemInfo parseAllItemsIds() {
        ItemInfo allItemIds = new ItemInfo();
        int skip = 0;
        do {
            parseItemIds().getMap().forEach(allItemIds::putItem);
            System.out.println(this.apiUri.toUriString());
            this.apiUri.replaceQueryParam("skip", String.valueOf(skip));
            skip += 100;
        } while (new JSONObject(getJSON()).getJSONObject("response").getJSONArray("assets").length() != 0);

        return allItemIds;
    }

    public boolean writeResultsToFile(String itemsInfoFile) {
        try {
            FileOutputStream outputStream = new FileOutputStream(itemsInfoFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(parseAllItemsIds());
            return true;
        } catch (Exception exception) {
            System.out.println(this.apiUri.toUriString());
            exception.printStackTrace();
            return false;
        }
    }

    public static ItemInfo readItemsInfoFromFile(String itemsInfoFile) throws Exception {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(itemsInfoFile));
        return (ItemInfo) objectInputStream.readObject();
    }
}