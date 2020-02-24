package jm.apidemos.fridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jm.apidemos.fridge.db.Fridge;
import jm.apidemos.fridge.db.Item;
import jm.apidemos.fridge.main.FridgeRatPackApp;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ratpack.test.MainClassApplicationUnderTest;
import ratpack.test.http.TestHttpClient;
import java.util.NoSuchElementException;


import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
public class FridgeRatPackAppTest {

    //MainClassApplicationUnderTest appUnderTest = new MainClassApplicationUnderTest(FridgeRatPackApp.class);
    public static final TestHttpClient client = new MainClassApplicationUnderTest(FridgeRatPackApp.class).getHttpClient();


    @BeforeClass
    public static void populate() {
        System.out.println("@BeforeClass: populate here");
    }

    //test test
    @Test
    public void test() {
        assertEquals(200, 200);
    }

    //Test failed auth
    @Test
    public void whenAuthFail_thenGot401() {
        assertEquals(401,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","badtoken")))
                        .get("/fridges").getStatusCode());
    }


    //Test /fridges get returns ok even if empty
    @Test
    public void whenViewAll_thenGotOK() {
        assertEquals(200,
                client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .get("/fridges").getStatusCode());
    }


    //Test put fridge
    @Test
    public void whenPutFridge_thenGotOK() throws JsonProcessingException {
        assertEquals(200,
                        client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .put("/fridges/fridge1").getStatusCode());
    }


    //Test get fridge
    @Test
    public void whenGetFridge_thenGotFridge() throws JsonProcessingException {
        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge1.1");
        Fridge ret = new ObjectMapper().readValue(
                        client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .get("/fridges/fridge1.1").getBody().getText(), Fridge.class);
        assertEquals("fridge1.1",  ret.getName());
    }

    //Test put duplicate fridge
    @Test
    public void whenPutDuplicateFridge_thenGot409() throws JsonProcessingException {
        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge_dup");
        assertEquals(409,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .put("/fridges/fridge_dup").getStatusCode());
    }

    //Test delete fridge
    @Test
    public void whenDeleteFridge_thenGotOK() throws JsonProcessingException {

        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge1.1.1");
        assertEquals(200,
                        client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .delete("/fridges/fridge1.1.1").getStatusCode());
    }

    //Test get non-existing fridge
    @Test
    public void whenNoFridge_thenGot404() throws JsonProcessingException {
        assertEquals(404,
                        client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .get("/fridges/fridge_bad").getStatusCode());
    }

    //Test get non-existing fridge
    @Test
    public void whenDeleteNonExistingFridge_thenGot404() throws JsonProcessingException {
        assertEquals(404,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .delete("/fridges/fridge_bad").getStatusCode());
    }

    //Test put new item into new fridge
    @Test
    public void whenPutNewFridgeAndItem_thenGotFridgeAndItem() throws JsonProcessingException, NoSuchElementException {
                client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge2");
                client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge2/soda");
        Fridge ret = new ObjectMapper().readValue(
                        client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .get("/fridges/fridge2").getBody().getText(), Fridge.class);
        assertEquals("soda",  ret.getItem("soda").get().getName());
    }


    //Test put max number of sodas
    @Test
    public void whenPutMaxItems_thenGotOK() throws JsonProcessingException, NoSuchElementException {
                client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge3");
        //we dont have an endpoint to put a specific quantity. its one at a time. so looping to put 12 items
        for(int i=0; i <12; i++) {
                    client
                    .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                    .put("/fridges/fridge3/soda");
        }
        Fridge ret = new ObjectMapper().readValue(
                    client
                    .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                    .get("/fridges/fridge3").getBody().getText(), Fridge.class);
        assertEquals(12,  ret.getItem("soda").get().getCount());
    }

    //Test put item into non existing fridge
    @Test
    public void whenItemToBadFridge_thenGot404() throws JsonProcessingException, NoSuchElementException {
        assertEquals(404,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .put("/fridges/fridge_bad/cheese").getStatusCode());
    }



    //Test exceed max number of sodas
    @Test
    public void whenPutMoreThanMaxSoda_thenGot409() throws JsonProcessingException, NoSuchElementException {
                client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge4");
        //we dont have an endpoint to put a specific quantity. its one at a time. so looping to put 12 items
        for(int i=0; i < 12; i++) {
                    client
                    .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                    .put("/fridges/fridge4/soda");
        }
        Fridge ret = new ObjectMapper().readValue(client.get("/fridges/fridge4").getBody().getText(), Fridge.class);
        assertEquals(409,
                    client
                    .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                    .put("/fridges/fridge4/soda").getStatusCode());
    }

    //Test get non-existing item
    @Test
    public void whenNoItem_thenGot404() throws JsonProcessingException {

        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge5");
        //fridge exists, item does not exist
        assertEquals(404,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .get("/fridges/fridge5/baditem").getStatusCode());
        //fridge does not exist
        assertEquals(404,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .get("/fridges/badfridge/baditem").getStatusCode());
    }

    //Test get item
    @Test
    public void whenGetItem_thenGotItem() throws JsonProcessingException {
        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge6");
        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge6/cake");
        Item ret = new ObjectMapper().readValue(
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .get("/fridges/fridge6/cake").getBody().getText(), Item.class);
        assertEquals("cake",  ret.getName());
    }

    //Test delete item
    @Test
    public void whenDeleteItem_thenGotOK() throws JsonProcessingException {

        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge7");
        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge7/cheese");
        assertEquals(200,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .delete("/fridges/fridge7/cheese").getStatusCode());

        assertEquals(404,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .delete("/fridges/fridge7/cheese").getStatusCode());
    }

    //Test get non-existing fridge
    @Test
    public void whenDeleteNonExistingItem_thenGot404() throws JsonProcessingException {
        client
                .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                .put("/fridges/fridge8");
        assertEquals(404,
                client
                        .requestSpec(r -> r.headers(h-> h.set("Authorization","dummytoken")))
                        .delete("/fridges/fridge8/baditem").getStatusCode());
    }


}
