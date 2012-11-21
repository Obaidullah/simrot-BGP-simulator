/*
 * Message.java
 *
 * Created on July 3, 2007, 3:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package pvsimulator;


import gnu.trove.*;

/**
 *
 * @author ahmed
 */
public class MessageV2 extends TLinkableAdapter{
    private int type;
    private int src;
    private TIntArrayList withdrawans=new TIntArrayList();
    private TIntArrayList announced = new TIntArrayList();
    private int withdrawanLength;
    private GRIBTNode path;
    private int announcedPrefix;
    private int IID;
    private int affectedNeighbor;
    private int pathAttrLength;
    private int dst;
    /** Creates a new instance of Message */
    public MessageV2() {
    }
    public MessageV2(int type,int src,int dst,TIntArrayList withdrawans,int pathAttrLength,GRIBTNode path,int announcedPrefix){
        this.type = type;
        this.src = src;
        this.dst = dst;
        this.withdrawans = withdrawans;
        withdrawanLength = withdrawans.size();
        this.pathAttrLength = pathAttrLength;
        this.path = path;
        this.announcedPrefix = announcedPrefix;
    }
    public MessageV2(int type,int src,int dst,TIntArrayList withdrwans) {
        
        this.type = type;
        this.src = src;
        this.dst = dst;
        this.withdrawans = withdrwans;

        withdrawanLength = withdrawans.size();

    }

    public MessageV2(int type,int src,int dst,int pathAttrLength,GRIBTNode path,int announced_prefix){
      this.type = type;
      this.src = src;
      this.dst = dst;
      withdrawanLength = 0;
      this.pathAttrLength = pathAttrLength;
      this.path = path;
      this.announcedPrefix = announced_prefix;

    }
    public MessageV2(int type,int src,int dst)
    {
      this.type = type;
      this.src = src;
      this.dst = dst;
    }
 
    public int getAffectedNeighbor() {
        return affectedNeighbor;
    }

    public int getAnnouncedPrefix() {
        return announcedPrefix;
    }

    public int getIID() {
        return IID;
    }

    public GRIBTNode getPath() {
        return path;
    }

    public int getPathAttrLength() {
        return pathAttrLength;
    }

    public int getSrc() {
        return src;
    }

    public int getType() {
        return type;
    }

    public int getWithdrawanLength() {
        return withdrawanLength;
    }

    public TIntArrayList getWithdrawans() {
        return withdrawans;
    }

    public void setAnnouncedPrefix(int announcedPrefix) {
        this.announcedPrefix = announcedPrefix;
    }

    public void setPath(GRIBTNode path) {
        this.path = path;
    }

    public void setPathAttrLength(int pathAttrLength) {
        this.pathAttrLength = pathAttrLength;
    }

    public void setWithdrawans(TIntArrayList withdrawans) {
        this.withdrawans = withdrawans;
    }

    public void setAnnounced(TIntArrayList announced) {
        this.announced = announced;
    }

    public TIntArrayList getAnnounced() {
        return announced;
    }

    public String toString() {
        String result="";
        if(this.type == Values.updateMessage)
            result = "Msg_Type=update_msg,src"+this.src+",Withdrawan_List="+this.withdrawans.toString()+", PathAttrLength = "+this.pathAttrLength+",AnnouncedPrefix = "+this.announcedPrefix+",Reachability = "+this.path;
        if(this.type==Values.interruptMessage)
            result="Msg_Type=interrupt_msg,IID="+this.getIID()+",Affected_neigh="+this.affectedNeighbor;
        return result;
    }

    public int getDst() {
        return dst;
    }

    public void setDst(int dst) {
        this.dst = dst;
    }

    
    public boolean equals(Object object) {
        if(((Message)object).getAnnouncedPrefix()==announcedPrefix)
            return true;
        else
            return false;
    }

}
