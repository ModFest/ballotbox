package net.modfest.ballotbox.mixin.client;

import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpruceOptionListWidget.OptionEntry.class)
public interface OptionEntryAccessor {

    @Invoker("<init>")
    static SpruceOptionListWidget.OptionEntry ballotbox$create(SpruceOptionListWidget parent) {
        return null;
    }
}
