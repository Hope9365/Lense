package ru.hope_zv.mod.api;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DeferredUICommandBuilder {

    @Getter
    private final List<Consumer<UICommandBuilder>> operations = new ArrayList<>();

    public DeferredUICommandBuilder() {
    }

    public static DeferredUICommandBuilder create() {
        return new DeferredUICommandBuilder();
    }

    public DeferredUICommandBuilder clear(String selector) {
        operations.add(builder -> builder.clear(selector));
        return this;
    }

    public DeferredUICommandBuilder remove(String selector) {
        operations.add(builder -> builder.remove(selector));
        return this;
    }

    public DeferredUICommandBuilder append(String documentPath) {
        operations.add(builder -> builder.append(documentPath));
        return this;
    }

    public DeferredUICommandBuilder append(String selector, String documentPath) {
        operations.add(builder -> builder.append(selector, documentPath));
        return this;
    }

    public DeferredUICommandBuilder appendInline(String selector, String document) {
        operations.add(builder -> builder.appendInline(selector, document));
        return this;
    }

    public DeferredUICommandBuilder insertBefore(String selector, String documentPath) {
        operations.add(builder -> builder.insertBefore(selector, documentPath));
        return this;
    }

    public DeferredUICommandBuilder insertBeforeInline(String selector, String document) {
        operations.add(builder -> builder.insertBeforeInline(selector, document));
        return this;
    }

    public DeferredUICommandBuilder setNull(String selector) {
        operations.add(builder -> builder.setNull(selector));
        return this;
    }

    public DeferredUICommandBuilder set(String selector, String str) {
        operations.add(builder -> builder.set(selector, str));
        return this;
    }

    public DeferredUICommandBuilder set(String selector, boolean b) {
        operations.add(builder -> builder.set(selector, b));
        return this;
    }

    public DeferredUICommandBuilder set(String selector, int n) {
        operations.add(builder -> builder.set(selector, n));
        return this;
    }

    public DeferredUICommandBuilder set(String selector, float n) {
        operations.add(builder -> builder.set(selector, n));
        return this;
    }

    public DeferredUICommandBuilder set(String selector, double n) {
        operations.add(builder -> builder.set(selector, n));
        return this;
    }

    public DeferredUICommandBuilder set(String selector, Message message) {
        operations.add(builder -> builder.set(selector, message));
        return this;
    }

    public <T> DeferredUICommandBuilder set(String selector, Value<T> ref) {
        operations.add(builder -> builder.set(selector, ref));
        return this;
    }

    public DeferredUICommandBuilder setObject(String selector, Object data) {
        operations.add(builder -> builder.setObject(selector, data));
        return this;
    }

    public <T> DeferredUICommandBuilder set(String selector, T[] data) {
        operations.add(builder -> builder.set(selector, data));
        return this;
    }

    public <T> DeferredUICommandBuilder set(String selector, List<T> data) {
        operations.add(builder -> builder.set(selector, data));
        return this;
    }

    public DeferredUICommandBuilder custom(Consumer<UICommandBuilder> operation) {
        operations.add(operation);
        return this;
    }

    public UICommandBuilder applyTo(UICommandBuilder builder) {
        for (Consumer<UICommandBuilder> operation : operations) {
            operation.accept(builder);
        }
        return builder;
    }

    public UICommandBuilder buildNew() {
        return applyTo(new UICommandBuilder());
    }

    public DeferredUICommandBuilder reset() {
        operations.clear();
        return this;
    }
    
}
