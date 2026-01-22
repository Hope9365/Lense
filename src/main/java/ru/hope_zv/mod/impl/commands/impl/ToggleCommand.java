package ru.hope_zv.mod.impl.commands.impl;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import ru.hope_zv.mod.api.command.CommandProvider;
import ru.hope_zv.mod.impl.config.LensePlayerConfig;

import javax.annotation.Nonnull;

public class ToggleCommand extends CommandBase implements CommandProvider {

    private final LenseCommand parent;

    public ToggleCommand(LenseCommand parent) {
        super("toggle", "Toggle Lense HUD on/off");
        this.parent = parent;
        this.setAllowsExtraArguments(true);
        this.addAliases("t");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        CommandSender sender = ctx.sender();

        if (!(sender instanceof Player player)) {
            ctx.sendMessage(LenseCommand.prefixed(
                    Message.translation("server.lense.command.toggle.exec_err_only_by_player").color(COLOR_OFF)
            ));
            return;
        }

        LensePlayerConfig config = parent.getConfigService().getConfig(player.getPlayerRef());

        String[] args = ctx.getInputString().trim().split("\\s+");
        String mode = args.length >= 3 ? args[2] : null;
        boolean value;

        if (mode == null || mode.isBlank()) {
            value = !config.isHudEnabled();
            config.setHudEnabled(value);
            parent.getConfigService().markDirty();
        } else {
            switch (mode.trim().toLowerCase()) {
                case "on", "true", "1", "yes" -> {
                    value = true;
                    config.setHudEnabled(true);
                    parent.getConfigService().markDirty();
                }
                case "off", "false", "0", "no" -> {
                    value = false;
                    config.setHudEnabled(false);
                    parent.getConfigService().markDirty();
                }
                default -> {
                    Message msg = Message.empty()
                            .insert(Message.translation("server.lense.command.toggle.exec_err_invalid_opt").color(COLOR_WARN))
                            .insert(" ")
                            .insert(Message.translation("server.lense.command.toggle.exec_err_leave_empty")
                                    .param("on", Message.raw("On").bold(true).color(COLOR_ON))
                                    .param("off", Message.raw("Off").bold(true).color(COLOR_OFF)));

                    ctx.sendMessage(LenseCommand.prefixed(msg));
                    return;
                }
            }
        }

        Message msg = Message.empty()
                .insert(Message.translation("server.lense.command.toggle.exec_hud_is_now")
                        .param("value", Message.raw(value ? "On" : "Off").bold(true).color(value ? COLOR_ON : COLOR_OFF))); // inline param formatting not working :(
        ctx.sendMessage(LenseCommand.prefixed(msg));
    }

}
