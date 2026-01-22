package ru.hope_zv.mod.api.content;

import ru.hope_zv.mod.api.context.Context;
import ru.hope_zv.mod.api.DeferredUICommandBuilder;

import java.awt.*;

public interface ContentProvider<CTX extends Context> {
    Color MOD_NAME_COLOR = new Color(85, 85, 255);
    Color DESC_COLOR = new Color(170, 170, 170);

    void updateContent(CTX context, DeferredUICommandBuilder deferredBuilder);
}
