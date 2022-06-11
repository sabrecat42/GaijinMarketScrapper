import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.util.LinkedMultiValueMap;

import java.io.*;
import java.util.*;

public class ItemBooks extends AssetApi {
    public ItemBooks() {
        super();
        this.apiUri.replaceQueryParam("action", "cln_books_brief");
        this.apiUri.replaceQueryParam("token", token);
        this.apiUri.replaceQueryParam("appid", "1067");
        this.apiUri.replaceQueryParam("market_name", "");
    }

    public String getJSON() {
        try {
            System.out.println(this.apiUri.toUriString());
            Document doc = Jsoup.connect(this.apiUri.toUriString()).ignoreContentType(true).post();
            Elements responseBody = doc.select("body");
            return responseBody.text();
        } catch (Exception e) {
            return e.toString();
        }
    }

    public LinkedMultiValueMap<String, ArrayList<Integer>> extractBuySell() {
        LinkedMultiValueMap<String, ArrayList<Integer>> buySellOffers = new LinkedMultiValueMap<>();
        try {
            JSONObject jsonObject = new JSONObject(getJSON());

            JSONArray buyArray = jsonObject.getJSONObject("response").getJSONArray("BUY");
            JSONArray sellArray = jsonObject.getJSONObject("response").getJSONArray("SELL");

            for (int i = 0; i < buyArray.length(); i++) {
                JSONArray eachBuyOffer = (JSONArray) buyArray.get(i);
                ArrayList<Integer> buyAndCount = new ArrayList<>();
                buyAndCount.add(eachBuyOffer.getInt(0));
                buyAndCount.add(eachBuyOffer.getInt(1));
                buySellOffers.add("BUY", buyAndCount);
            }
            for (int i = 0; i < sellArray.length(); i++) {
                JSONArray eachSellOffer = (JSONArray) sellArray.get(i);
                ArrayList<Integer> sellAndCount = new ArrayList<>();
                sellAndCount.add(eachSellOffer.getInt(0));
                sellAndCount.add(eachSellOffer.getInt(1));
                buySellOffers.add("SELL", sellAndCount);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return buySellOffers;
    }

    public ArrayList<String> readItemHashNamesFromFile() {
        ArrayList<String> itemHashNames = new ArrayList<>();
        try {
            itemHashNames.addAll(MarketSearch.readItemsInfoFromFile(AssetApi.itemsInfoFile).getMap().keySet());
            return itemHashNames;
        } catch (Exception exception) {
            exception.printStackTrace();
            return itemHashNames;
        }
    }

    public HashMap<String, LinkedMultiValueMap<String, ArrayList<Integer>>> extractAllBuySell() {
        HashMap<String, LinkedMultiValueMap<String, ArrayList<Integer>>> allBuySellMap = new HashMap<>();
        ArrayList<String> itemHashNames = readItemHashNamesFromFile();

        itemHashNames.forEach(listItem -> {
            this.apiUri.replaceQueryParam("market_name", listItem);
            allBuySellMap.put(listItem, extractBuySell());
        });

        return allBuySellMap;
    }

    public boolean writeSellBuyItemsToFile(String sellBuyItemsFile) {
        try {
            HashMap<String, LinkedMultiValueMap<String, ArrayList<Integer>>> buySellMap = extractAllBuySell();
            List<ItemSellBuy> entryList = new ArrayList<>();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(sellBuyItemsFile));
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(itemsInfoFile));
            ItemInfo itemsInfo = (ItemInfo) objectInputStream.readObject();

            for (Map.Entry<String, LinkedMultiValueMap<String, ArrayList<Integer>>> entry : buySellMap.entrySet()) {
                entryList.add(new ItemSellBuy(entry.getKey(), itemsInfo.getMap().get(entry.getKey()), entry.getValue()));
            }
            objectOutputStream.writeObject(entryList);
            objectOutputStream.close();
            objectInputStream.close();

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public static ArrayList<ItemSellBuy> readSellBuyItemsFromFile(String sellBuyItemsFile) {
        ArrayList<ItemSellBuy> itemsList = new ArrayList<>();

        try {
            FileInputStream fileInputStream = new FileInputStream(sellBuyItemsFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            itemsList = (ArrayList<ItemSellBuy>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return itemsList;
    }
}

class ItemSellBuy implements Serializable
{
    private final String itemName;
    private final ArrayList<String> itemInfo;
    private final LinkedMultiValueMap<String, ArrayList<Integer>> buySellOffer;

    public ItemSellBuy(String itemName, ArrayList<String> itemInfo, LinkedMultiValueMap<String, ArrayList<Integer>> itemOffers) {
        this.itemName = itemName;
        this.itemInfo = itemInfo;
        this.buySellOffer = itemOffers;
    }

    public String getItemName() {
        return itemName;
    }

    public ArrayList<String> getItemInfo() {
        return itemInfo;
    }

    public LinkedMultiValueMap<String, ArrayList<Integer>> getBuySellOffer() {
        return buySellOffer;
    }
}
