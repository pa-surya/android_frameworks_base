package co.aospa.android.systemui.dagger;

import com.android.systemui.dagger.DefaultComponentBinder;
import com.android.systemui.dagger.DependencyProvider;
import com.android.systemui.dagger.SysUISingleton;
import com.android.systemui.dagger.SystemUIBinder;
import com.android.systemui.dagger.SysUIComponent;
import com.android.systemui.dagger.SystemUIModule;

import co.aospa.android.systemui.columbus.ColumbusModule;
import co.aospa.android.systemui.elmyra.ElmyraModule;
import co.aospa.android.systemui.keyguard.AospaKeyguardSliceProvider;
import co.aospa.android.systemui.smartspace.KeyguardSmartspaceController;

import dagger.Subcomponent;

@SysUISingleton
@Subcomponent(modules = {
        ColumbusModule.class,
        DefaultComponentBinder.class,
        DependencyProvider.class,
        ElmyraModule.class,
        AospaSystemUIBinder.class,
        SystemUIModule.class,
        AospaSystemUIModule.class})
public interface AospaSysUIComponent extends SysUIComponent {
    @SysUISingleton
    @Subcomponent.Builder
    interface Builder extends SysUIComponent.Builder {
        AospaSysUIComponent build();
    }

    /**
     * Member injection into the supplied argument.
     */
    void inject(AospaKeyguardSliceProvider keyguardSliceProvider);

    @SysUISingleton
    KeyguardSmartspaceController createKeyguardSmartspaceController();
}
