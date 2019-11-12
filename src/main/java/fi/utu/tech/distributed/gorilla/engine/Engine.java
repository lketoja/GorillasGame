package fi.utu.tech.distributed.gorilla.engine;

import java.util.Collection;

public interface Engine {
    void init();

    void bindObject(ProxyGameObject g, boolean movable);

    void unbind(ProxyGameObject g);

    Collection<ProxyGameObject> objectsInRegion(Region region);

    void run();

    GameObject getEngineObject(ProxyGameObject g);

    void updateObject(ProxyGameObject g);

    double currentTimeStamp();

    double timeStep();

    default void runUntil(double timeStamp) {
        while (currentTimeStamp() < timeStamp)
            run();
    }
}