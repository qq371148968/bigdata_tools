package com.yongqing.presto.hbase.io;

import com.yongqing.presto.hbase.HbaseClient;
import com.yongqing.presto.hbase.HbaseConnectorId;
import com.yongqing.presto.hbase.conf.HbaseConfig;
import com.yongqing.presto.hbase.model.HbaseColumnHandle;
import com.yongqing.presto.hbase.model.HbaseSplit;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorSplit;
import com.facebook.presto.spi.RecordSet;
import com.facebook.presto.spi.connector.ConnectorRecordSetProvider;
import com.facebook.presto.spi.connector.ConnectorTransactionHandle;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class HbaseRecordSetProvider
        implements ConnectorRecordSetProvider
{
    private final String connectorId;
    private final HbaseClient hbaseClient;
    private final HbaseConfig config;

    @Inject
    public HbaseRecordSetProvider(
            HbaseClient hbaseClient,
            HbaseConnectorId connectorId,
            HbaseConfig config)
    {
        this.hbaseClient = requireNonNull(hbaseClient, "hbaseClient is null");
        this.connectorId = requireNonNull(connectorId, "connectorId is null").toString();
        this.config = requireNonNull(config, "config is null");
    }

    @Override
    public RecordSet getRecordSet(ConnectorTransactionHandle transactionHandle, ConnectorSession session, ConnectorSplit split, List<? extends ColumnHandle> columns)
    {
        requireNonNull(split, "split is null");
        HbaseSplit hbaseSplit = (HbaseSplit) split;
        checkArgument(hbaseSplit.getConnectorId().equals(connectorId), "split is not for this connector");

        ImmutableList.Builder<HbaseColumnHandle> handles = ImmutableList.builder();
        for (ColumnHandle handle : columns) {
            handles.add((HbaseColumnHandle) handle);
        }

        return new HbaseRecordSet(hbaseClient, session, hbaseSplit, handles.build());
    }
}
