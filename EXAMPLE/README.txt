{\rtf1\ansi\ansicpg1252\cocoartf1038\cocoasubrtf350
{\fonttbl\f0\fswiss\fcharset0 Helvetica;}
{\colortbl;\red255\green255\blue255;}
\paperw11900\paperh16840\margl1440\margr1440\vieww16880\viewh14660\viewkind0
\pard\tx566\tx1133\tx1700\tx2267\tx2834\tx3401\tx3968\tx4535\tx5102\tx5669\tx6236\tx6803\ql\qnatural\pardirnatural

\f0\fs24 \cf0 \
README\
--------------\
\
SIMROT-sim is simple BGP simulator that aims to study the dynamics and propagation of routing updates in an AS-Graph.\
\
In the following we list what is captured and assumed by the simulator:\
\
1- Each AS is an atomic node.\
2- Each node has Adj_RIB_INs, Loc_RIB, Adj_RIB_Out tables.\
3- MRAI.\
4- Simplified decision process as follows:\
     i - First using local preferences (routes learned from customers are preferred m\
          more than Peers more than Providers).\
     ii- AS PATH length.\
     iii- The last tie-breaker is  a hash value of nodes' ID.\
 5- Export filters.\
\
The simulator is designed with scalability in mind. Thus it is able to simulate BGP dynamics in several thousands nodes network. (More detailed documentation will follow shortly).\
\
To run the simulator please use the following command:\
\
     % java  -jar  SIMROT-sim.jar $topology_file $latencies_file $event_file $expected_number_of_simulation_events $log_file $Use_partial_convergence $MRAI $magic_number $Protocol_version\
\

	java  -jar  SIMROT-sim.jar topology_file latency_file event_file 100 log_file 0 30 10 0


SIMROT-sim.jar is located under SIMROT-sim/dist/\
\
A - $topology_file: refers to the topology of the network we want to simulate.\
B-  $latencies_file: specifies a text file contains all inter-AS links delay.\
C-  $event_file: Event file of all events we want to simulate such as prefix-up/prefix-down\
D- $expected_number_of_simulation_events: How large you want the simulator queue to be usually we set it to 100000.\
E- $log_file: The log file.\
F- $Use_partial_convergence: Either 0 or 1. This means do we want initially to converge all prefixes from the topology file. However, it is useful to converge\
     only the prefixes we want to simulate in order to scale better.\
G- $MRAI: MRAI timer value in seconds.\
H-$magic_number: An integer used to generate random seeds by the simulator (primary for message processing time by a node).\
I- $Protocol_version: There are two versions now. 0 stands for SPV-No Policies, and 1 stands for SPV-Policies.\
\
\
Topology file format:\
----------------------------\
\
Here is the topology file format accepted by the simulator:\
\
Line1  : # of nodes in the topology - n\
Line2  : The prefixes  announced by node 1 (The prefixes are just integers)\
.\
.\
.\
Line n+1: The prefixes announced by node n\
Line n+2 : node 1 adjacency information (in neighbor relation pairs i.e 2 3 here 2 denotes that node 2 is a neighbor for the current node and 3 
	denotes that the relation between them is provider-cusromer. Relations Code 0 provider, 1 Peer, and 3 Customer).\
.\
.\
.\
Line 2n+1: node n adjacency information.\
\
\
Evt files format:\
----------------------\
\
Here is the event file format accepted by the simulator (Now we support only prefix up and down events):\
\
The file may consist of many lines with the following format\
\
Event_Code   Affected_Prefix  origin_AS  Time\
\
Event_Code: 1 for prefix withdrawal, 2 for announcements.\
Affected_Prefix: The failed or restored prefix.\
origin_AS; The AS that owns this prefix.\
Time; That time at which the event takes place.\
\
\
Latency files format:\
----------------------------\
\
Here is the latency file format accepted by the simulator:\
\
The file may consist of many lines with the following format\
\
Node_1 Node_2 Latency\
\
The above line means that the uni-directional link between node_1 and node_2 has latency equals to Latency.\
\
\
More detailed documentation is following shortly. Any question can be sent to Ahmed Elmokashfi;  ahmed at simula.no\
}