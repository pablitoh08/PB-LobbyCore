package org.pablito.pBLobbyCore.commands;

import org.pablito.pBLobbyCore.PBLobbyCore;
import org.pablito.pBLobbyCore.utils.MessageManager;

public class ReloadManager {

    private final PBLobbyCore plugin;
    private final MessageManager messageManager;

    public ReloadManager(PBLobbyCore plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    public void reloadPlugin() {
        plugin.reloadPluginConfigs();
    }
}