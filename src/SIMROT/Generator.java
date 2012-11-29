/*
 * Generator.java
 *
 * Created on December 19, 2007, 2:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package SIMROT;

import gnu.trove.TIntArrayList;

/**
 * Generates an AS level topology based on the user inputs
 * @author ahmed
 */
public class Generator {
    Parameters parameters;
    Graph graph;
    RegionsAssigner assigner;
    RandomGenerator random;
    TIntArrayList tierOnes = new TIntArrayList();
    TIntArrayList middleNodes  = new TIntArrayList();
    TIntArrayList cpNodes = new TIntArrayList();
    TIntArrayList cNodes = new TIntArrayList();
    public static boolean GLP = false;
    private static double beta = 0.90;
    /**
     * Creates a new instance of Generator
     * @param parameters Topology configurations
     * @param graph An object for storing the topology
     * @param assigner An object for assiging nodes to regions
     * @param random Random number generator
     */
    public Generator(Parameters parameters,Graph graph,RegionsAssigner assigner,RandomGenerator random) {
        this.parameters = parameters;
        this.graph = graph;
        this.assigner = assigner;
        this.random = random;
    }
    /**
     *  Generate the topology
     */
    public void GenerateGraph() {
        int M_LB=parameters.getN_TierOne()+1;
        int M_UB=parameters.getN_TierOne()+parameters.getN_Middle();
        int CP_LB=M_UB+1;
        int CP_UB=M_UB+parameters.getN_CP();
        int S_LB=CP_UB+1;
        int S_UB=CP_UB+parameters.getN_Stubs();
        addTierOnes(parameters.getN_TierOne());
        addMiddleNodes(M_LB,M_UB);
        addCPNodes(CP_LB,CP_UB);
        addStubNodes(S_LB,S_UB);
        addPeers(Values.CP,Values.Middle);
        addPeers(Values.CP,Values.CP);
    }
    private void addTierOnes(int numberOfTierOnes) {
        for(int i=1;i<=numberOfTierOnes;i++) {
            Node node = new Node(i,Values.Tier_1,assigner.getRegions(parameters.getNum_Of_Regions(),parameters));
            graph.addNode(node);
            tierOnes.add(i);
        }
        meshTierOnes(numberOfTierOnes);
    }
    private void meshTierOnes(int numberOfTierOnes) {
        for(int i=1;i<=numberOfTierOnes;i++) {
            for(int j=i+1;j<=numberOfTierOnes;j++) {
                graph.getNode(i).addPeer(j);
                graph.getNode(j).addPeer(i);
            }
        }
    }
    private void addMiddleNodes(int LB,int UB) {
        for(int i=LB;i<=UB;i++) {
            Node node = new Node(i,Values.Middle,assigner.getRegions(random.getNumberOfRegions(parameters.getRegionsCoinFlipper_M()),parameters));
            graph.addNode(node);
            middleNodes.add(i);
            addProviders(node);
        }
    }
    private void addCPNodes(int LB,int UB) {
        for(int i=LB;i<=UB;i++) {
            Node node = new Node(i,Values.CP,assigner.getRegions(random.getNumberOfRegions(parameters.getRegionsCoinFlipper_CP()),parameters));
            graph.addNode(node);
            cpNodes.add(i);
            addProviders(node);
        }
    }
    private void addStubNodes(int LB,int UB) {
        for(int i=LB;i<=UB;i++) {
            Node node = new Node(i,Values.Stub,assigner.getRegions(1,parameters));
            graph.addNode(node);
            cNodes.add(i);
            addProviders(node);
        }
    }
    private TIntArrayList getCandidates(int nodeAddress,int nodeType,TIntArrayList regions) {
        TIntArrayList candidates = new TIntArrayList();
        for(int i=0;i<regions.size();i++) {
            TIntArrayList result = graph.getNodesPerRegion(nodeType,regions.get(i));
            for(int j=0;j<result.size();j++) {
                if(result.get(j)!=nodeAddress)
                    candidates.add(result.get(j));
            }
        }
        return candidates;
    }
     private TIntArrayList getCandidatesPeering(int nodeAddress,int nodeType,TIntArrayList regions) {
        TIntArrayList candidates = new TIntArrayList();
        for(int i=0;i<regions.size();i++) {
            TIntArrayList result = graph.getNodesPerRegion(Values.CP,regions.get(i));
            for(int j=0;j<result.size();j++) {
                if(result.get(j)!=nodeAddress)
                    candidates.add(result.get(j));
            }
            /*result = graph.getNodesPerRegion(Values.Stub,regions.get(i));
            for(int j=0;j<result.size();j++) {
                if(result.get(j)!=nodeAddress)
                    candidates.add(result.get(j));
            }*/
        }
        return candidates;
    }
    private TIntArrayList getCandidatesEdges(int nodeAddress,int nodeType,TIntArrayList regions) {
        TIntArrayList candidates = new TIntArrayList();
        for(int i=0;i<regions.size();i++) {
            TIntArrayList result = graph.getNodesPerRegion(nodeType,regions.get(i));
            for(int j=0;j<result.size();j++) {
                if(result.get(j)!=nodeAddress)
                    candidates.add(result.get(j));
            }
        }
        return candidates;
    }
    
    private void addProviders(Node node) {
        int nodeType = node.getNode_Type();
        int numberOfProviders;
        int numberOfProvidersT1=0;
        int numberOfMiddleProviders;
        TIntArrayList regions = node.getRegions();
        TIntArrayList candidates;
        if(nodeType==Values.Middle) {
            numberOfProviders = parameters.getMHD_M().getNoProvider(random);
            numberOfProvidersT1 = parameters.getMHD_M().getNoT1Providers(random,numberOfProviders);
            if(numberOfProvidersT1<numberOfProviders)
                numberOfMiddleProviders = numberOfProviders-numberOfProvidersT1;
            else
                numberOfMiddleProviders=0;
            candidates = getCandidates(node.getAS_NUM(),nodeType,regions);
            if(candidates.size()<numberOfMiddleProviders) {
                numberOfMiddleProviders = candidates.size();
                numberOfProvidersT1 = numberOfProviders-numberOfMiddleProviders;
                if(numberOfProvidersT1>parameters.getN_TierOne())
                    numberOfProvidersT1=parameters.getN_TierOne();
            }
            if((numberOfProvidersT1!=0)&&!(candidates.isEmpty())) {
                if(numberOfProvidersT1!=0) {
                    
                    if(numberOfProvidersT1==parameters.getN_TierOne()) {
                        for(int i=0;i<numberOfProvidersT1;i++) {
                            int nextProvider = tierOnes.get(i);
                            node.addProvider(nextProvider);
                            graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
                        }
                    } else {
                        TIntArrayList clonedTierOnes = (TIntArrayList) tierOnes.clone();
                        for(int i=0;i<numberOfProvidersT1;i++) {
                            if(!clonedTierOnes.isEmpty())
                            {    
                            int indexNextProvider = 0;
                            if(GLP)
                                indexNextProvider = chooseProviderGLP(clonedTierOnes,beta);
                            else
                                indexNextProvider = chooseProvider(clonedTierOnes);
                            int nextProvider = clonedTierOnes.get(indexNextProvider);
                            node.addProvider(nextProvider);
                            graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
                            clonedTierOnes.remove(indexNextProvider);
                        }
                        }
                    }
                }
                if(!candidates.isEmpty()) {
                    for(int i=0;i<numberOfMiddleProviders;i++) {
                        if(!candidates.isEmpty()) {
                            int indexNextProvider = 0;
                            if(GLP)
                                indexNextProvider = choosePeerGLP(candidates,beta);
                            else
                                indexNextProvider = chooseProvider(candidates);
                            int nextProvider = candidates.get(indexNextProvider);
                            node.addProvider(nextProvider);
                            graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
                            candidates.remove(indexNextProvider);
                        }
                    }
                }
            } else {
                numberOfProvidersT1 = 1;
                int indexNextProvider=chooseProvider(tierOnes);
                int nextProvider = tierOnes.get(indexNextProvider);
                node.addProvider(nextProvider);
                graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
            }
        } else {
            if(nodeType==Values.CP) {
                numberOfProviders = parameters.getMHD_CP().getNoProvider(random);
                numberOfProvidersT1 = parameters.getMHD_CP().getNoT1Providers(random,numberOfProviders);
                if(numberOfProvidersT1<=numberOfProviders)
                    numberOfMiddleProviders = numberOfProviders-numberOfProvidersT1;
                else
                    numberOfMiddleProviders=0;
                candidates = getCandidates(node.getAS_NUM(),Values.Middle,regions);
                if(numberOfProvidersT1!=0) {
                    TIntArrayList clonedTierOnes = (TIntArrayList) tierOnes.clone();
                    for(int i=0;i<numberOfProvidersT1;i++) {
                        if(!clonedTierOnes.isEmpty())
                        {    
                        int indexNextProvider = 0;
                        if(GLP)
                            indexNextProvider = chooseProviderGLP(clonedTierOnes,beta);
                        else
                            indexNextProvider = chooseProvider(clonedTierOnes);
                        int nextProvider = clonedTierOnes.get(indexNextProvider);
                        node.addProvider(nextProvider);
                        graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
                        clonedTierOnes.remove(indexNextProvider);
                        }
                    }
                }
                if(!candidates.isEmpty()) {
                    for(int i=0;i<numberOfMiddleProviders;i++) {
                        if(!candidates.isEmpty()) {
                            int indexNextProvider = 0;
                            if(GLP)
                                indexNextProvider = choosePeerGLP(candidates,beta);
                            else
                                indexNextProvider = chooseProvider(candidates);
                            int nextProvider = candidates.get(indexNextProvider);
                            node.addProvider(nextProvider);
                            graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
                            candidates.remove(indexNextProvider);
                        }
                    }
                }
            } else {
                numberOfProviders = parameters.getMHD_C().getNoProvider(random);
          
                numberOfProvidersT1 = parameters.getMHD_C().getNoT1Providers(random,numberOfProviders);
                if(numberOfProvidersT1<=numberOfProviders)
                    numberOfMiddleProviders = numberOfProviders-numberOfProvidersT1;
                else
                    numberOfMiddleProviders=0;
                
                candidates = getCandidates(node.getAS_NUM(),Values.Middle,regions);
                if(numberOfProvidersT1!=0) {
                    TIntArrayList clonedTierOnes = (TIntArrayList) tierOnes.clone();
                    for(int i=0;i<numberOfProvidersT1;i++) {
                        if(!clonedTierOnes.isEmpty())
                        {    
                        int indexNextProvider = 0;
                        if(GLP)
                            indexNextProvider = chooseProviderGLP(clonedTierOnes,beta);
                        else
                            indexNextProvider = chooseProvider(clonedTierOnes);
                        int nextProvider = clonedTierOnes.get(indexNextProvider);
                        node.addProvider(nextProvider);
                        graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
                        clonedTierOnes.remove(indexNextProvider);
                        }
                    }
                }
                if(!candidates.isEmpty()) {
                    for(int i=0;i<numberOfMiddleProviders;i++) {
                        if(!candidates.isEmpty()) {
                            int indexNextProvider = 0;
                            if(GLP)
                                indexNextProvider = choosePeerGLP(candidates,beta);
                            else
                                indexNextProvider = chooseProvider(candidates);
                            int nextProvider = candidates.get(indexNextProvider);
                            node.addProvider(nextProvider);
                            graph.getNode(nextProvider).addCustomer(node.getAS_NUM());
                            candidates.remove(indexNextProvider);
                        }
                    }
                }
                
            }
        }
    }
    
    private void addPeers(int selectionType,int nodesType) {
        if(nodesType==Values.Middle) {
            for(int i=0;i<middleNodes.size();i++) {                
                Node node = graph.getNode(middleNodes.get(i));
                int numberOfPeers = parameters.getP_m_m().getNoPeeringLinks(random);
                //System.out.println(numberOfPeers);
                if(numberOfPeers!=0) {
                    TIntArrayList candidates = getCandidates(middleNodes.get(i),nodesType,node.getRegions());
                    TIntArrayList removalCandidates = new TIntArrayList();
                    for(int k=0;k<candidates.size();k++) {
                        if(node.isPeer(candidates.get(k)))
                            removalCandidates.add(candidates.get(k));
                    }
                    if(!removalCandidates.isEmpty())
                        removeAll(candidates,removalCandidates);
                    toPeerValidator(node.getAS_NUM(),Values.Middle,candidates);
                    if(selectionType==Values.SizeBased) {
                        
                        for(int j=0;j<numberOfPeers;j++) {
                            if(!candidates.isEmpty()) {
                                int indexNextPeer = choosePeer(middleNodes.get(i),candidates);
                                int nextPeer = candidates.get(indexNextPeer);
                                node.addPeer(nextPeer);
                                graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                candidates.remove(indexNextPeer);
                            }
                        }
                    } else {
                        for(int j=0;j<numberOfPeers;j++) {
                            if(!candidates.isEmpty()) {
                                int indexNextPeer = 0;
                                indexNextPeer = choosePeerBA(candidates);
                                int nextPeer = candidates.get(indexNextPeer);
                                node.addPeer(nextPeer);
                                graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                candidates.remove(indexNextPeer);
                            }
                        }
                        
                    }
                }
            }
        } else {
            if(nodesType==Values.CP) {
                for(int i=0;i<cpNodes.size();i++) {
                    Node node = graph.getNode(cpNodes.get(i));
                    int numberOfPeersCP = parameters.getP_cp_cp().getNoPeeringLinks(random);
                    int numberOfPeersCPM = parameters.getP_cp_m().getNoPeeringLinks(random);
                    if(numberOfPeersCP!=0 && numberOfPeersCPM!=0) {
                        TIntArrayList candidatesMiddle = getCandidates(node.getAS_NUM(),Values.Middle,node.getRegions());
                        TIntArrayList candidatesCP = getCandidatesPeering(node.getAS_NUM(),Values.CP,node.getRegions());
                        TIntArrayList removalCandidatesMiddle = new TIntArrayList();
                        for(int k=0;k<candidatesMiddle.size();k++) {
                            if(node.isPeer(candidatesMiddle.get(k)))
                                removalCandidatesMiddle.add(candidatesMiddle.get(k));
                        }
                        if(!removalCandidatesMiddle.isEmpty())
                            removeAll(candidatesMiddle,removalCandidatesMiddle);
                        toPeerValidator(node.getAS_NUM(),Values.CP,candidatesMiddle);
                        TIntArrayList removalCandidatesCP = new TIntArrayList();
                        for(int k=0;k<candidatesCP.size();k++) {
                            if(node.isPeer(candidatesCP.get(k)))
                                removalCandidatesCP.add(candidatesCP.get(k));
                        }
                        if(!removalCandidatesCP.isEmpty())
                            removeAll(candidatesCP,removalCandidatesCP);
                        if(selectionType==Values.SizeBased) {
                            for(int j=0;j<numberOfPeersCP;j++) {
                                if(!candidatesCP.isEmpty()) {
                                    int indexNextPeer = choosePeer(cpNodes.get(i),candidatesCP);
                                    int nextPeer = candidatesCP.get(indexNextPeer);
                                    node.addPeer(nextPeer);
                                    graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                    candidatesCP.remove(indexNextPeer);
                                }
                            }
                            
                        } else {
                            for(int j=0;j<numberOfPeersCP;j++) {
                                if(!candidatesCP.isEmpty()) {
                                    int indexNextPeer = choosePeerUniform(candidatesCP);
                                    int nextPeer = candidatesCP.get(indexNextPeer);
                                    node.addPeer(nextPeer);
                                    graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                    candidatesCP.remove(indexNextPeer);
                                }
                            }
                        }
                        for(int j=0;j<numberOfPeersCPM;j++) {
                            if(!candidatesMiddle.isEmpty()) {
                                int indexNextPeer = choosePeerBA(candidatesMiddle);
                                int nextPeer = candidatesMiddle.get(indexNextPeer);
                                node.addPeer(nextPeer);
                                graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                candidatesMiddle.remove(indexNextPeer);
                            }
                        }
                    }
                }
            }
            else
            {
              for(int i=0;i<cNodes.size();i++) {
                    Node node = graph.getNode(cNodes.get(i));
                    int numberOfPeersCP = parameters.getP_cp_cp().getNoPeeringLinks(random);
                    int numberOfPeersCPM = parameters.getP_cp_m().getNoPeeringLinks(random);
                    if(numberOfPeersCP!=0 && numberOfPeersCPM!=0) {
                        TIntArrayList candidatesMiddle = getCandidates(node.getAS_NUM(),Values.Middle,node.getRegions());
                        TIntArrayList candidatesCP = getCandidatesPeering(node.getAS_NUM(),Values.CP,node.getRegions());
                        TIntArrayList removalCandidatesMiddle = new TIntArrayList();
                        for(int k=0;k<candidatesMiddle.size();k++) {
                            if(node.isPeer(candidatesMiddle.get(k)))
                                removalCandidatesMiddle.add(candidatesMiddle.get(k));
                        }
                        if(!removalCandidatesMiddle.isEmpty())
                            removeAll(candidatesMiddle,removalCandidatesMiddle);
                        toPeerValidator(node.getAS_NUM(),Values.CP,candidatesMiddle);
                        TIntArrayList removalCandidatesCP = new TIntArrayList();
                        for(int k=0;k<candidatesCP.size();k++) {
                            if(node.isPeer(candidatesCP.get(k)))
                                removalCandidatesCP.add(candidatesCP.get(k));
                        }
                        if(!removalCandidatesCP.isEmpty())
                            removeAll(candidatesCP,removalCandidatesCP);
                        if(selectionType==Values.SizeBased) {
                            for(int j=0;j<numberOfPeersCP;j++) {
                                if(!candidatesCP.isEmpty()) {
                                    int indexNextPeer = choosePeer(cpNodes.get(i),candidatesCP);
                                    int nextPeer = candidatesCP.get(indexNextPeer);
                                    node.addPeer(nextPeer);
                                    graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                    candidatesCP.remove(indexNextPeer);
                                }
                            }
                            
                        } else {
                            for(int j=0;j<numberOfPeersCP;j++) {
                                if(!candidatesCP.isEmpty()) {
                                    int indexNextPeer = choosePeerUniform(candidatesCP);
                                    int nextPeer = candidatesCP.get(indexNextPeer);
                                    node.addPeer(nextPeer);
                                    graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                    candidatesCP.remove(indexNextPeer);
                                }
                            }
                        }
                        for(int j=0;j<numberOfPeersCPM;j++) {
                            if(!candidatesMiddle.isEmpty()) {
                                int indexNextPeer = choosePeerBA(candidatesMiddle);
                                int nextPeer = candidatesMiddle.get(indexNextPeer);
                                node.addPeer(nextPeer);
                                graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                                candidatesMiddle.remove(indexNextPeer);
                            }
                        }
                    }
                }
            }    
        }
    }
 
    private void addPeers(int nodesType) {
        if(nodesType==Values.Middle) {
            double P_M = parameters.getP_M();
            for(int i=0;i<middleNodes.size();i++) {
                Node node = graph.getNode(middleNodes.get(i));
                TIntArrayList candidates = getCandidates(middleNodes.get(i),nodesType,node.getRegions());
                TIntArrayList removalCandidates = new TIntArrayList();
                for(int k=0;k<candidates.size();k++) {
                    if(node.isPeer(candidates.get(k)))
                        removalCandidates.add(candidates.get(k));
                }
                if(!removalCandidates.isEmpty())
                    removeAll(candidates,removalCandidates);
                toPeerValidator(node.getAS_NUM(),Values.Middle,candidates);
                
                for(int j=0;j<candidates.size();j++) {
                    double rand = random.getRand();
                    if(P_M<rand) {
                        int nextPeer = candidates.get(j);
                        node.addPeer(nextPeer);
                        graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                        candidates.remove(j);
                    }
                }
            }
        } else {
            if(nodesType==Values.CP) {
                double P_CP_M = parameters.getP_CP_M();
                double P_CP_CP = parameters.getP_CP_CP();
                for(int i=0;i<cpNodes.size();i++) {
                    Node node = graph.getNode(cpNodes.get(i));
                    TIntArrayList candidatesMiddle = getCandidates(node.getAS_NUM(),Values.Middle,node.getRegions());
                    TIntArrayList candidatesCP = getCandidates(node.getAS_NUM(),Values.CP,node.getRegions());
                    TIntArrayList removalCandidatesMiddle = new TIntArrayList();
                    for(int k=0;k<candidatesMiddle.size();k++) {
                        if(node.isPeer(candidatesMiddle.get(k)))
                            removalCandidatesMiddle.add(candidatesMiddle.get(k));
                    }
                    if(!removalCandidatesMiddle.isEmpty())
                        removeAll(candidatesMiddle,removalCandidatesMiddle);
                    toPeerValidator(node.getAS_NUM(),Values.CP,candidatesMiddle);
                    TIntArrayList removalCandidatesCP = new TIntArrayList();
                    for(int k=0;k<candidatesCP.size();k++) {
                        if(node.isPeer(candidatesCP.get(k)))
                            removalCandidatesCP.add(candidatesCP.get(k));
                    }
                    if(!removalCandidatesCP.isEmpty())
                        removeAll(candidatesCP,removalCandidatesCP);
                    for(int j=0;j<candidatesMiddle.size();j++) {
                        double rand = random.getRand();
                        if(P_CP_M<rand) {
                            int nextPeer = candidatesMiddle.get(j);
                            node.addPeer(nextPeer);
                            graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                            candidatesMiddle.remove(j);
                        }
                    }
                    for(int j=0;j<candidatesCP.size();j++) {
                        double rand = random.getRand();
                        if(P_CP_CP<rand) {
                            int nextPeer = candidatesCP.get(j);
                            node.addPeer(nextPeer);
                            graph.getNode(nextPeer).addPeer(node.getAS_NUM());
                            candidatesCP.remove(j);
                        }
                    }
                }
            }
            
        }
    }
    private int chooseProvider(TIntArrayList candidates) {
        int sumOutDegrees = 0;
        for(int i=0;i<candidates.size();i++) {
            sumOutDegrees+=graph.getNode(candidates.get(i)).getNodeDegree();
        }
        double probability = random.getRand();
        double sum=0.0;
        int position = -1;
        for(int index=0;index<candidates.size();index++) {
            sum+=(double)(graph.getNode(candidates.get(index)).getNodeDegree())/sumOutDegrees;
            position=index;
            if(probability<sum)
                break;
        }
        return position;
    }
    private int chooseProviderGLP(TIntArrayList candidates,double beta) {
        int sumOutDegrees = 0;
        for(int i=0;i<candidates.size();i++) {
            sumOutDegrees+=(graph.getNode(candidates.get(i)).getNodeDegree()-beta);
        }
        double probability = random.getRand();
        double sum=0.0;
        int position = -1;
        for(int index=0;index<candidates.size();index++) {
            sum+=(double)(graph.getNode(candidates.get(index)).getNodeDegree()-beta)/sumOutDegrees;
            position=index;
            if(probability<sum)
                break;
        }
        return position;
    }
    private int choosePeerBA(TIntArrayList candidates) {
     
        int sumOutDegrees = 0;
        for(int i=0;i<candidates.size();i++) {
            sumOutDegrees+=graph.getNode(candidates.get(i)).getPeerDegree();
        }
        double probability = random.getRand();
        double sum=0.0;
        int position = -1;
        for(int index=0;index<candidates.size();index++) {
            sum+=(double)(graph.getNode(candidates.get(index)).getPeerDegree())/sumOutDegrees;
            position=index;
            if(probability<sum)
                break;
        }
        return position;
    }
    private int choosePeerGLP(TIntArrayList candidates,double beta) {
        int sumOutDegrees = 0;
        for(int i=0;i<candidates.size();i++) {
            sumOutDegrees+=(graph.getNode(candidates.get(i)).getNodeDegree()-beta);
        }
        double probability = random.getRand();
        double sum=0.0;
        int position = -1;
        for(int index=0;index<candidates.size();index++) {
            sum+=((double)(graph.getNode(candidates.get(index)).getNodeDegree())-beta)/sumOutDegrees;
            position=index;
            if(probability<sum)
                break;
        }
        return position;
    }
    private int choosePeer(int node,TIntArrayList candidates) {
        int sumDegreeDiff = 0;
        int myDegree = graph.getNode(node).getNodeDegree();
        for(int i=0;i<candidates.size();i++) {
            sumDegreeDiff+= Math.abs(graph.getNode(candidates.get(i)).getNodeDegree()-myDegree);
        }
        double probability = random.getRand();
        double sum = 0.0;
        int index = -1;
        for(index=0;index<candidates.size();index++) {
            sum+=1-(Math.abs(graph.getNode(candidates.get(index)).getNodeDegree()-myDegree)/sumDegreeDiff);
            if(probability<sum)
                break;
        }
        return index;
    }
    private int choosePeerUniform(TIntArrayList candidates) {
        int indexOfNextPeer = random.getRand(candidates.size());
        return indexOfNextPeer;
    }
    private void toPeerValidator(int node,int nodeType,TIntArrayList candidates) {
        TIntArrayList toBeRemoved = new TIntArrayList();
        if(nodeType==Values.Middle) {
            for(int i=0;i<candidates.size();i++) {
                if(graph.isInMyCustomerTree(node,candidates.get(i))||graph.isInMyCustomerTree(candidates.get(i),node))
                    toBeRemoved.add(candidates.get(i));
            }
        } else {
            for(int i=0;i<candidates.size();i++) {
                if(graph.isInMyCustomerTree(node,candidates.get(i)))
                    toBeRemoved.add(candidates.get(i));
            }
        }
        if(!toBeRemoved.isEmpty())
            removeAll(candidates,toBeRemoved);
    }
    public RegionsAssigner getAssigner() {
        return assigner;
    }
    private void removeAll(TIntArrayList all,TIntArrayList toBeRemoved) {
        for(int i=0;i<toBeRemoved.size();i++) {
            all.remove(all.indexOf(toBeRemoved.get(i)));
        }
    }
}
