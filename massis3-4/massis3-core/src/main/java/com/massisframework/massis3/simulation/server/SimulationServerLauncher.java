package com.massisframework.massis3.simulation.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.SimpleApplication;
import com.jme3.system.JmeContext.Type;
import com.massisframework.massis3.commons.app.server.ServerJMEApplication;
import com.massisframework.massis3.commons.app.server.impl.multi.MultiCameraApp;
import com.massisframework.massis3.commons.loader.LocalMassisSceneLoader;
import com.massisframework.massis3.core.config.HttpServerConfig;
import com.massisframework.massis3.core.config.SimulationServerConfig;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class SimulationServerLauncher {

    private static final Logger log = LoggerFactory.getLogger(SimulationServerLauncher.class);

    public static void main(String[] args) throws IOException {
//		if (args.length == 0)
//		{
//			log.error("Configuration file is required, load default configuration");
//			System.exit(-1);
//		}
        String path = "/home/mcardenas/git-projects/massis3-4/massis3-examples/configurations/local-config.json";
        launch(Paths.get(path));
    }

    public static void launch(Path configFile) throws IOException {

        JsonObject cfg = new JsonObject(new String(Files.readAllBytes(configFile)));
        launch(Vertx.vertx(), new SimulationServerConfig(cfg), r -> {
            if (r.failed()) {
                log.error("Error when launching server", r.cause());
                System.exit(-1);
            } else {

                if (log.isInfoEnabled()) {
                    log.info("Server launched succesfully");
                }
            }
        });
    }

    public static Future<Void> launchSimpleApplicationTest() {
        Future<Void> testFuture = Future.future();
        MultiCameraApp.launch(false, "Graphics test", ar -> {
            if (ar.succeeded()) {
                if (log.isInfoEnabled()) {
                    log.info("Test application launched and succeed. Shutting it down");
                }
                ar.result().stop(true);
                testFuture.complete();
            } else {
                testFuture.fail(ar.cause());
            }
        });
        return testFuture;
    }

    public static synchronized void compile(SimulationServerConfig sscfg) throws IOException {

        List<String> assetFolders = sscfg.getAssetFolders();

        java.util.logging.Logger.getLogger(
                com.massisframework.massis3.commons.spatials.FastLodGenerator.class.getName())
                .setLevel(Level.OFF);
        java.util.logging.Logger
                .getLogger(
                        com.massisframework.massis3.commons.loader.GroupedObjLoader.class.getName())
                .setLevel(Level.OFF);
        java.util.logging.Logger.getLogger(com.jme3.util.TangentBinormalGenerator.class.getName())
                .setLevel(Level.OFF);

        System.setProperty(ServerJMEApplication.RUN_MODE_KEY, sscfg.getRenderMode().value());

        assetFolders
                .stream()
                .map(Paths::get)
                .flatMap(root -> walkFiles(root)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".sh3d"))
                        .map(path -> root.relativize(path))
                        .map(path -> path.toString()))
                .distinct()
                .parallel()
                .forEach(s -> new LocalMassisSceneLoader(assetFolders, s));

    }

    private static Stream<Path> walkFiles(Path path) {
        try {
            return Files.walk(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void launch(Vertx vertx, JsonObject cfg,
                              Handler<AsyncResult<String>> resultHandler)

    {
        launch(vertx, new SimulationServerConfig(cfg), resultHandler);

    }

    public static void launch(Vertx vertx, SimulationServerConfig cfg,
                              Handler<AsyncResult<String>> resultHandler) {
        launchSimpleApplicationTest().compose(_void -> {
            Future<String> f = Future.future();
            try {
                compile(cfg);
            } catch (IOException e) {
                f.fail(e);
                return f;
            }
            DeploymentOptions options = new DeploymentOptions().setConfig(cfg.toJson());
            options.setInstances(cfg.getInstances());
            vertx.deployVerticle(SimulationServerVerticle.class.getName(), options, f.completer());
            return f;
        }).setHandler(resultHandler);

    }

}
