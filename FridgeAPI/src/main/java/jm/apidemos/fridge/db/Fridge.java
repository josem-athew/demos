package jm.apidemos.fridge.db;

import jm.apidemos.fridge.exceptions.UnavailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Fridge {

    private String name; //name of fridge
    private List<Item> items = new ArrayList<Item>(); //items in this fridge

    public Fridge(){ super(); }

    public Fridge(String name, List<Item> items){
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    //Get item, null if not present
    public Optional<Item> getItem(String name){
       return  items.stream()
                    .filter(item -> name.equals(item.getName()))
                    .findAny();
    }

    //Get item count
    public Optional<Integer> getItemCount(String name){
        return  items.stream()
                .filter(item -> name.equals(item.getName()))
                .findAny()
                .map(item -> item.getCount());
    }


    //Add item, increment count if already present
    public void addToItem(String name, int count) {
        getItem(name).ifPresentOrElse(
                (item) -> item.add(count),
                () -> addNewItem(new Item(name, count))
        );

    }

    private Item addNewItem(Item item){
        this.items.add(item);
        return item;
    }


    //completely delete item.
    public void deleteItem(String name) throws UnavailableException{
       items.remove (getItem(name).orElseThrow(UnavailableException::new));
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("[Fridge: " + this.name + " Items:[");
        String strItems = this.items.stream()
                                    .map(item -> item.toString())
                                    .collect(Collectors.joining());
        strBuilder.append(strItems);
        strBuilder.append("]");
        return strBuilder.toString();
    }
}
