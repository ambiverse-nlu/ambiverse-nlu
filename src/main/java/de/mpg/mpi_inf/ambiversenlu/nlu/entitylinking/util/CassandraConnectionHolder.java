package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.CassandraConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.UnexpectedException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class CassandraConnectionHolder {

    private static final Logger logger =
        LoggerFactory.getLogger(CassandraConnectionHolder.class);
    private final boolean read;

    private Cluster cluster;
    private Session session;
    private String replicationFactor;

    public CassandraConnectionHolder(boolean read) throws IOException {
        this(read, CassandraConfig.FILE_NAME);
    }

    public CassandraConnectionHolder(boolean read, String fileName) throws IOException {
      this.read = read;

      Collection<InetSocketAddress> addresses = new ArrayList<>();
      String [] hp = CassandraConfig.get(fileName, CassandraConfig.HOST).split(":|,");
      replicationFactor = CassandraConfig.get(fileName, CassandraConfig.REPLICATION);
      if(hp.length % 2 != 0) {
          throw new UnexpectedException("Hosts in file " + fileName +
                  " wrongly specified, each host should be specified with the corresponding port format host:port and separated by commas");
      }
      for(int i = 0; i < hp.length; i++) {
          addresses.add(new InetSocketAddress(hp[i], Integer.parseInt(hp[++i])));
      }

      //Check if the server is running and listens on the port
      boolean shouldWait = EntityLinkingConfig.getBoolean("dataaccess.startup.wait");
      int maxWaitTimeInSeconds = EntityLinkingConfig.getAsInt("dataaccess.startup.timeoutins");
      int totalWaitTimeInSeconds = 0;


      IOException lastException = new IOException();

      do {
        try {
          // Once the server is running, create connections.
          LoadBalancingPolicy loadBalancingPolicy;
          switch (CassandraConfig.get(fileName, CassandraConfig.LOAD_BALANCING_POLICY)) {
            case "RoundRobinPolicy":
              loadBalancingPolicy = new RoundRobinPolicy();
              break;
            default:
              loadBalancingPolicy = new RoundRobinPolicy();
          }

          SocketOptions socketOptions = new SocketOptions();
          int readWritetimeout;
          int connectionTimeOut;
          int poolTimeout;
          int iddleTimeOut;
          ConsistencyLevel consistencyLevel;
          if (!read) {
            consistencyLevel = ConsistencyLevel.ALL;
            readWritetimeout = 900000000;
            connectionTimeOut = 900000000;
            poolTimeout = 900000000;
            iddleTimeOut = 900000000;
          } else {
            consistencyLevel = ConsistencyLevel.ONE; //because there are some many replicas as there are nodes
            readWritetimeout = 120000;
            connectionTimeOut = 1000;
            poolTimeout = 1000;
            iddleTimeOut = PoolingOptions.DEFAULT_IDLE_TIMEOUT_SECONDS;
          }

          socketOptions.setConnectTimeoutMillis(connectionTimeOut);
          socketOptions.setReadTimeoutMillis(readWritetimeout);

          //https://datastax.github.io/java-driver/manual/pooling/
          PoolingOptions poolingOptions = new PoolingOptions();
          poolingOptions.setCoreConnectionsPerHost(HostDistance.LOCAL, 5).setCoreConnectionsPerHost(HostDistance.REMOTE, 5)
              .setMaxConnectionsPerHost(HostDistance.LOCAL, 100).setMaxConnectionsPerHost(HostDistance.REMOTE, 100).setPoolTimeoutMillis(poolTimeout).setIdleTimeoutSeconds(iddleTimeOut);

          cluster = Cluster.builder().addContactPointsWithPorts(addresses).withLoadBalancingPolicy(loadBalancingPolicy).withQueryOptions(
              new QueryOptions().setFetchSize(Integer.parseInt(CassandraConfig.get(fileName, CassandraConfig.PAGE_SIZE))).setConsistencyLevel(consistencyLevel)).withSocketOptions(socketOptions).withPoolingOptions(poolingOptions).build();
          session = cluster.connect();


          logger.info("Cassandra session and cluster created");
          Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run()
            {
              logger.info("Shutting down Cassandra...");
              try {
                finalize();
              } catch (Throwable throwable) {
                throwable.printStackTrace();
              }
            }
          });
          return;
        } catch (NoHostAvailableException e) {
          try {
            Thread.sleep(60000);
            lastException = new IOException(e);
            totalWaitTimeInSeconds += 60;
            for (InetSocketAddress address : addresses) {
              logger.info("Database server {} not listening on port {}. Waiting for 60s.", address.getHostName(), address.getPort());
            }
            if (totalWaitTimeInSeconds >= maxWaitTimeInSeconds) {
              for (InetSocketAddress address : addresses) {
                logger.error("Database server {} not listening on port {}, still not up after waiting {}s. Trying to connect one last time.", address.getHostName(), address.getPort(), totalWaitTimeInSeconds);
              }
              break;
            }
          } catch (InterruptedException e1) {
            lastException = new IOException(e);
          }

        }
      } while(shouldWait && totalWaitTimeInSeconds < maxWaitTimeInSeconds);

      throw lastException;
    }

    public void createKeyspaceIfNotExists(String keyspace) {
        if (read) {
            throw new RuntimeException("It is not allowed to create keyspace if CassandraConnectionHolder in read mode");
        }
        session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace
                + " WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor': " + replicationFactor + " };");
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        if (session != null) {
            session.close();
        }
        if (cluster != null) {
            cluster.close();
        }
    }


    public void createKeyspace(String keyspace) {
        if (read) {
            throw new RuntimeException("It is not allowed to create keyspace if CassandraConnectionHolder in read mode");
        }
        session.execute("CREATE KEYSPACE " + keyspace //This should crash if Keyspace already exists
                + " WITH replication "
                + "= {'class':'SimpleStrategy', 'replication_factor': " + replicationFactor + " };");
    }
}