package com.airlenet.netconf.datasource;

import com.airlenet.netconf.datasource.util.Utils;
import com.tailf.jnc.Element;
import com.tailf.jnc.NodeSet;
import com.tailf.jnc.YangNsPackage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class NetconfPooledConnection extends NetconfConnection implements AutoCloseable {

    public ReentrantLock lock = new ReentrantLock();
    public AtomicInteger useCount = new AtomicInteger(0);
    protected final Thread ownerThread;
    public StackTraceElement[] connectStackTrace;

    private long connectedTimeMillis;
    protected NetconfConnection conn;
    protected volatile NetconfConnectionHolder holder;
    private volatile boolean disable = false;
    protected volatile boolean closed = false;
    protected long receiveSubscriberCount;
    protected String stream;
    protected String runStackTrace;
    private long connectedTimeNano;

    public NetconfPooledConnection(NetconfConnectionHolder holder) {
        super(holder.conn.sessionName, holder.conn.sshSession, holder.conn.netconfSession, holder.conn.jncSubscriber);
        this.conn = holder.conn;
        this.holder = holder;
        ownerThread = Thread.currentThread();
        connectedTimeMillis = System.currentTimeMillis();
    }

    protected Thread getOwnerThread() {
        return ownerThread;
    }

    public long getConnectedTimeMillis() {
        return connectedTimeMillis;
    }


    public NetconfConnection getConnection() throws NetconfException {
        return conn;
    }

    public int useIncrement() {
        return useCount.incrementAndGet();
    }

    @Override
    public void close() throws NetconfException {
        if (this.disable) {
            return;
        }

        NetconfConnectionHolder holder = this.holder;
        if (holder == null) {
            return;
        }
        recycle();
    }

    @Override
    public boolean isClosed() throws NetconfException {
        if (holder == null) {
            return true;
        }
        return closed || disable;
    }

    protected void recycle() throws NetconfException {
        if (this.disable) {
            return;
        }
        int count = useCount.decrementAndGet();
        if (count <= 0) {
            if (conn.abandoned) {//废弃此链接
                holder.dataSource.discardConnection(this);
            } else {//回收链接
                holder.dataSource.recycle(this);
            }
            holder.recycle();

            this.holder = null;
            closed = true;
        }
    }

    public void discard() throws NetconfException {
        conn.abandoned = true;
        close();
    }

    protected void discardConnection() throws NetconfException {
        conn.abandoned = true;
        close();
    }

    public void updateZoneId(String zoneId) {
        if (holder != null) {
            holder.dataSource.setZoneId(zoneId);
        }
    }

    public String getZoneId() {
        if (holder != null) {
            return holder.dataSource.getZoneId();
        }
        return null;
    }

    @Override
    public void rollback() throws NetconfException {

    }

    @Override
    public void commit() throws NetconfException {

    }

    public String toString() {
        if (conn != null) {
            return conn.toString();
        } else {
            return "closed-conn-" + System.identityHashCode(this);
        }
    }

    @Override
    public NodeSet callRpc(Element data) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.callRpc(data);
    }

    @Override
    public Element rpc(String request) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.rpc(request);
    }

    @Override
    public Element rpc(Element request) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.rpc(request);
    }

    @Override
    public int sendRequest(String request) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.sendRequest(request);
    }

    @Override
    public int sendRequest(Element request) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.sendRequest(request);
    }

    @Override
    public int sendRpc(Element data) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.sendRpc(data);
    }

    @Override
    public void validate(Element configTree) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        conn.validate(configTree);
    }

    @Override
    public Element action(Element data) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.action(data);
    }

    @Override
    public NodeSet get(String xpath, YangNsPackage... yangNsPackages) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.get(xpath,yangNsPackages);
    }

    @Override
    public NodeSet get(Element subtreeFilter) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.get(subtreeFilter);
    }

    @Override
    public NodeSet getConfig(String xpath) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.getConfig(xpath);
    }

    @Override
    public NodeSet getConfig(Element subtreeFilter) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        return conn.getConfig(subtreeFilter);
    }

    @Override
    public void editConfig(Element configTree) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        conn.editConfig(configTree);
    }

    @Override
    public void editConfig(NodeSet nodeSet) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        conn.editConfig(nodeSet);
    }

    @Override
    public void subscription(String stream) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        this.stream = stream;
        holder.setStream(stream);
        this.receiveSubscriberCount = 0;
        holder.dataSource.updateSubscriberCount(stream);
        holder.dataSource.updateNotificationCount(stream, this.receiveSubscriberCount);
        conn.subscription(stream);
    }

    @Override
    public void subscription(String stream, String eventFilter, String startTime) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        this.stream = stream;
        holder.setStream(stream);
        this.receiveSubscriberCount = 0;
        holder.dataSource.updateSubscriberCount(stream);
        holder.dataSource.updateNotificationCount(stream, this.receiveSubscriberCount);
        conn.subscription(stream, eventFilter, startTime);
    }

    @Override
    public void subscription(String stream, String eventFilter, String startTime, String stopTime) throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        this.stream = stream;
        holder.setStream(stream);
        this.receiveSubscriberCount = 0;
        holder.dataSource.updateSubscriberCount(stream);
        holder.dataSource.updateNotificationCount(stream, this.receiveSubscriberCount);
        conn.subscription(stream, eventFilter, startTime, stopTime);
    }

    @Override
    public Element receiveNotification() throws NetconfException {
        runStackTrace = Utils.toString(Thread.currentThread().getStackTrace());
        Element receiveNotification = conn.receiveNotification();
        if (holder != null) {
            this.receiveSubscriberCount++;
            holder.dataSource.updateNotificationCount(conn.stream, this.receiveSubscriberCount);
        }
        return receiveNotification;
    }

    @Override
    protected void updateInputDataInteraction(String message, long inputCount, long inputTimeMillis) {
        if (conn != null)
            conn.updateInputDataInteraction(message, inputCount, inputTimeMillis);
    }

    @Override
    protected void updateOutputDataInteraction(String message, long outputCount, long outputTimeMillis) {
        if (conn != null)
            conn.updateOutputDataInteraction(message, outputCount, outputTimeMillis);
    }

    @Override
    public void startTransaction() throws Exception {
        if (conn != null)
            conn.startTransaction();
    }

    @Override
    public void commitTransaction() throws Exception {
        if (conn != null)
            conn.commitTransaction();
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        if (conn != null)
            conn.setAutoCommit(autoCommit);
    }

    @Override
    public void unlock() throws Exception {
        if (conn != null)
            conn.unlock();
    }

    public String getRunStackTrace() {
        return runStackTrace;
    }

    protected void setRunStackTrace(String runStackTrace) {
        this.runStackTrace = runStackTrace;
    }

    public String getStream() {
        return holder.getStream();
    }

    public StackTraceElement[] getConnectStackTrace() {
        return connectStackTrace;
    }

    protected void setConnectStackTrace(StackTraceElement[] connectStackTrace) {
        this.connectStackTrace = connectStackTrace;
    }

    public long getConnectedTimeNano() {
        return connectedTimeNano;
    }

    protected void setConnectedTimeNano() {
        if (connectedTimeNano <= 0) {
            this.setConnectedTimeNano(System.nanoTime());
        }
    }

    protected void setConnectedTimeNano(long connectedTimeNano) {
        this.connectedTimeNano = connectedTimeNano;
    }


}
