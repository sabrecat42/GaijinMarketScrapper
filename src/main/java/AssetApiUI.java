import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class AssetApiUI
{
    private final Scanner scanner;
    private final MarketSearch marketSearch;
    private final ItemBooks itemBooks;

    public AssetApiUI(Scanner scanner) {
        this.scanner = scanner;
        this.marketSearch = new MarketSearch();
        this.itemBooks = new ItemBooks();
    }

    private void start() {
        System.out.println("""
                Commands:
                update <"itemList" or "sbList" or "both"> - to update market items list or sell/buy item offers list or both
                filter *diff=<minSell/maxBuy_ratio* *type=<type>* *maxp=<maxPrice>* *minp=<minPrice>* *minoffs=<minSell&BuyOffers>* (*...* - optional)
                find <hashName> - to search info about the item
                image <hashName> - to get item image link
                help - print commands again
                
                close - to close app
                """);
        while (true) {
            String[] inputList = this.scanner.nextLine().split(" ");

            switch (inputList[0]) {
                case "update" -> {
                    switch (inputList[1]) {
                        case "itemList" -> {
                            if (this.marketSearch.writeResultsToFile(AssetApi.getItemsInfoFile())) {
                                System.out.println("Items list was updated successfully");
                            } else {
                                System.out.println("Failed to update Items list");
                            }
                        }
                        case "sbList" -> {
                            if (this.itemBooks.writeSellBuyItemsToFile(AssetApi.getSellBuyItemsFile())) {
                                System.out.println("Sell & Buy offers list was updated successfully");
                            } else {
                                System.out.println("Failed to update Sell & Buy offers list");
                            }
                        }
                        case "both" -> {
                            if (this.marketSearch.writeResultsToFile(AssetApi.getItemsInfoFile()) && this.itemBooks.writeSellBuyItemsToFile(AssetApi.getSellBuyItemsFile())) {
                                System.out.println("Item list and Sell & Buy offers list were updated successfully");
                            } else {
                                System.out.println("Failed to update Item list and Sell & Buy offers list");
                            }
                        }
                        default -> System.out.println("Unknown parameter. Accepted parameters: \"itemList\", \"sbList\", \"both\".");
                    }
                }
                case "filter" -> {
                    AssetApi.filter(extractFilterParams(inputList));
                }
                case "find" -> {
                    ArrayList<String> inputArrayList = new ArrayList<>(Arrays.asList(inputList));
                    inputArrayList.remove("find");
                    String hashName = String.join(" ", inputArrayList);

                    try {
                        ItemInfo itemsInfo = MarketSearch.readItemsInfoFromFile(AssetApi.itemsInfoFile);
                        ArrayList<String> itemInfo = itemsInfo.getMap().get(hashName);
                        if (itemInfo == null) {
                            System.out.println("Such item was not founded");
                            break;
                        }
                        System.out.println("MarketName: " +
                                itemInfo.get(0) +
                                "\nType: " +
                                itemInfo.get(1).split(":")[1] +
                                "\nImage link: " +
                                itemInfo.get(2) +
                                "\n");
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                case "image" -> {
                    ArrayList<String> inputArrayList = new ArrayList<>(Arrays.asList(inputList));
                    inputArrayList.remove("search");
                    String hashName = String.join(" ", inputArrayList);

                    try {
                        ItemInfo itemsInfo = MarketSearch.readItemsInfoFromFile(AssetApi.itemsInfoFile);
                        ArrayList<String> itemInfo = itemsInfo.getMap().get(hashName);
                        if (itemInfo == null) {
                            System.out.println("Such item was not founded");
                            break;
                        }
                        System.out.println("Image link: " + itemInfo.get(2));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                case "help" ->
                    System.out.println("""
                Commands:
                update <"itemList" or "sbList" or "both"> - to update market items list or sell/buy item offers list or both
                filter *diff=<minSell/maxBuy_ratio* *type=<type>* *maxp=<maxPrice>* *minp=<minPrice>* *minoffs=<minSell&BuyOffers>* (*...* - optional)
                find <hashName> - to search info about the item
                image <hashName> - to get item image link
                help - print commands again
                
                close - to close app
                """);
                case "close" -> System.exit(0);
                default -> System.out.println("Unknown command.");
            }
        }
    }

    public ArrayList<String> extractFilterParams(String[] inputList) {
        ArrayList<String> filterParams = new ArrayList<>(5);
        filterParams.addAll(Arrays.asList("", "", "", "", ""));

        ArrayList<String> inputArrayList = new ArrayList<>(Arrays.asList(inputList));
        inputArrayList.remove("filter");
        inputArrayList.forEach(filterParam -> {
            if (filterParam.contains("=")) {
                String[] splitParam = filterParam.split("=");
                switch (splitParam[0]) {
                    case "type" -> filterParams.set(0, splitParam[1]);
                    case "maxp" -> filterParams.set(1, splitParam[1]);
                    case "minp" -> filterParams.set(2, splitParam[1]);
                    case "minoffs" -> filterParams.set(3, splitParam[1]);
                    case "diff" -> filterParams.set(4, splitParam[1]);
                    default -> System.out.println(splitParam[0] + " is not an accepted parameter type");
                }
            }
        });

        return filterParams;
    }

    public static void main(String[] args) {
        AssetApiUI gaijinMarketApp = new AssetApiUI(new Scanner(System.in));
        gaijinMarketApp.start();
    }
}
