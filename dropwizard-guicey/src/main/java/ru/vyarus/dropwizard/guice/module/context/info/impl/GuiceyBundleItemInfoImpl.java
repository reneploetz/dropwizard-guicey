package ru.vyarus.dropwizard.guice.module.context.info.impl;

import ru.vyarus.dropwizard.guice.module.context.ConfigItem;
import ru.vyarus.dropwizard.guice.module.context.ConfigScope;
import ru.vyarus.dropwizard.guice.module.context.info.GuiceyBundleItemInfo;
import ru.vyarus.dropwizard.guice.module.installer.bundle.GuiceyBundle;

/**
 * Bundle item info implementation.
 *
 * @author Vyacheslav Rusakov
 * @since 06.07.2016
 */
public class GuiceyBundleItemInfoImpl extends BundleItemInfoImpl<GuiceyBundle> implements GuiceyBundleItemInfo {

    private int initOrder;

    /**
     * Create disable-only bundle item (only disabled).
     *
     * @param type bundle type
     */
    public GuiceyBundleItemInfoImpl(final Class<? extends GuiceyBundle> type) {
        super(ConfigItem.Bundle, type);
    }

    /**
     * Create bundle info.
     *
     * @param bundle guicey bundle
     */
    public GuiceyBundleItemInfoImpl(final GuiceyBundle bundle) {
        super(ConfigItem.Bundle, bundle);
    }


    @Override
    public boolean isFromLookup() {
        return getRegisteredBy().contains(ConfigScope.BundleLookup.getKey());
    }

    @Override
    public boolean isTransitive() {
        return getRegisteredBy().stream()
                .noneMatch(type -> ConfigScope.recognize(type) != ConfigScope.GuiceyBundle);
    }

    @Override
    public boolean isDropwizard() {
        return false;
    }

    @Override
    public int getInitOrder() {
        return initOrder;
    }

    /**
     * @param initOrder initialization order
     */
    public void setInitOrder(final int initOrder) {
        this.initOrder = initOrder;
    }
}
