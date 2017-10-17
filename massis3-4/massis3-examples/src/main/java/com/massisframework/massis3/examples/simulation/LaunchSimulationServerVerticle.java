package com.massisframework.massis3.examples.simulation;

import com.massisframework.massis3.core.config.HttpServerConfig;
import com.massisframework.massis3.core.config.SimulationServerConfig;
import com.massisframework.massis3.services.eventbus.Massis3ServiceUtils;
import com.massisframework.massis3.services.eventbus.SimulationServerService;
import com.massisframework.massis3.services.eventbus.sim.EnvironmentService;
import com.massisframework.massis3.simulation.server.SimulationServerLauncher;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by mcardenas on 16/10/17.
 */
public class LaunchSimulationServerVerticle extends AbstractVerticle {

    //variable log
    private static final Logger log = LoggerFactory.getLogger(VideoStreamingOutputExampleVerticle.class);

    static {
        Massis3ServiceUtils.configureVertxJSONMapper();
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        /* 1. Deploy simulation server verticle cfg=JsonObject.mapFrom(this.config());*/

        SimulationServerConfig cfg = new SimulationServerConfig()
                .withAssetFolders(Arrays.asList("/home/mcardenas/git-projects/massis3-assets/Scenes",
                        "/home/mcardenas/git-projects/massis3-assets/models",
                        "/home/mcardenas/git-projects/massis3-assets/animations"))
                .withHttpServerConfig(new HttpServerConfig().withHost("127.0.0.1")
                        .withPort(8082))
                .withAuthPropertiesFile("classpath:webauth.properties")
                .withInstances(1)
                .withRendererType(SimulationServerConfig.RendererType.LWJGL_OPEN_GL_3)
                .withRenderMode(SimulationServerConfig.RenderMode.SERVER);

        SimulationServerLauncher.launch(this.vertx, cfg, ar -> {
            if (ar.failed()) startFuture.fail(ar.cause());
            else {
                /* Creamos una simulación de prueba */
                createSim("Faculty_1floor")
                        .compose(simId -> createCamera(simId))
                        .map((Void) null)
                        .setHandler(startFuture.completer());
            }
        });
    }

    private Future<String> createCamera(Long simId) {
        Future<String> cameraIdFut = Future.future();
        EnvironmentService es = Massis3ServiceUtils.createProxy(vertx, EnvironmentService.class, simId);
        es.addCamera(cameraIdFut.completer());
        return cameraIdFut;
    }

    private Future<Long> createSim(String sceneFile) {
        SimulationServerService proxy = Massis3ServiceUtils.createProxy(vertx, SimulationServerService.class, Massis3ServiceUtils.GLOBAL_SERVICE_GROUP);
        Future<Long> simCreateFuture = Future.future();
        proxy.create(sceneFile, simCreateFuture.completer());
        return simCreateFuture;
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }

    public static void main(String[] args) throws IOException {
        Vertx vertx = Vertx.vertx();

        // String cfgPath = "configurations/rpax-local-config.json";
        // JsonObject config = new JsonObject(new
        // String(Files.readAllBytes(Paths.get(cfgPath))));

        vertx.deployVerticle(VideoStreamingOutputExampleVerticle.class.getName(),
                new DeploymentOptions() /* .setConfig(config) */, r -> {
                    if (r.failed()) {
                        r.cause().printStackTrace();
                        log.error("Error when launching verticle", r.cause());
                    } else {

                        if (log.isInfoEnabled()) {
                            log.info("Verticle launched successfully");
                        }

                    }
                });

    }
}
