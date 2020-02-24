package jm.apidemos.fridge.db;

import jm.apidemos.fridge.exceptions.CountExceededException;
import jm.apidemos.fridge.exceptions.UnavailableException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class FridgeDB {

    private List<Fridge> fridges = new ArrayList<Fridge>(); //Fridges in the fridge database

    //This kind of business logic should be handled by its own handler
    private static final String CONSTR_ITEM = "SODA";
    private static final int CONSTR_COUNT = 12;

    public List<Fridge> getFridges() {
        return fridges;
    }

    public void setFridges(List<Fridge> fridges) {
        this.fridges = fridges;
    }

    //Get fridge, null if not present
    public Optional<Fridge> getFridge(String name){
        return  fridges.stream()
                .filter(fridge -> name.equals(fridge.getName()))
                .findAny();
    }

    public boolean hasFridge(String name){
        return !getFridge(name).isEmpty();
    }

    //Add fridge
    public boolean addFridge(String fridgeName){
        if(fridgeName != null && !hasFridge(fridgeName)) {
            this.fridges.add(new Fridge(fridgeName, new ArrayList<Item>()));
            return true;
        }
        else{ return  false;}
    }


    //Delete item in fridge
    public void removeItemInFridge(String fridgeName, String itemName) throws UnavailableException{

        Fridge fridge = getFridge(fridgeName).orElseThrow(UnavailableException::new);
        Item item = fridge.getItem(itemName).orElseThrow(UnavailableException::new);
        item.remove(1);
        if(item.getCount() == 0){ fridge.deleteItem(itemName); }

    }

    //Get item in fridge
    public Item getItemInFridge(String fridgeName, String itemName) throws UnavailableException{
        Fridge fridge = getFridge(fridgeName).orElseThrow(UnavailableException::new);
        return fridge.getItem(itemName).orElseThrow(UnavailableException::new);
    }

    public void removeFridge(String fridgeName) throws UnavailableException {
        fridges.remove(getFridge(fridgeName).orElseThrow(UnavailableException::new));
    }

    //Add item to fridge, expects fridge to exist
    public void addItemtoFridge(String fridgeName, String item) throws UnavailableException, CountExceededException {

     try {
            Fridge fridge = getFridge(fridgeName).get();
            Integer count = 0;

            try{
                count = fridge.getItemCount(item).get();
            }
            catch(NoSuchElementException nosuchElEx){ /*ignore*/ }
            //Cannot exceed configured max count for the configured item
            if (item.toUpperCase().equalsIgnoreCase(CONSTR_ITEM) && (count + 1) > CONSTR_COUNT) {
                throw new CountExceededException();
            } else {
                fridge.addToItem(item, 1);
            }
       }
        catch(NoSuchElementException nosuchElEx){
            throw new UnavailableException(nosuchElEx);
        }
    }


    @Override
    public String toString() {
        return this.fridges.stream()
                .map(fridge -> fridge.toString())
                .collect(Collectors.joining());
    }


}
