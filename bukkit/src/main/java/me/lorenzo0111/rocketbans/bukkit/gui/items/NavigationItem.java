package me.lorenzo0111.rocketbans.bukkit.gui.items;

import me.lorenzo0111.rocketbans.bukkit.RocketBans;
import me.lorenzo0111.rocketbans.api.RocketBansProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.builder.SkullBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

import java.util.stream.Collectors;

public class NavigationItem extends ControlItem<PagedGui<?>> {
    private final boolean forward;

    public NavigationItem(boolean forward) {
        this.forward = forward;
    }

    @Override
    public ItemProvider getItemProvider(PagedGui<?> gui) {
        RocketBans plugin = (RocketBans) RocketBansProvider.get();
        AbstractItemBuilder<?> builder;

        Material type = Material.getMaterial(plugin.getConfig().getString("menus.items." + (forward ? "next" : "previous") + ".type", "BARRIER"));
        if (type == null)
            type = Material.BARRIER;

        if (type.equals(Material.PLAYER_HEAD)) {
            builder = new SkullBuilder(new SkullBuilder.HeadTexture(plugin.getConfig().getString("menus.items." + (forward ? "next" : "previous") + ".texture", "")));
        } else {
            builder = new ItemBuilder(type);
        }

        builder.setDisplayName(
                replacePlaceholders(
                        plugin.getMessage("menus.items." + (forward ? "next" : "previous") + ".name", false)
                )
        );

        builder.setLegacyLore(
                plugin.getMessages(
                                "menus.items." + (forward ? "next" : "previous") + ".lore." + (
                                        ((forward && gui.hasNextPage()) || (!forward && gui.hasPreviousPage())) ? "has" : "has_not"),
                                false
                        ).stream()
                        .map(this::replacePlaceholders)
                        .collect(Collectors.toList())
        );

        return builder;
    }

    private String replacePlaceholders(String origin) {
        return origin.replace("%page%", String.valueOf(getGui().getCurrentPage() + 1))
                .replace("%pages%", String.valueOf(getGui().getPageAmount()));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickType == ClickType.LEFT) {
            if (forward) {
                getGui().goForward();
            } else {
                getGui().goBack();
            }
        } else if (clickType == ClickType.SHIFT_LEFT) {
            if (forward) {
                getGui().setPage(getGui().getPageAmount());
            } else {
                getGui().setPage(0);
            }
        }
    }

}
