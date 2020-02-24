package jm.apidemos.fridge.ratpackserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import jm.apidemos.fridge.appmetrics.MetricsConf;
import jm.apidemos.fridge.db.FridgeDB;
import jm.apidemos.fridge.exceptions.CountExceededException;
import jm.apidemos.fridge.exceptions.UnavailableException;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.http.client.direct.HeaderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.dropwizard.metrics.DropwizardMetricsConfig;
import ratpack.dropwizard.metrics.DropwizardMetricsModule;
import ratpack.error.ServerErrorHandler;
import ratpack.func.Action;
import ratpack.guice.BindingsSpec;
import ratpack.handling.Chain;
import ratpack.handling.RequestLogger;
import ratpack.jackson.Jackson;
import ratpack.pac4j.RatpackPac4j;
import ratpack.server.ServerConfigBuilder;
import ratpack.session.SessionModule;
import ratpack.spring.config.RatpackProperties;
import ratpack.spring.config.RatpackServerCustomizerAdapter;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

//
/**
 * Extending  {@link ratpack.spring.config.RatpackServerCustomizerAdapter}
 * instead of implementing  {@link ratpack.spring.config.RatpackServerCustomizer}
 */
public class RatPackServer extends RatpackServerCustomizerAdapter {

    /**
     * Properties to configure {@link ratpack.dropwizard.metrics.DropwizardMetricsModule}.
     */
    private final MetricsConf dropwzMetrics;

    /**
     * Configure {@link ratpack.spring.config.RatpackProperties}.
     */
    private final RatpackProperties ratPackProps;


    public RatPackServer(
            final RatpackProperties ratPackProps,
            final MetricsConf dropwzMetrics) {
        this.ratPackProps = ratPackProps;
        this.dropwzMetrics = dropwzMetrics;
    }


    /**
     * Set server configuration items
     * @return Server Config builder
     */
    @Override
    public Action<ServerConfigBuilder> getServerConfig() {
        return serverConfigBuilder -> {
            serverConfigBuilder.development(ratPackProps.isDevelopment());
        };
    }

    /**
     * @return Ratpack Handlers
     */
    @Override
    public List<Action<Chain>> getHandlers() {
        return Arrays.asList(messageHandler());
    }

    /**
     * Bind to {@link DropwizardMetricsModule}
     * @return Ratpack bindings
     */
    @Override
    public Action<BindingsSpec> getBindings() {
        return bindings -> {
            bindings.module(DropwizardMetricsModule.class, dropwzMetricsConfig());
            bindings.module(new SessionModule());
        };
    }



    /**
     * Config for {@link DropwizardMetricsModule}
     * @return Config Action for {@link DropwizardMetricsModule}
     */
    private Action<DropwizardMetricsConfig> dropwzMetricsConfig() {
        return config -> {
            if (dropwzMetrics.isJmx()) {
                config.jmx();
            }
            if (dropwzMetrics.getSlf4j().isEnabled()) {
                config.slf4j(slf4jConfig -> slf4jConfig
                        .enable(true)
                        .reporterInterval(Duration.ofSeconds(dropwzMetrics.getSlf4j().getInterval())));
            }
            /* JVM metrics */
            //config.jvmMetrics(true);
        };
    }

    /**
      * Ratpack messagehandler chain
      * @return a Ratpack chain with action implementations
      */
    private Action<Chain> messageHandler() {

        // simple auth for demo purposes, hardcoded non-expiring token
        final HeaderClient headerClient = new HeaderClient("Authorization", ((credentials, webContext) -> {
            String token = ((TokenCredentials) credentials).getToken();
            // check the token and create a profile
            if ("dummytoken".equals(token)) {
                CommonProfile profile = new CommonProfile();
                profile.setId("myId");
                // save in the credentials to be passed to the default AuthenticatorProfileCreator
                credentials.setUserProfile(profile);
            }
        }));


        return chain -> chain
                // Add logging for requests.
                .all(RequestLogger.ncsa())
                .register(r -> r.add (ServerErrorHandler.class,
                        (context, throwable) ->
                        {
                            LOGGER.error(throwable.getMessage());
                            context.render("api-exception: " + throwable.getMessage());
                        }
                        )
                )
                //.register(r -> r.add(Guice.registry(b -> b.module(SessionModule.class))))
                .all(RatpackPac4j.authenticator(headerClient)) //deprecated!
                .all(RatpackPac4j.requireAuth(HeaderClient.class))
                .get("fridges", ctx -> {
                    LOGGER.info("All Fridges: " +  new ObjectMapper().writeValueAsString(fridgeDb));
                    ctx.render(Jackson.json(fridgeDb));
                })
                .path("fridges/:fridgename",
                        ctx ->
                                ctx.byMethod(m -> m
                                        .get(() -> {
                                            final String fridgeName = ctx.getPathTokens().get("fridgename");
                                            LOGGER.info("Getting Fridge: " + fridgeName);
                                            if(fridgeDb.hasFridge(fridgeName)) {
                                                ctx.render(Jackson.json(fridgeDb.getFridge(fridgeName).get()));
                                            }
                                            else{
                                                LOGGER.warn("Fridge not found: " + fridgeName);
                                                LOGGER.debug("Fridge db:: " + fridgeDb);
                                                ctx.getResponse().status(404).send("Fridge not found");
                                            }
                                        })
                                        .put(() -> {
                                                    final String fridgeName = ctx.getPathTokens().get("fridgename");
                                                    if(fridgeDb.addFridge(fridgeName)) {
                                                        LOGGER.info("Added fridge:" + fridgeName);
                                                        ctx.getResponse().status(201).send();
                                                    }
                                                    else {
                                                        LOGGER.warn("Empty name or fridge already exists. Returning 409 (resource conflict)");
                                                        LOGGER.debug("Fridge db:: " + fridgeDb);
                                                        ctx.getResponse().status(409).send("Empty name or fridge already exists");
                                                    }
                                                }
                                        )
                                        .delete(() -> {
                                                    final String fridgeName = ctx.getPathTokens().get("fridgename");
                                                    try {
                                                        fridgeDb.removeFridge(fridgeName);
                                                        LOGGER.info("Deleted fridge:" + fridgeName);
                                                        ctx.getResponse().status(204).send();
                                                    }
                                                    catch(UnavailableException une){
                                                        LOGGER.warn("Empty name or fridge does not exist. Returning 404");
                                                        LOGGER.debug("Fridge db:: " + fridgeDb);
                                                        ctx.getResponse().status(404).send("Empty name or fridge already exists");
                                                    }
                                                }
                                        )


                                )

                )
                .path("fridges/:fridgename/:item",

                        ctx ->
                                ctx.byMethod(m -> m

                                        .get(() -> {
                                            final String fridgeName = ctx.getPathTokens().get("fridgename");
                                            final String item = ctx.getPathTokens().get("item");
                                            LOGGER.info(String.format("Getting item %s from fridge %s ",  fridgeName, item));
                                            try{
                                                ctx.render(Jackson.json(fridgeDb.getItemInFridge(fridgeName, item)));
                                            }
                                            catch(UnavailableException unAvEx){
                                                LOGGER.warn("Fridge or item does not exist. Returning 404");
                                                LOGGER.debug("Fridge db:: " + fridgeDb);
                                                ctx.getResponse().status( 404).send("Fridge/Item not found");
                                            }

//                                            if(fridgeDb.hasFridge(fridgeName)) {
//                                                ctx.render(Jackson.json(fridgeDb.getFridge(fridgeName).get()));
//                                            }
//                                            else{
//                                                LOGGER.warn("Fridge not found: " + fridgeName);
//                                                LOGGER.debug("Fridge db:: " + fridgeDb);
//                                                ctx.getResponse().status(404).send("Fridge not found");
//                                            }
                                        })


                                        .put(() -> {
                                                    final String item = ctx.getPathTokens().get("item");
                                                    final String fridgeName = ctx.getPathTokens().get("fridgename");
                                                    try{
                                                        fridgeDb.addItemtoFridge(fridgeName, item);
                                                        LOGGER.info(String.format("Added item %s to fridge %s ",item, fridgeName));
                                                        ctx.getResponse().status(201).send();
                                                    }
                                                    catch(CountExceededException cntEx){
                                                        LOGGER.warn("Count exceeded. Returning 409");
                                                        LOGGER.debug("Fridge db:: " + fridgeDb);
                                                        ctx.getResponse().status( 409).send("Exceeded limit");
                                                    }
                                                    catch(UnavailableException unAvEx){
                                                        LOGGER.warn("Fridge does not exist. Returning 404");
                                                        LOGGER.debug("Fridge db:: " + fridgeDb);
                                                        ctx.getResponse().status( 404).send("Fridge not found");
                                                    }
                                                }
                                        )
                                        .delete(() -> {
                                                    final String item = ctx.getPathTokens().get("item");
                                                    final String fridgeName = ctx.getPathTokens().get("fridgename");
                                                    try {
                                                        fridgeDb.removeItemInFridge(fridgeName, item);
                                                        LOGGER.info(String.format("Deleted item: %s in fridge %s", item, fridgeName ));
                                                        ctx.getResponse().status(204).send();
                                                    }
                                                    catch(UnavailableException uex){
                                                        LOGGER.warn("Fridge or item does not exist. Returning 404");
                                                        LOGGER.debug("Fridge db:: " + fridgeDb);
                                                        ctx.getResponse().status(404).send("Fridge or item does not exist");
                                                    }
                                                }
                                        )
                                )
                );


    }

    //Rudimentary database, try to upgrade H2 and JPA?
    private  static FridgeDB fridgeDb = new FridgeDB();
    private final static Logger LOGGER = LoggerFactory.getLogger(RatPackServer.class);

}
