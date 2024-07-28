package me.lorenzo0111.rocketbans.bukkit.gui.menus;

import me.lorenzo0111.rocketbans.bukkit.gui.items.NavigationItem;
import me.lorenzo0111.rocketbans.bukkit.RocketBans;
import me.lorenzo0111.rocketbans.api.RocketBansProvider;
import me.lorenzo0111.rocketbans.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseMenu {
    protected final RocketBans plugin = (RocketBans) RocketBansProvider.get();
    private final String id;
    private final Map<Character, Item> items = new HashMap<>();
    private final List<Item> content = new ArrayList<>();
    private final boolean paginated;

    @SuppressWarnings("ConstantConditions")
    public BaseMenu(String id, boolean paginated) {
        this.id = id;
        this.paginated = paginated;

        ConfigurationSection custom = plugin.getConfig().getConfigurationSection("menus.items.custom");
        for (String key : custom.getKeys(false)) {
            ConfigurationSection section = custom.getConfigurationSection(key);
            if (section == null) continue;

            ItemBuilder builder = new ItemBuilder(Material.getMaterial(section.getString("type", "BARRIER")));
            builder.setDisplayName(StringUtils.color(section.getString("name")));
            builder.setLegacyLore(StringUtils.color(section.getStringList("lore")));

            this.setItem(key.charAt(0), new SimpleItem(builder));
        }
    }

    @SuppressWarnings("unchecked")
    public Gui build() {
        Gui.Builder<?, ?> builder;

        if (paginated) {
            builder = PagedGui.items();
        } else {
            builder = Gui.normal();
        }

        builder.setStructure(
                        plugin.getConfig()
                                .getStringList("menus." + id + ".structure")
                                .toArray(new String[0])
                )
                .addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('<', new NavigationItem(false))
                .addIngredient('>', new NavigationItem(true));

        if (paginated) {
            ((PagedGui.Builder<Item>) builder).setContent(content);
        }

        for (Map.Entry<Character, Item> entry : items.entrySet()) {
            builder.addIngredient(entry.getKey(), entry.getValue());
        }

        return builder.build();
    }

    public void setItem(char character, Item item) {
        items.put(character, item);
    }

    public void addItem(Item item) {
        content.add(item);
    }

    public void open(Player player) {
        Window window = Window.single()
                .setViewer(player)
                .setTitle(plugin.getMessage("menus." + id + ".title", false))
                .setGui(this.build())
                .build();

        if (!Bukkit.isPrimaryThread()) Bukkit.getScheduler().runTask(plugin, window::open);
        else window.open();
    }
}
