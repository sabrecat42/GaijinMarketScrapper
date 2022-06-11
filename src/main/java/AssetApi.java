import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public abstract class AssetApi
{
    protected final UriComponentsBuilder apiUri;
    protected static final String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRoIjoibG9naW4iLCJjbnRyeSI6Ik5MIiwiZXhwIjoxNjU1MDc0MjkxLCJmYWMiOiJjYzQ3ZWQ5YzFjMjNlY2QyYzAzNzFkN2E1YWUxZjEwMmRmNzBkYzlhYTlkNjg5ZDQxZmU4N2NiZjEyNjNhNjQzIiwiaWF0IjoxNjUyNDgyMjkxLCJpc3MiOiIxIiwia2lkIjoiMzgxNDc2IiwibG5nIjoicnUiLCJsb2MiOiJkNmI4MWMwMDM3YTJhMDU4NTg3ODYyODc4ZGFmMTFmMTdlMjdkOTgxZWEwMjBiMmY4NzJjNWYwM2VhN2IyNzMxIiwibmljayI6IlNwaVRmaVJlIiwic2x0IjoiQXZ0ZUZwdk8iLCJ0Z3MiOiIyc3RlcCwyc3RlcF9lbWFpbCwyc3RlcF90b3RwLGN1c3RvbWVyLGN1c3RvbWVyX3d0LGVtYWlsX3ZlcmlmaWVkLGdqcGFzcyxnb29nbGUsbGFuZ19ydSxub251c3NyLHBhcnRuZXJfdW5rbm93bixwaG9uZV92ZXJpZmllZCxwbGF5ZXJfd3Qsc3NvLHNzb19hbGxvd2VkX3Bvc3Qsd3RfZmlyc3RfbG9naW4iLCJ1aWQiOiI0NjI0NzM1OSJ9.ZAM_Lhz0DdcKYl0As5Z9kC-oVXVmyEDzZIUD-nuyvQMIcsUfNDef_HNJkpm5NIN-Rpcf5K6jawjz7calqeYgu1A1K2SzLMjdaMgZs9jO4Taji8to2diavBplx_lF6HzOg3PaaYCSKCzpnTOfNC-DtgOmlsUlO1TeFtfZJU6OD5SWyIhmMHnatZu7Ufi7Z8o_vNV02aMMn87wLEd73xJaIhqseCNcY-oZVYDbvpMggNgOG1yDTtYxhMfV7sMnoRvHoXM6epe-XURsseQs5KDi-ZIYuitAt9RAotofk24JwKfgeGniPw_jPGnQOIuPDgNdMNVHLBklwOWA5tv2PwhxJg";
    protected static String itemsInfoFile = "src/MarketItemsInfo";
    protected static String sellBuyItemsFile = "src/MarketBuySellOffers";

    public AssetApi() {
        this.apiUri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("market-proxy.gaijin.net")
                .path("assetAPI");
    }

    public static String getItemsInfoFile() {
        return itemsInfoFile;
    }

    public static String getSellBuyItemsFile() {
        return sellBuyItemsFile;
    }

    public static ArrayList<ItemSellBuy> filter(ArrayList<String> filterParams) {
        String[] stringParams = {filterParams.get(0)};
        System.out.print(stringParams[0]);
        //doubleParams: [0] = maxPrice; [1] = minPrice, [2] = minOffers, [3] = Sell/BuyDifference
        Double[] doubleParams = parseDoubleParams(filterParams);
        System.out.println(Arrays.toString(doubleParams));

        ArrayList<ItemSellBuy> returnItemList = new ArrayList<>();
        ArrayList<ItemSellBuy> itemsList = ItemBooks.readSellBuyItemsFromFile(AssetApi.sellBuyItemsFile);

        itemsList.stream()
                // filter item type
                .filter(itemSellBuy -> {
                    if (stringParams[0].equals("")) { return true; }
                    return itemSellBuy.getItemInfo().get(1).equals("type:" + stringParams[0]);
                })
                // filter Sell/Buy difference
                .filter(itemSellBuy -> {
                    try {
                        return (Double.valueOf(Objects.requireNonNull(itemSellBuy.getBuySellOffer().get("SELL")).get(0).get(0)) / Double.valueOf(Objects.requireNonNull(itemSellBuy.getBuySellOffer().get("BUY")).get(0).get(0))) >= doubleParams[3];
                    } catch (Exception e) { return false; }
                })
                // filter Sell & Buy offers count
                .filter(itemSellBuy -> {
                    int countBuy = 0;
                    int countSell = 0;
                    for (ArrayList<Integer> buyList : Objects.requireNonNull(itemSellBuy.getBuySellOffer().get("BUY"))) {
                        countBuy += buyList.get(1);
                    }
                    for (ArrayList<Integer> sellList : Objects.requireNonNull(itemSellBuy.getBuySellOffer().get("SELL"))) {
                        countSell += sellList.get(1);
                    }
                    return countBuy >= doubleParams[2] && countSell >= doubleParams[2];
                })
                // filter min Buy offer amount
                .filter(itemSellBuy -> Objects.requireNonNull(itemSellBuy.getBuySellOffer().get("BUY")).get(0).get(0) >= doubleParams[1] * 10_000)
                // filter max Buy offer amount
                .filter(itemSellBuy -> Objects.requireNonNull(itemSellBuy.getBuySellOffer().get("BUY")).get(0).get(0) <= doubleParams[0] * 10_000)
                .forEach(returnItemList::add);

        // build and print filtered items info
        StringBuilder stringBuilder = new StringBuilder();
        for (ItemSellBuy eachItem : returnItemList) {
            stringBuilder.append("HashName: ")
                    .append(eachItem.getItemName()).append("\n")
                    .append("MarketName: ").append(eachItem.getItemInfo().get(0)).append("\n")
                    .append(eachItem.getItemInfo().get(1)).append("\n")
                    .append("sell: ").append(String.valueOf((Objects.requireNonNull(eachItem.getBuySellOffer().get("SELL")).get(0).get(0)) / 10000.0)).append(" GJN\n")
                    .append("buy: ").append(String.valueOf((Objects.requireNonNull(eachItem.getBuySellOffer().get("BUY")).get(0).get(0)) / 10000.0)).append(" GJN\n")
                    .append("\n");
        }
        System.out.println(stringBuilder.toString());

        // write filtered items to file
        try {
            FileWriter fileWriter = new FileWriter("src/FilterFile");
            BufferedWriter writer = new BufferedWriter(fileWriter);
            writer.append(stringBuilder.toString());
            writer.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return returnItemList;
    }

    public static Double[] parseDoubleParams(ArrayList<String> filterParams) {
        Double[] doubleParams = {Double.MAX_VALUE, 0.0, 0.0, 0.0};
        label:
        try {
            if (filterParams.get(1).equals("")) {
                break label;
            } else if (Double.parseDouble(filterParams.get(1)) >= 0) {
                doubleParams[0] = Double.valueOf(filterParams.get(1));
            }
        } catch (NumberFormatException exception) { System.out.println("Unaccepted maxPrice value (must be real and >= 0)"); }
        label:
        try {
            if (filterParams.get(2).equals("")) {
                break label;
            } else if (Double.parseDouble(filterParams.get(2)) >= 0) {
                doubleParams[1] = Double.valueOf(filterParams.get(2));
            }
        } catch (Exception exception) { System.out.println("Unaccepted minPrice value (must be real and >= 0)"); }
        label:
        try {
            if (filterParams.get(3).equals("")) {
                break label;
            } else if (Double.parseDouble(filterParams.get(3)) >= 0) {
                doubleParams[2] = Double.valueOf(filterParams.get(3));
            }
        } catch (Exception exception) { System.out.println("Unaccepted minOffers value (must be real and >= 0)"); }
        label:
        try {
            if (filterParams.get(4).equals("")) {
                break label;
            } else if (Double.parseDouble(filterParams.get(4)) >= 0) {
                doubleParams[3] = Double.parseDouble(filterParams.get(4));
            }
        } catch (Exception exception) { System.out.println("Unaccepted difference value (must be real and >= 0)"); }

        return doubleParams;
    }

    public static void main(String[] args) {
        ItemBooks itemBooks = new ItemBooks();
        MarketSearch marketSearch = new MarketSearch();

        marketSearch.setTextParam("mig 21");
        try {
            for (Map.Entry<String, ArrayList<String>> itemEntry : marketSearch.parseItemIds().getMap().entrySet()) {
                System.out.println(itemEntry.getKey());
                System.out.println(itemEntry.getValue().toString());
            }
        } catch (Exception e) {
            System.out.println("No item found");
        }

/*
        try {
            ArrayList<String> typeList = new ArrayList<>();

            FileInputStream fileInputStream = new FileInputStream("src/MarketItemsInfo");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            ItemInfo itemsMap = (ItemInfo) objectInputStream.readObject();
            itemsMap.getMap().keySet().forEach(key -> {
                if (itemsMap.getMap().get(key).get(1).equals("type:attachable")) {
                    System.out.println(itemsMap.getMap().get(key).get(0));
                }
                if (!(typeList.contains(itemsMap.getMap().get(key).get(1)))) {
                    typeList.add(itemsMap.getMap().get(key).get(1));
                }
            });
            typeList.forEach(System.out::println);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
*/
    }
}