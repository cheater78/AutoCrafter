package com.c78.autocrafter.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Objects;

public class AutoCrafterConstants {

    /**
     * the general Namespace of this plugin
     */
    public static final String NAMESPACE = "com.c78.autocrafter";

    /**
     * the namespace of the custom {@link com.c78.autocrafter.data.item.AutoCrafterItem}
     */
    public static final String ITEM_NAMESPACE = NAMESPACE + ".item";
    public static final NamespacedKey ITEM_NAMESPACED_KEY = Objects.requireNonNull(NamespacedKey.fromString(ITEM_NAMESPACE));

    /**
     * the namespace of the {@link org.bukkit.persistence.PersistentDataType},
     * {@link com.c78.autocrafter.data.instance.AutoCrafterUnit.UnitContainer}
     * an {@link com.c78.autocrafter.data.instance.AutoCrafterUnit} has
     */
    public static final String UNIT_NAMESPACE = NAMESPACE + ".unit";
    public static final NamespacedKey UNIT_NAMESPACED_KEY = Objects.requireNonNull(NamespacedKey.fromString(UNIT_NAMESPACE));

    /**
     * the InGame DisplayName of the {@link com.c78.autocrafter.data.instance.AutoCrafterUnit}
     */
    public static final Component ACU_DISPLAYNAME = Component.join(JoinConfiguration.builder().build(), List.of(
            Component.text("AutoCrafter", NamedTextColor.GOLD),
            Component.text(" "),
            Component.text("by c78", NamedTextColor.GRAY)
    ));

    /**
     * the InGame Title for the Configuration UI of an {@link com.c78.autocrafter.data.instance.AutoCrafterUnit}
     */
    public static final String ACU_CONFIG_UI_TITLE = "Configure AutoCrafter";


}
