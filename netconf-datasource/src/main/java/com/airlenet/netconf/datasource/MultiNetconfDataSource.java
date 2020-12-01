package com.airlenet.netconf.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class MultiNetconfDataSource extends NetconfDataSource {

    protected final Map<Object, NetconfDataSource> dataSourceObjectMap = new HashMap<>();
    public ReentrantLock lock = new ReentrantLock();

    public MultiNetconfDataSource() {
        this("", "", "");
    }

    private MultiNetconfDataSource(String url, String username, String password) {
        super(url, username, password);
    }

    @Override
    public final NetconfPooledConnection getConnection() throws NetconfException {
        throw new IllegalArgumentException("");
    }

    @Override
    public final NetconfPooledConnection getConnection(String username, String password) throws NetconfException {
        throw new IllegalArgumentException("");
    }

    //    @Override
    private final NetconfPooledConnection getConnectionDirect(long maxWaitMillis) throws NetconfException {
        throw new IllegalArgumentException("");
    }


    public NetconfDataSource getDataSource(String url, String username, String password) throws NetconfException {
        synchronized (dataSourceObjectMap) {
            NetconfDataSource netconfDataSource = dataSourceObjectMap.get(url);
            if (netconfDataSource == null) {
                netconfDataSource = new NetconfDataSource(url, username, password);
                netconfDataSource.setReadTimeout(this.getReadTimeout());
                netconfDataSource.setConnectionTimeout(this.getConnectionTimeout());
                netconfDataSource.setMaxPoolSize(this.getMaxPoolSize());
                netconfDataSource.setKexTimeout(this.getKexTimeout());
                netconfDataSource.setAutoReconnection(this.isAutoReconnection());

                netconfDataSource.setTestOption(this.getTestOption());
                netconfDataSource.setErrorOption(this.getErrorOption());
                netconfDataSource.setDefaultOperation(this.getDefaultOperation());
                dataSourceObjectMap.put(url, netconfDataSource);
            } else {
                if (!username.equals(netconfDataSource.getUsername()) || !password.equals(netconfDataSource.getPassword())) {
                    netconfDataSource.restart();//需要重置DataSource
                    netconfDataSource.setUsername(username);
                    netconfDataSource.setPassword(password);
                }
            }
            return netconfDataSource;
        }
    }


    public NetconfPooledConnection getConnection(String url, String username, String password) throws NetconfException {
        NetconfDataSource dataSource = getDataSource(url, username, password);
        return dataSource.getConnection();
    }


    public void removeDataSource(String url) throws NetconfException {
        synchronized (dataSourceObjectMap) {
            NetconfDataSource netconfDataSource = dataSourceObjectMap.get(url);
            if (netconfDataSource != null) {
                netconfDataSource.close();
            }
        }
    }


    public void addDataSource(String url, String username, String password) throws NetconfException {
        getDataSource(url, username, password);
    }


    public void removeDataSource(NetconfDataSource originalNetconfDataSource) throws NetconfException {
        synchronized (dataSourceObjectMap) {
            NetconfDataSource netconfDataSource = dataSourceObjectMap.get(originalNetconfDataSource.getUrl());
            if (netconfDataSource != null) {
                netconfDataSource.close();
            }
        }

    }


    public void addDataSource(NetconfDataSource originalNetconfDataSource) throws NetconfException {
        synchronized (dataSourceObjectMap) {
            NetconfDataSource netconfDataSource = dataSourceObjectMap.get(originalNetconfDataSource.getUrl());
            if (netconfDataSource == null) {
                dataSourceObjectMap.put(originalNetconfDataSource.getUrl(), originalNetconfDataSource);
            }
        }

    }
}
