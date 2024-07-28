package me.lorenzo0111.rocketbans.bukkit.gui.items;

import me.lorenzo0111.rocketbans.bukkit.RocketBans;
import me.lorenzo0111.rocketbans.api.RocketBansProvider;
import org.bukkit.Material;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.builder.SkullBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ConfiguredItem extends AbstractItem {
    protected final RocketBans plugin = (RocketBans) RocketBansProvider.get();
    private final String id;

    public ConfiguredItem(String id) {
        this.id = id;
    }

    public ItemBuilder overrideBase() {
        return null;
    }

    @Override
    public ItemProvider getItemProvider() {
        AbstractItemBuilder<?> builder = overrideBase();

        if (builder == null) {
            Material type = Material.getMaterial(plugin.getConfiguration().node("menus", "items", this.id, "type").getString("BARRIER"));
            if (type == null) type = Material.BARRIER;

            if (type.equals(Material.PLAYER_HEAD)) {
                builder = new SkullBuilder(new SkullBuilder.HeadTexture(plugin.getConfiguration().node("menus", "items", this.id, "texture").getString("")));
            } else {
                builder = new ItemBuilder(type);
            }
        }

        if (!plugin.getConfiguration().node("menus", "items", this.id, "name").virtual()) {
            builder.setDisplayName(replacePlaceholders(plugin.getMessage("menus.items." + this.id + ".name", false)));
        }

        if (!plugin.getConfiguration().node("menus", "items", this.id, "lore").virtual()) {
            List<String> lore = plugin.getMessages("menus.items." + this.id + ".lore", false)
                    .stream()
                    .map(this::replacePlaceholders)
                    .collect(Collectors.toList());

            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("<line>")) {
                    String[] split = lore.get(i).split("<line>");
                    lore.remove(i);
                    for (String s : split) {
                        lore.add(i, s);
                        i++;
                    }
                }
            }

            builder.setLegacyLore(lore);
        }

        return builder;
    }

    public String replacePlaceholders(String origin) {
        return origin;
    }

}
