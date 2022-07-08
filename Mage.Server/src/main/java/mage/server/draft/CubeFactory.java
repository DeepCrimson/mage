package mage.server.draft;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author LevelX2
 */
public enum CubeFactory {

    instance;

    private final Map<String, Class> draftCubes = new LinkedHashMap<>();

    public Set<String> getDraftCubes() {
        return draftCubes.keySet();
    }

    public void addDraftCube(String name, Class draftCube) {
        if (draftCube != null) {
            this.draftCubes.put(name, draftCube);
        }
    }

}
