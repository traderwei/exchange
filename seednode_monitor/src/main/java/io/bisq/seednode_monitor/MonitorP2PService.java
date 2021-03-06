/*
 * This file is part of bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.seednode_monitor;

import io.bisq.common.app.Log;
import io.bisq.common.proto.persistable.PersistedDataHost;
import io.bisq.network.Socks5ProxyProvider;
import io.bisq.network.p2p.network.NetworkNode;
import io.bisq.network.p2p.network.SetupListener;
import io.bisq.network.p2p.storage.P2PDataStorage;
import io.bisq.seednode_monitor.metrics.MonitorRequestManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
public class MonitorP2PService implements SetupListener, PersistedDataHost {
    private final NetworkNode networkNode;
    @Getter
    private final P2PDataStorage p2PDataStorage;
    private final MonitorRequestManager requestDataManager;
    private final Socks5ProxyProvider socks5ProxyProvider;

    private volatile boolean shutDownInProgress;
    private boolean shutDownComplete;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @com.google.inject.Inject
    public MonitorP2PService(NetworkNode networkNode,
                             P2PDataStorage p2PDataStorage,
                             MonitorRequestManager requestDataManager,
                             Socks5ProxyProvider socks5ProxyProvider) {
        this.networkNode = networkNode;
        this.p2PDataStorage = p2PDataStorage;
        this.requestDataManager = requestDataManager;
        this.socks5ProxyProvider = socks5ProxyProvider;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void readPersisted() {
        p2PDataStorage.readPersisted();
    }

    public void start() {
        networkNode.start(this);
    }

    public void shutDown(Runnable shutDownCompleteHandler) {
        Log.traceCall();
        if (!shutDownInProgress) {
            shutDownInProgress = true;

            if (requestDataManager != null) {
                requestDataManager.shutDown();
            }

            if (networkNode != null) {
                networkNode.shutDown(() -> {
                    shutDownComplete = true;
                });
            } else {
                shutDownComplete = true;
            }
        } else {
            log.debug("shutDown already in progress");
            if (shutDownComplete) {
                shutDownCompleteHandler.run();
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // SetupListener implementation
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onTorNodeReady() {
        socks5ProxyProvider.setSocks5ProxyInternal(networkNode.getSocksProxy());
    }

    @Override
    public void onHiddenServicePublished() {
        checkArgument(networkNode.getNodeAddress() != null, "Address must be set when we have the hidden service ready");
        requestDataManager.start();
    }

    @Override
    public void onSetupFailed(Throwable throwable) {
    }

    @Override
    public void onRequestCustomBridges() {
    }
}
