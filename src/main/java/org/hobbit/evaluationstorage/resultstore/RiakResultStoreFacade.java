/**
 * This file is part of evaluation-storage.
 *
 * evaluation-storage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * evaluation-storage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with evaluation-storage.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hobbit.evaluationstorage.resultstore;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hobbit.core.data.ResultPair;
import org.hobbit.evaluationstorage.Constants;
import org.hobbit.evaluationstorage.ContainerController;
import org.hobbit.evaluationstorage.ResultFuture;
import org.hobbit.evaluationstorage.ResultPairIterator;
import org.hobbit.evaluationstorage.ResultType;
import org.hobbit.evaluationstorage.RiakResultFuture;
import org.hobbit.evaluationstorage.SerializableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.ListKeys;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;

/**
 * Result store facade implementation that communicates with a Riak instance.
 *
 * @author Ruben Taelman (ruben.taelman@ugent.be)
 */
public class RiakResultStoreFacade implements ResultStoreFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiakResultStoreFacade.class);

    private static final String RIAK_IMAGE_NAME = "git.project-hobbit.eu:4567/gitadmin/basho-riak-kv:1.0.0";

    private final ContainerController containerController;
    private final String[] nodeContainerIds;
    protected final RiakCluster cluster;
    protected final RiakClient client;

    public RiakResultStoreFacade(ContainerController containerController) throws Exception {
        int nodes = Integer.parseInt(System.getenv().getOrDefault(Constants.RIAK_NODES, "5"));
        int httpPort = Integer
                .parseInt(System.getenv().getOrDefault(Constants.RIAK_HTTP_PORT_BASE, Integer.toString(8098)));
        int pbPort = Integer
                .parseInt(System.getenv().getOrDefault(Constants.RIAK_PB_PORT_BASE, Integer.toString(8087)));
        this.containerController = containerController;

        LOGGER.info(String.format("Setting up Riak cluster with %s nodes...", nodes));
        this.nodeContainerIds = new String[nodes];
        String hostName = startContainers();
        waitForContainers(httpPort);

        RiakNode node = new RiakNode.Builder().withRemoteAddress(hostName).withRemotePort(pbPort).build();
        this.cluster = new RiakCluster.Builder(node).build();
        this.client = new RiakClient(cluster);
    }

    protected String startContainers() throws Exception {
        String[] baseVariables = new String[] { "CLUSTER_NAME=riakkv", "HOST=0.0.0.0" };
        for (int nodeId = 0; nodeId < nodeContainerIds.length; nodeId++) {
            LOGGER.info(String.format("Starting node %s....", nodeId));
            String[] env = baseVariables;
            if (nodeId > 0) {
                env = ArrayUtils.add(baseVariables, "COORDINATOR_NODE=" + nodeContainerIds[0]);
            }
            nodeContainerIds[nodeId] = containerController.createContainer(RIAK_IMAGE_NAME, env);
            if (nodeContainerIds[nodeId] == null) {
                LOGGER.error("Couldn't create node. Aborting.");
                throw new Exception("Couldn't create node. Aborting.");
            }
        }
        return nodeContainerIds[0];
    }

    protected void waitForContainers(int httpPort) {
        for (int nodeId = 0; nodeId < nodeContainerIds.length; nodeId++) {
            LOGGER.info(String.format("Waiting for node %s....", nodeId));
            // This only checks if the node has been started, this does not mean
            // it is usable yet!
            int count = 0;
            while (!isNodeUp(nodeContainerIds[nodeId], httpPort)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //++count;
                if (count > 120) {
                    throw new IllegalStateException(
                            "Didn't got a response from node " + nodeId + " (" + nodeContainerIds[nodeId] + ").");
                }
            }
        }
        LOGGER.info(String.format("Finished setting up a Riak cluster with %s nodes.", nodeContainerIds.length));
    }

    protected boolean isNodeUp(String host, int port) {
        try {
            URL nodeUrl = new URL("http://" + host + ":" + port + "/ping");
            String response = getFromUrl(nodeUrl);
            LOGGER.info("Got \"{}\" from \"{}\"", response, nodeUrl.toString());
            if ("OK".equals(response)) {
                return true;
            } else {
                nodeUrl = new URL("http://" + host + ":" + port + "/explore/ping");
                response = getFromUrl(nodeUrl);
                LOGGER.info("Got \"{}\" from \"{}\"", response, nodeUrl.toString());
                return "OK".equals(response);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected static String getFromUrl(URL url) {
        try {
            return IOUtils.toString(url.openStream());
        } catch (IOException e) {
            return null;
        }
    }

    protected Namespace getNamespace(ResultType resultType) {
        return new Namespace(resultType.name());
    }

    protected Location getLocation(ResultType resultType, String taskId) {
        Namespace bucket = getNamespace(resultType);
        return new Location(bucket, taskId);
    }

    @Override
    public void put(ResultType resultType, String taskId, SerializableResult result) {
        RiakObject riakObject = new RiakObject().setValue(BinaryValue.create(result.serialize()));

        Location objectLocation = getLocation(resultType, taskId);
        StoreValue storeOp = new StoreValue.Builder(riakObject).withLocation(objectLocation).build();

        try {
            client.execute(storeOp);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected ResultFuture get(Location location) {
        FetchValue fetch = new FetchValue.Builder(location).build();
        RiakFuture<FetchValue.Response, Location> future = client.executeAsync(fetch);
        return new RiakResultFuture(future);
    }

    @Override
    public ResultFuture get(ResultType resultType, String taskId) {
        return get(getLocation(resultType, taskId));
    }

    @Override
    public Iterator<ResultPair> createIterator() {
        Namespace namespace = getNamespace(ResultType.EXPECTED);
        ListKeys listKeys = new ListKeys.Builder(namespace).build();
        ListKeys.Response response;
        try {
            response = client.execute(listKeys);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyIterator();
        }
        return new ResultPairIterator(response.iterator(), this);
    }

    @Override
    public void init() throws Exception {
        cluster.start();

        // Try looking up a non-existing value for checking if the cluster is
        // usable.
        // Retry until lookups stop failing, and we get an empty response.
        boolean stabilized = false;
        LOGGER.info("Waiting for cluster stabilitation...");
        while (!stabilized) {
            try {
                client.execute(new FetchValue.Builder(new Location(new Namespace("_dummy"), "_dummy")).build());
                stabilized = true;
            } catch (ExecutionException | InterruptedException e) {
                Thread.sleep(1000);
            }
        }
    }

    @Override
    public void run() throws Exception {
        // nothing to do
    }

    @Override
    public void close() throws IOException {
        if (cluster != null) {
            cluster.shutdown();
        }

        if (nodeContainerIds != null) {
            // Iterate in reverse to make sure that the master node is the last
            // one to be stopped
            for (int nodeId = nodeContainerIds.length - 1; nodeId >= 0; nodeId--) {
                containerController.stopContainer(nodeContainerIds[nodeId]);
            }
        }
    }

    @Override
    public void containerStopped(String containerName, int exitCode) throws IllegalStateException {
        for (int i = 0; i < nodeContainerIds.length; ++i) {
            if (containerName.equals(nodeContainerIds[i]) && (exitCode != 0)) {
                throw new IllegalStateException(
                        "Riak container terminated unexpectedly with exit code " + exitCode + ".");
            }
        }
    }
}
