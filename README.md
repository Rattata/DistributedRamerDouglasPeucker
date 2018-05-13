# DistributedRamerDouglasPeucker

Distributed implementation of the Ramer-Douglas-Peucker algorithm. 
Not production ready (very security-liberal ActiveMq configuration)

## Features

* Node-failure tolerant

* Donut caching of data

* concurrent searching 



## External requirements
* apache-activemq-5.10+


## How to use
1. make sure your activemq host is running
1. start node to generate configuration files ```java -cp siege.RDP.Node```
1. modify configuration files to your configuration
1. creating the node(s), repeat steps (2,3) on each host ```java -cp siege.RDP.Node```
1. creating the registrar ```java -cp siege.RDP.Registrar```
1. submit and await lines with ```java -cp siege.RDP.Client``` ?? (yeah this sucks, should improve) (connect to the registrar and roll your own)


## Configuration

#### node.xml


variable | description | default
--- | --- | ---
consumers | number of workqueue consumers created | 2
search_segments | number of search tasks created for each incoming piece of work | 3
split | threshold for splitting segments back unto the work_queue, in contrast to solving entirely local  | 10000



#### remote.xml

variable | description | default
--- | --- | ---
ACTIVEMQ_URL | queue host | failover:(tcp://192.168.1.61:61616,localhost:8161)
ACTIVEMQ_USER | user| admin
ACTIVEMQ_PASSWORD | password | admin
REGISTRATION_MASTER | registration node host | 192.168.1.61
REGISTRATION_PORT | registration node host port | 1200
NODE_UPDATE_PORT | node update port | 1201
RMI_REGISTRAR_REGISTRAR | RMI registry key for registrar | REGISTRAR
RMI_REGISTRAR_LINEREPO | RMI registry key for IRDPrepository | LINE_REPOSITORY
RMI_NODE_UPDATE | RMI registry key node updates | UPDATENODE
QUEUE_LINES | queue key for work | 1_LINES
QUEUE_RESULTS | queue key for results | 2_RESULTS
TOPIC_CLEANUP | queue key for cleanup | 3_CLEANUP

