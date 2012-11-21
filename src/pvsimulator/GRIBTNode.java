/*
 * GRIBTNode.java
 *
 * Created on July 31, 2007, 2:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;
import gnu.trove.TIntArrayList;
import gnu.trove.TLinkableAdapter;
import gnu.trove.TLinkedList;
import gnu.trove.TShortArrayList;
/**
 * A node in the GRIB tree structure 
 * @author ahmed
 */
public class GRIBTNode extends TLinkableAdapter{
    
    /** Creates a new instance of GRIBTNode */
    public GRIBTNode parent;
    public int refCount=0;
    public int AS;
    private TLinkedList childs = new TLinkedList();
    public GRIBTNode() {
      this.parent = null;
    }
    public GRIBTNode(GRIBTNode parent) {
      this.parent = parent;
    }
    public GRIBTNode(GRIBTNode parent,int AS) {
      this.parent = parent;
      this.AS = AS;
    }
    public TLinkedList getChilds() {
        return childs;
    }

    public boolean equals(Object object) {    
        TIntArrayList myPath = GRIB.getPath(this);
        TIntArrayList parameterPath = GRIB.getPath((GRIBTNode)object);
        if(myPath.equals(parameterPath))
            return true;
        else
            return false;
    }

    public String toString() {
        return ""+this.AS;
    }


   
   
}
