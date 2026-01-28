//package ru.hope_zv.mod.impl.commands.impl;
//
//import com.hypixel.hytale.server.core.Message;
//import com.hypixel.hytale.server.core.command.system.CommandContext;
//import com.hypixel.hytale.server.core.command.system.CommandSender;
//import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
//import com.hypixel.hytale.server.core.entity.entities.Player;
//import ru.hope_zv.mod.LenseConfig;
//import ru.hope_zv.mod.api.command.CommandProvider;
//import ru.hope_zv.mod.impl.config.LensePlayerConfig;
//
//import javax.annotation.Nonnull;
//import java.util.Locale;
//
//public class ScaleCommand extends CommandBase implements CommandProvider {
//
//    private final LenseCommand parent;
//
//    public ScaleCommand(LenseCommand parent) {
//        super("scale", "Set Lense HUD scale");
//        this.parent = parent;
//        this.setAllowsExtraArguments(true);
//        this.addAliases("s");
//    }
//
//    private static String formatScale(float value) {
//        String s = String.format(Locale.ROOT, "%.2f", value);
//        return s.replaceAll("\\.?0+$", "");
//    }
//
//    @Override
//    protected void executeSync(@Nonnull CommandContext ctx) {
//        CommandSender sender = ctx.sender();
//
//        if (!(sender instanceof Player player)) {
//            ctx.sendMessage(LenseCommand.prefixed(
//                    Message.translation("lense.command.scale.exec_err_only_by_player").color(COLOR_OFF)
//            ));
//            return;
//        }
//
//        LensePlayerConfig config = parent.getConfigService().getConfig(player.getPlayerRef());
//
//        String[] args = ctx.getInputString().trim().split("\\s+");
//        String arg = args.length >= 3 ? args[2] : null;
//
//        if (arg == null || arg.isBlank()) {
//            Message msg = Message.translation("lense.command.scale.exec_current_scale")
//                    .param("value", Message.raw(formatScale(config.getHudScale())).bold(true).color(COLOR_ACCENT));
//            ctx.sendMessage(LenseCommand.prefixed(msg));
//            return;
//        }
//
//        String normalized = arg.trim().toLowerCase().replace(',', '.');
//
//        final float value;
//        try {
//            value = Float.parseFloat(normalized);
//        } catch (NumberFormatException e) {
//            Message msg = Message.empty()
//                    .insert(Message.translation("lense.command.scale.exec_err_invalid_number").color(COLOR_WARN))
//                    .insert(" ")
//                    .insert(Message.translation("lense.command.scale.exec_err_usage")
//                            .param("cmd", Message.raw("/lense scale <value>").bold(true).color(COLOR_ACCENT)));
//
//            ctx.sendMessage(LenseCommand.prefixed(msg));
//            return;
//        }
//
//        if (Float.isNaN(value) || Float.isInfinite(value)) {
//            ctx.sendMessage(LenseCommand.prefixed(
//                    Message.translation("lense.command.scale.exec_err_invalid_number").color(COLOR_WARN)
//            ));
//            return;
//        }
//
//        if (value < LenseConfig.HUD_SCALE_MIN || value > LenseConfig.HUD_SCALE_MAX) {
//            Message msg = Message.translation("lense.command.scale.exec_err_out_of_range")
//                    .param("min", Message.raw(formatScale(LenseConfig.HUD_SCALE_MIN)).bold(true).color(COLOR_ACCENT))
//                    .param("max", Message.raw(formatScale(LenseConfig.HUD_SCALE_MAX)).bold(true).color(COLOR_ACCENT));
//
//            ctx.sendMessage(LenseCommand.prefixed(msg.color(COLOR_WARN)));
//            return;
//        }
//
//        config.setHudScale(value);
//        parent.getConfigService().markDirty();
//
//        Message msg = Message.translation("lense.command.scale.exec_scale_is_now")
//                .param("value", Message.raw(formatScale(value)).bold(true).color(COLOR_ACCENT));
//        ctx.sendMessage(LenseCommand.prefixed(msg));
//    }
//
//}
