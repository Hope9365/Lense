package ru.hope_zv.mod.impl.commands.impl;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import lombok.Getter;
import ru.hope_zv.mod.api.command.CommandProvider;
import ru.hope_zv.mod.impl.config.LensePlayerConfigService;

import javax.annotation.Nonnull;

public class LenseCommand extends CommandBase implements CommandProvider {

    @Getter
    private final LensePlayerConfigService configService;

    public LenseCommand(LensePlayerConfigService configService) {
        super("lense", "Manage Lense settings");
        this.configService = configService;
        this.addAliases("l");
        this.setAllowsExtraArguments(true);

        this.addSubCommand(new ToggleCommand(this));
//        this.addSubCommand(new ScaleCommand(this));
    }

    public static Message prefixed(@Nonnull Message body) {
        return Message.raw("")
                .insert(Message.raw("(Lense) ").bold(true).color(COLOR_PREFIX))
                .insert(body);
    }

    private static void showHelp(@Nonnull CommandContext ctx) {
        Message msg = Message.empty()
                .insert(Message.translation("lense.command.lense.desc_available_commands").bold(true))
                .insert("\n")
                .insert(Message.raw("/lense toggle ").color(COLOR_ACCENT))
                .insert(Message.translation("lense.command.lense.desc_toggle_hud"))
                .insert("\n")
                .insert(Message.raw("/lense toggle on ").color(COLOR_ACCENT))
                .insert(Message.translation("lense.command.lense.desc_toggle_hud_on"))
                .insert("\n")
                .insert(Message.raw("/lense toggle off ").color(COLOR_ACCENT))
                .insert(Message.translation("lense.command.lense.desc_toggle_hud_off"))
                .insert("\n")
                .insert(Message.raw("/lense scale ").color(COLOR_ACCENT))
                .insert(Message.translation("lense.command.lense.desc_scale_hud"))
                .insert("\n")
                .insert(Message.raw("/lense scale <value> ").color(COLOR_ACCENT))
                .insert(Message.translation("lense.command.lense.desc_scale_hud_set"));

        ctx.sendMessage(prefixed(msg));
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        showHelp(ctx);
    }

}
