package org.cache.code.ui;

import java.util.Set;
import javax.swing.DefaultListModel;
import org.cache.code.CachedFileRefreshIFC;
import org.cache.code.MyCacheServer;
import org.cache.code.FileCache;
import org.cache.code.BlockCache;

public class CachedFileRefresh implements CachedFileRefreshIFC {
    private DefaultListModel cachedFileListModel;

    public CachedFileRefresh(DefaultListModel model) {
        cachedFileListModel = model;
    }
    
    public void refresh() {
        cachedFileListModel.clear();
        Set<String> cachedFileSet = null;
        if(MyCacheServer.getCachePolicy() == MyCacheServer.POLICY_FILE) {
            cachedFileSet = FileCache.getCachedFileSet();
            
        }
        else if(MyCacheServer.getCachePolicy() == MyCacheServer.POLICY_BLOCK) {
            cachedFileSet = BlockCache.getCachedFileSet();
        }

        for(String fileName: cachedFileSet) {
            cachedFileListModel.addElement(fileName);
        }
    }
}
