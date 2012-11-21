/*
 * GRIB.java
 *
 * Created on July 31, 2007, 2:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;

import gnu.trove.TIntArrayList;
import gnu.trove.TLinkedList;
import gnu.trove.TShortArrayList;

/**
 * This class represents a global routing information base in order to remove the unnecessary redundant memory usage.
 * GRIB employs a tree structure for this purpose.
 * @author ahmed
 */
public class GRIB {
    
    public static GRIBTNode root=new GRIBTNode();
    public static GRIBTNode[] levelOne;
    public static boolean phaseOne = true;
    /** Creates a new instance of GRIB */
    public GRIB() {
    }
    /** Initialize GRIB */ 
    public static void initializeRIB(int numberOfNodes)
    {
      levelOne = new GRIBTNode[numberOfNodes];
      for(short i=1;i<=numberOfNodes;i++)
      {      
        levelOne[i-1] = new GRIBTNode(root,i); 
        ++root.refCount;
      }
    }
    /**Add a new child to a certain parent tree*/
    private static synchronized void addChild(GRIBTNode parent,GRIBTNode child)
    {
      parent.getChilds().add(child);
    }
    /**Remove an existing child from a certain parent tree*/
    private static synchronized void removeChild(GRIBTNode parent,GRIBTNode child)
    {
     if(parent==null)
         System.out.println("Null");
     parent.getChilds().remove(child);    
    }
    /**Check if this entry exist in a certain parent tree*/
    public static boolean isChild(GRIBTNode parent,GRIBTNode child)
    {
      if(parent.getChilds().contains(child))
          return true;
      else
          return false;
    }
    /**Add new node*/
    public static synchronized GRIBTNode addNode(int AS,GRIBTNode parent)
    {
       GRIBTNode child = getChild(AS,parent);
       if(child!=null)
           return child;
       else
       {   
       child = new GRIBTNode(parent,AS);
       addChild(parent,child);
       increaseRefCount(child);
       return child;
       }
    }
    /**Check if the node to be removed has any children or parents*/
    public static synchronized void checkForRemoval(GRIBTNode node)
    {
      if(node.refCount==0&&node.parent!=null)
          removeChild(node.parent,node);
    }
    private static synchronized GRIBTNode getChild(int AS,GRIBTNode parent)
    {
      GRIBTNode child = null;
      TLinkedList childs = parent.getChilds();
      if(childs!=null&&!childs.isEmpty())
      {    
           
      for(int i=0;i<childs.size();i++)
      {
        if(((GRIBTNode)childs.get(i)).AS==AS)
            child = (GRIBTNode)childs.get(i);
      }
     
      }
      return child;
    }
    public static int getASPathLength(GRIBTNode node)
    {
      int val = 1;
      GRIBTNode parent = node.parent;
      while (parent!=root)
      {
        ++val;
        parent = parent.parent;
      }
      return val;
    }
    public static boolean isValidPath(int myAS,GRIBTNode node)
    {
      boolean valid = true;
      if(node.parent == root)
      {
        if(myAS==node.AS)
            valid = false;
      } 
      else {
      GRIBTNode parent = node.parent;
      while (parent!=root)
      {
        if(parent.AS!=myAS)
        {    
          parent = parent.parent;
        }
        else
        {
          valid = false;
          break;
        }    
      }
      }
      return valid;
    }
    public static TIntArrayList getPath(GRIBTNode node)
    {
     if(node!=null)
     {    
     TIntArrayList path = new TIntArrayList();
     path.add(node.AS);
     GRIBTNode parent = node.parent;
     while (parent!=root)
      {
        path.add(parent.AS);
        parent = parent.parent;
      }
     return path;
     }
     else
     {
     return new TIntArrayList(); 
     }    
    }
    public static synchronized void increaseRefCount(GRIBTNode node)
    {
     if(phaseOne)
     {    
     ++(node.refCount);
     GRIBTNode parent = node.parent;
     while (parent!=root)
      {
       ++(parent.refCount);
       parent = parent.parent;
      }
     ++root.refCount; 
     }
    }
    public static synchronized void decreaseRefCount(GRIBTNode node)
    {
      if(phaseOne)
      {    
      --(node.refCount);
      GRIBTNode parent = node.parent;
     while (parent!=root)
      {
       --(parent.refCount);
       parent = parent.parent;
      }
     --root.refCount; 
     if(node.refCount==0 && node.getChilds().isEmpty()&& node!= root)
        removeChild(node.parent,node);
    }
    }
}
