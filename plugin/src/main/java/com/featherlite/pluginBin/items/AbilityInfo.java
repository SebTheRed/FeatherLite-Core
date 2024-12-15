package com.featherlite.pluginBin.items;

import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.Map;

public class AbilityInfo {
    private final Object abilitySource;  // Can be either Plugin or InternalAbilities
    private final String methodName;
    private final Map<String, String> defaultParams;

    public AbilityInfo(Object abilitySource, String methodName, Map<String, String> defaultParams) {
        this.abilitySource = abilitySource;
        this.methodName = methodName;
        this.defaultParams = defaultParams != null ? defaultParams : Collections.emptyMap();
    }

    public Object getAbilitySource() {
        return abilitySource;
    }

    public String getMethodName() {
        return methodName;
    }

    public Map<String, String> getDefaultParams() {
        return defaultParams;
    }

    /**
     * Returns the name of the plugin or "Internal" if it’s an internal ability.
     */
    public String getSourceName() {
        if (abilitySource instanceof Plugin) {
            return ((Plugin) abilitySource).getName();
        } else {
            return "Internal";
        }
    }

    @Override
    public String toString() {
        return "AbilityInfo{" +
                "source=" + getSourceName() +
                ", methodName='" + methodName + '\'' +
                ", defaultParams=" + defaultParams +
                '}';
    }
}
