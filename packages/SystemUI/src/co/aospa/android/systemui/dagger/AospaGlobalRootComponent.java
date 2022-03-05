package co.aospa.android.systemui.dagger;

import android.content.Context;

import com.android.systemui.dagger.GlobalModule;
import com.android.systemui.dagger.GlobalRootComponent;
import com.android.systemui.dagger.WMModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {
        GlobalModule.class,
        AospaSysUISubcomponentModule.class,
        WMModule.class})
public interface AospaGlobalRootComponent extends GlobalRootComponent {

    @Component.Builder
    interface Builder extends GlobalRootComponent.Builder {
        AospaGlobalRootComponent build();
    }

    @Override
    AospaSysUIComponent.Builder getSysUIComponent();
}
