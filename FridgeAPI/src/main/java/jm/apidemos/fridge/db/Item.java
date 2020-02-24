package jm.apidemos.fridge.db;

import jm.apidemos.fridge.exceptions.UnavailableException;

public class Item {

    private String name; // item name
    private int count  = 0; // storing item count for simplicity rather than having multiple item objects of same name

    public Item(){
        super();
    }

    public Item(String name, int initCount){
        this.name = name;
        this.count = initCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    //Also defines operations in spite of being a model


    public void add(int count)  {
        this.count += count;
    }

    public void remove(int count) throws UnavailableException {
        /* this should not happen */
        //if((this.count - count) < 0) throw new UnavailableException();
        this.count -= count;
    }

    @Override
    public String toString() {
        return "[Item: " + this.name + " count: " + this.count + "]";
    }
}
