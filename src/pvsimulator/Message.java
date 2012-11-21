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
 * This Class represents the control messages used by the routing protocol 
 * @author ahmed
 */
public class Message extends TLinkableAdapter{
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
    private boolean isPartOfTableTransfer = false;
    /** Creates a new instance of Message */
    public Message() {
    }
    /** Creates a new instance of Message-Update Message contains both announcements and withdrawals*/
    public Message(int type,int src,TIntArrayList withdrawans,int pathAttrLength,GRIBTNode path,int announcedPrefix){
        this.type = type;
        this.src = src;
        this.withdrawans = withdrawans;
        withdrawanLength = withdrawans.size();
        this.pathAttrLength = pathAttrLength;
        this.path = path;
        this.announcedPrefix = announcedPrefix;
    }
   /** Creates a new instance of Message-Update Message contains only withdrawals*/
    public Message(int type,int src,TIntArrayList withdrwans) {
        
        this.type = type;
        this.src = src;
        this.withdrawans = withdrwans;
        withdrawanLength = withdrawans.size();

    }
    /** Creates a new instance of Message-Update Message contains only announcements*/
    public Message(int type,int src,int pathAttrLength,GRIBTNode path,int announced_prefix){
      this.type = type;
      this.src = src;
      withdrawanLength = 0;
      this.pathAttrLength = pathAttrLength;
      this.path = path;
      this.announcedPrefix = announced_prefix;

    }
    /** Creates a new instance of Message-Update Message contains only announcements*/
    public Message(int type,int src,int pathAttrLength,GRIBTNode path,int announced_prefix,boolean tableTransfer){
      this.type = type;
      this.src = src;
      withdrawanLength = 0;
      this.pathAttrLength = pathAttrLength;
      this.path = path;
      this.announcedPrefix = announced_prefix;
      this.isPartOfTableTransfer = tableTransfer; 
    }
    public Message(int type,int src)
    {
      this.type = type;
      this.src = src;
    }
    /** Creates a new instance of Message-Interrupt message*/
    public Message(int type,int IID,int affectedNeighbor) {
    this.type = type;
    this.IID = IID;
    this.affectedNeighbor = affectedNeighbor;
    }
    /**
     * Returns the neighbor affected by the interrupt.
     */
    public int getAffectedNeighbor() {
        return affectedNeighbor;
    }
    /**
     * Returns the destination prefix announced in the message 
     */
    public int getAnnouncedPrefix() {
        return announcedPrefix;
    }
    /**
     *  Returns the Interrupt ID
     */
    public int getIID() {
        return IID;
    }
    /**
     *  Returns the AS-PATH for the destination prefix announced in the message
     */
    public GRIBTNode getPath() {
        return path;
    }
    /**
     *  Returns the path attributes length
     */
    public int getPathAttrLength() {
        return pathAttrLength;
    }
    /**
     * Returns the source of the message
     */
    public int getSrc() {
        return src;
    }
    /**
     *  Returns the type of the message
     */
    public int getType() {
        return type;
    }
    /**
     * Returns how many prefix are withdrawans by this message
     */
    public int getWithdrawanLength() {
        return withdrawanLength;
    }
    /**
     * Returns the list of the withdrawans prefixes
     */
    public TIntArrayList getWithdrawans() {
        return withdrawans;
    }
    /**
     *  Set the announced prefix
     */
    public void setAnnouncedPrefix(int announcedPrefix) {
        this.announcedPrefix = announcedPrefix;
    }

    public boolean isIsPartOfTableTransfer() {
        return isPartOfTableTransfer;
    }
    
    /**
     *  Set the AS-PATH
     */
    public void setPath(GRIBTNode path) {
        this.path = path;
    }

    public void setIsPartOfTableTransfer(boolean isPartOfTableTransfer) {
        this.isPartOfTableTransfer = isPartOfTableTransfer;
    }

    /**
     *  Set AS-PATH attributes length.
     */ 
    public void setPathAttrLength(int pathAttrLength) {
        this.pathAttrLength = pathAttrLength;
    }
    /**
     *  Set Withdrawans
     */ 
    public void setWithdrawans(TIntArrayList withdrawans) {
        this.withdrawans = withdrawans;
    }
    /**
     *  Set announced
     */
    public void setAnnounced(TIntArrayList announced) {
        this.announced = announced;
    }
    /**
     *  Get announced
     */
    public TIntArrayList getAnnounced() {
        return announced;
    }
    /**
     * Returns a string representation for the message 
     */
    public String toString(int dst) {
        String result="";
        if(this.type == Values.updateMessage)
        {
            result = "Src="+this.src+"\tDst="+dst+"\tWDL="+this.withdrawans.toString()+"\tPrefix = "+this.announcedPrefix+"\t PATH="+GRIB.getPath(path).toString();
        }
        if(this.type==Values.interruptMessage)
            result="Msg_Type=interrupt_msg,IID="+this.getIID()+",Affected_neigh="+this.affectedNeighbor;
        return result;
    }
    /**
     * Override Object class equals method
     */
    public boolean equals(Object object) {
        if(((Message)object).getAnnouncedPrefix()==announcedPrefix)
            return true;
        else
            return false;
    }

}
