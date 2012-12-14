/*
 * WriteLogs.java
 *
 * Created on September 20, 2007, 11:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TLongArrayList;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A class for logging messages statistics (SPV+MRAI,SPV+MRAI+Policies)
 * 
 * @author ahmed
 */
public class WriteLogsV3 extends Unit {

	/** Creates a new instance of WriteLogs */
	String logFile = "";
	int numberOfNodes;
	Kernel kernel;
	ArrayList<Integer> listofprefix = new ArrayList<Integer>();
	ArrayList<Integer> withdrowedprefix = new ArrayList<Integer>();
	ArrayList<NodeV2> listofnodes = new ArrayList<NodeV2>();

	public WriteLogsV3() {
	}

	/** Creates a new instance of WriteLogs */
	public WriteLogsV3(double nextSchedule, String logFile, int numberOfNodes) {
		this.nextSchedule = nextSchedule;
		this.logFile = logFile;
		this.numberOfNodes = numberOfNodes;
		this.kernel = null;
	}

	public WriteLogsV3(double nextSchedule, String logFile, int numberOfNodes,
			Kernel kernel) {
		this.nextSchedule = nextSchedule;
		this.logFile = logFile;
		this.numberOfNodes = numberOfNodes;
		this.kernel = kernel;
	}

	public String EventEditor(ArrayList<NodeV2> array) {

		ArrayList<NodeV2> list = array;
		Random generator = new Random();
		int prefix = 0;
		int node = 0;
		int time = generator.nextInt(5000) + 1;
		int type = generator.nextInt(2) + 1;
		String line = "";
		try {
			if (type == 2) // it is an announcement
			{
				node = generator.nextInt(numberOfNodes) + 1;

				prefix = generator.nextInt(100000);
				boolean exists = true;
				while (exists) {
					for (int i = 0; i < listofprefix.size(); i++) {
						if (prefix == listofprefix.get(i)) {
							prefix = generator.nextInt(100000);
							break;
						}
						if (i == listofprefix.size() - 1)
							exists = false;
					}
				}
				line = type + " " + prefix + " " + node + " " + time;
				// System.out.println(type+"\t"+prefix+"\t"+node+"\t"+time);
			} else // it is a withdrawal
			{
				int node_prefix = 0;
				node = generator.nextInt(numberOfNodes) + 1;
				node_prefix = list.get(node - 1).getAttachedNetworks().size();
				if (node_prefix > 1)
					prefix = list.get(node - 1).getAttachedNetworks()
							.get(generator.nextInt(node_prefix));
				else
					prefix = list.get(node - 1).getAttachedNetworks().get(0);
				for (int i = 0; i < withdrowedprefix.size(); i++) {
					if (prefix == withdrowedprefix.get(i))
						return "";
				}
				line = type + " " + prefix + " " + node + " " + time;
				withdrowedprefix.add(prefix);

			}

		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out
					.println("Trying to withdraw a non-existing prefix (at node "
							+ node + ")");
		}
		return line;
	}

	public void act(int enabler) {
		try {
			FileOutputStream fout = new FileOutputStream(logFile);
			PrintStream ps = new PrintStream(fout);
			ps.println(MessagesMonitor.total);

			/* Rashed */
			FileWriter fstream = new FileWriter("NodeRoutingTable.txt", true);
			BufferedWriter bufout = new BufferedWriter(fstream);

			for (int i = 1; i <= numberOfNodes; i++) {

				NodeV2 node = Topology.getNodeByAddressV2(i);
				TIntArrayList staticAdj = Topology.getStaticAdj(i);
				// node.getRib().toString();
				if (MessagesMonitor.withTime) {
					TLongArrayList totalStat = node.getMonitor().getTotalStat();
					TLongArrayList maxStat = node.getMonitor().getMaxStat();
					long noOfMessages = node.getMonitor().getNoOfMessages();
					long totalMessages = node.getMonitor().getTotalMessages();
					String total = "";
					String max = "";
					for (int j = 0; j < totalStat.size(); j++) {
						total = total + "" + totalStat.get(j) + "\t";
						max = max + "" + maxStat.get(j) + "\t";
					}
					ps.println(i + "\t" + max + "" + noOfMessages);
					ps.println(i + "\t" + total + "" + totalMessages);

					/* Rashed */
					// bufout.write("UPdate node " +i + ": " +
					// node.getRib().toString() + "\n");
					// bufout.flush();
					// Rashed

				} else {
					TIntIntHashMap myUpStat = node.getMonitor()
							.getNeighborsStatUp();
					TIntIntHashMap myDownStat = node.getMonitor()
							.getNeighborsStatDown();
					String stat = "";
					for (int k = 0; k < staticAdj.size(); k++) {
						if (staticAdj.size() != k + 1)
							stat += staticAdj.get(k) + "\t"
									+ myDownStat.get(staticAdj.get(k)) + "\t"
									+ myUpStat.get(staticAdj.get(k)) + "\t";
						else
							stat += staticAdj.get(k) + "\t"
									+ myDownStat.get(staticAdj.get(k)) + "\t"
									+ myUpStat.get(staticAdj.get(k));
					}
					ps.println(node.getMonitor().getTotalMessages() + "\t"
							+ node.getMonitor().getTotalDownPhase() + "\t"
							+ node.getMonitor().getTotalUPPhase() + "\t" + stat);
					/* Rashed */
					// bufout.write("Update node " +i + ": " +
					// GetTime.class.newInstance()+ "\n");

					/**
					 * if(this.kernel != null) { GetTime getTime = new
					 * GetTime(0); getTime.getProcessingTime(kernel);
					 */
					// bufout.write("Update node " +i + ": " +
					// getTime.getProcessingTime(kernel) + "\n");
					// bufout.write("Update node " +i + ": " +
					// System.currentTimeMillis()+ "\n");
					/*
					 * for (int k = 0; k < node.getAttachedNetworks().size();
					 * k++) {
					 * listofprefix.add(node.getAttachedNetworks().get(k)); }
					 * listofnodes.add(node);
					 */
					if (enabler == 1) {
						bufout.write("RIB of node " + i + ": \n\n"
								+ node.getRib() + "\n");
						bufout.flush();
					}
				}
			}
			bufout.close();
			/*
			 * if (enabler == 1) { FileWriter fileevent = new
			 * FileWriter("generated_event.txt", true); BufferedWriter
			 * bufferevent = new BufferedWriter(fileevent); int numberOfEvent =
			 * 50; for (int num = 1; num <= numberOfEvent; num++) { String out =
			 * EventEditor(listofnodes); if (num == numberOfEvent) { if (out !=
			 * "") { bufferevent.write(out); bufferevent.flush(); }
			 * 
			 * } else { if (out != "") { bufferevent.write(out + "\n");
			 * bufferevent.flush(); }
			 * 
			 * } // bufferevent.flush(); }
			 * System.out.println("Stored prefixes : " +
			 * listofprefix.toString()); System.out.println("Prefix lenght : " +
			 * listofprefix.size()); bufferevent.close(); }
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}