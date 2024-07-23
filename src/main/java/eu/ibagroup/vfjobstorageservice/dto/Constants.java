package eu.ibagroup.vfjobstorageservice.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class for constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String EXECUTOR_CORES = "EXECUTOR_CORES";
    public static final String EXECUTOR_INSTANCES = "EXECUTOR_INSTANCES";
    public static final String EXECUTOR_MEMORY = "EXECUTOR_MEMORY";
    public static final String EXECUTOR_REQUEST_CORES = "EXECUTOR_REQUEST_CORES";
    public static final String SHUFFLE_PARTITIONS = "SHUFFLE_PARTITIONS";
    public static final String UP_TO = "UP_TO";
    public static final String INTERVALS = "INTERVALS";
    public static final String TAGS = "TAGS";
    public static final String DRIVER_CORES = "DRIVER_CORES";
    public static final String DRIVER_MEMORY = "DRIVER_MEMORY";
    public static final String DRIVER_REQUEST_CORES = "DRIVER_REQUEST_CORES";
    public static final String PROJECT_KEY_PREFIX = "project:";
    public static final String JOB_KEY_PREFIX = ":job:";
    public static final String PIPELINE_KEY_PREFIX = ":pipeline:";
    public static final String PROJECT_CONNECTION_PREFIX = "connection:";
    public static final String GET_ALL_ERROR = "Error while executing getAll method: ";
    public static final String CLUSTER_NAME = "CLUSTER_NAME";
    public static final String POLICY = "POLICY";
    public static final String NODES = "NODES";
    public static final String ACCESS_MODE = "ACCESS_MODE";
    public static final String DATABRICKS_RUNTIME_VERSION = "DATABRICKS_RUNTIME_VERSION";
    public static final String USE_PHOTON_ACCELERATION = "USE_PHOTON_ACCELERATION";
    public static final String WORKER_TYPE = "WORKER_TYPE";
    public static final String WORKERS = "WORKERS";
    public static final String MIN_WORKERS = "MIN_WORKERS";
    public static final String MAX_WORKERS = "MAX_WORKERS";
    public static final String DRIVER_TYPE = "DRIVER_TYPE";
    public static final String AUTOSCALING_WORKERS = "AUTOSCALING_WORKERS";
    public static final String AUTOSCALING_STORAGE = "AUTOSCALING_STORAGE";
    public static final String INSTANCE_PROFILE = "INSTANCE_PROFILE";
    public static final String CLUSTER_TAGS = "CLUSTER_TAGS";
    public static final String ON_DEMAND_SPOT = "ON_DEMAND_SPOT";
    public static final String IS_ON_DEMAND_SPOT = "IS_ON_DEMAND_SPOT";
    public static final String ENABLE_CREDENTIAL = "ENABLE_CREDENTIAL";
    public static final String AVAILABILITY_ZONE = "AVAILABILITY_ZONE";
    public static final String MAX_SPOT_PRICE = "MAX_SPOT_PRICE";
    public static final String EBS_VOLUME_TYPE = "EBS_VOLUME_TYPE";
    public static final String VOLUMES = "VOLUMES";
    public static final String DB_SIZE = "DB_SIZE";
    public static final String SPARK_CONFIG = "SPARK_CONFIG";
    public static final String ENV_VAR = "ENV_VAR";
    public static final String DESTINATION = "DESTINATION";
    public static final String LOG_PATH = "LOG_PATH";
    public static final String REGION = "REGION";
    public static final String SSH_PUBLIC_KEY = "SSH_PUBLIC_KEY";
    public static final String NODE_TYPE_PARAM = "NODE_TYPE";
    public static final String CLUSTER_DATABRICKS_SCHEMA = "CLUSTER_DATABRICKS_SCHEMA";
    public static final String CLUSTER_SCRIPTS = "CLUSTER_SCRIPTS";
    public static final String DRAFT_STATUS = "Draft";
}
