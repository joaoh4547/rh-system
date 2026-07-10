package com.rhsystem.interfaces.ui.pages.cache;

import com.rhsystem.application.dto.cache.CacheInfo;
import com.rhsystem.interfaces.ui.shared.ActionsGrid;
import com.rhsystem.interfaces.ui.shared.ObjectAction;

import java.util.Collection;

/**
 * Grid that lists the distributed cache regions with their entry count and
 * approximate memory footprint, plus a per-row "clear" action.
 */
public class CacheGrid extends ActionsGrid<CacheInfo> {

    public CacheGrid(Collection<ObjectAction<CacheInfo>> actions) {
        super(actions);
    }

    @Override
    protected void configColumns() {
        addColumn("name", CacheInfo::name)
                .setHeader(getTranslation("page.cache.col.name"))
                .setAutoWidth(true);
        addColumn("entryCount", CacheInfo::entryCount)
                .setHeader(getTranslation("page.cache.col.entries"))
                .setAutoWidth(true);
        addColumn("memoryBytes", info -> ByteFormat.humanReadable(info.memoryBytes()))
                .setHeader(getTranslation("page.cache.col.memory"))
                .setAutoWidth(true);
    }
}
