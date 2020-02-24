package jm.apidemos.fridge.exceptions;

// If unavailable, or if not enough count to remove from
public class UnavailableException extends Exception {

    public UnavailableException(){
        super();
    }


    public UnavailableException(Exception e){
        super(e);
    }

}
