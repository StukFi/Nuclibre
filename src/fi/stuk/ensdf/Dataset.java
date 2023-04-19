/*
* Copyright (c) 2023 Radiation and Nuclear Safety Authority (STUK)
*
* Use of this source code is governed by an MIT-style
* license that can be found in the LICENSE file.
*/
package fi.stuk.ensdf;

import fi.stuk.ensdf.record.AlphaRecord;
import fi.stuk.ensdf.record.BetaRecord;
import fi.stuk.ensdf.record.Record;
import fi.stuk.ensdf.record.CommentRecord;
import fi.stuk.ensdf.record.CrossReferenceRecord;
import fi.stuk.ensdf.record.DelayedParticleRecord;
import fi.stuk.ensdf.record.ECRecord;
import fi.stuk.ensdf.record.EndRecord;
import fi.stuk.ensdf.record.GammaRecord;
import fi.stuk.ensdf.record.HistoryRecord;
import fi.stuk.ensdf.record.IdentificationRecord;
import fi.stuk.ensdf.record.LevelRecord;
import fi.stuk.ensdf.record.NormalizationRecord;
import fi.stuk.ensdf.record.ParentRecord;
import fi.stuk.ensdf.record.ProductionNormalizationRecord;
import fi.stuk.ensdf.record.QValueRecord;
import fi.stuk.ensdf.record.ReferenceRecord;
import fi.stuk.ensdf.type.HalfLifeValue;
import java.util.ArrayList;
import java.util.List;

/**
 The Evaluated Nuclear Structure Data File (ENSDF) is made up of a collection of
`data sets' which present one of the following kinds of information:
<ol>
<li> The summary information for a mass chain giving information, e.g., evaluators' names and aliations, cuto date, evaluators' remarks, and publication
details, etc.<p></li>
<li>The references used in all the data sets for the given mass number. This
data set is based upon reference codes (key numbers) used in various data
sets for a given mass number and is added to the file by the NNDC.<p></li>
<li>The adopted level and gamma-ray properties for each nuclide.<p></li>
<li>The evaluated results of a single type of experiment, e.g., a radioactive decay
or a nuclear reaction for a given nuclide.<p></li>
<li>The combined evaluated results of a number of experiments of the same
kind, e.g., (Heavy ion, xn), Coulomb excitation, etc. for a given nuclide.<p></li>
 * </ol>
 The data sets in ENSDF are organized by their mass number. Within a mass
number the data sets are of two kinds:
 <ul>
<li>Data sets which contain information pertaining to the complete mass chain.
These data sets contain information of the type (1) and (2) given above.<p></li>
<li>Data sets belonging to a given nuclide (Z-value).<p></li>
 * </ul>
Latter data sets, i.e. for a given nuclide (Z-value), consist of the following:
<ul>
<li>A Comments data set which gives abstract information for the nuclide. This
data set contains summary information as described in (1) above. This data
set exists only if the nuclide was evaluated or updated beyond the whole
mass chain was evaluated.<p></li>
<li>Adopted data set (only one per Z-value) giving adopted properties of the
levels and gamma rays seen in that nuclide.<p></li>
<li>Data sets giving information of the type (4) or (5) above.<p></li>
</ul>
 * @author Tero Karhunen
 */
public class Dataset {
    /** Identification record of this dataset. */
    protected IdentificationRecord identificationRecord;
    
    /** History record of this dataset. */
    protected HistoryRecord historyRecord;
    
    /** Comment record of this dataset. */
    protected CommentRecord commentRecord;
    
    /** Tabular comments in this dataset. */
    protected CommentRecord tabularComments;
    
    /** Normalization record of this dataset. */
    protected NormalizationRecord normalizationRecord;
    
    /** Production Normalization record of this dataset. */
    protected ProductionNormalizationRecord productionNormalizationRecord;
    
    /** QValue record of this dataset. */
    protected QValueRecord qValueRecord;
    
    /** Parent record of this dataset. */
    protected ParentRecord parentRecord;
    
    /** End record of this dataset. */
    protected EndRecord endRecord;
    
    /** Previously added record is stored. This is used to check if the 
     record should be continued with next addition */
    protected Record previousAdded = null;
    
    /** List of all records. */
    List<Record> records = new ArrayList<>();
    
    /** Origin of the dataset. */
    private String origin;
    
    /** The line in the datafile where this dataset begins. */
    protected int lineNro = -1;
    
     /** Flag indicating that this decay has been stored already. */
    boolean stored = false;        
    
    /**
     * Get a key for this dataset. The key can be used to store the dataset
     * into a hashtable.
     * @return the key.
     */
    public String getKey(){
       return(this.identificationRecord.getNUCID());
    }
    /**
     * Create a suitable dataset for a given identification record
     * @param r the identification record
     * @return the dataset
     */
    public static Dataset createDataset(IdentificationRecord r, String origin){
        String dsid = r.getDSID();        
        Dataset d = null;
        if(dsid.equals("ADOPTED LEVELS, GAMMAS"))d=new AdoptedDataset(r);
        else if(dsid.startsWith("ADOPTED LEVELS"))d=new AdoptedDataset(r);
        else if(dsid.contains("DECAY"))d=new Decay(r);
        else d=new Reaction(r);
        d.setOrigin(origin);
        return(d);
    }
    
    /**
     * Get the records in this dataset.
     * @return the records.
     */
    public List<Record> getRecords(){
        return(records);
    }

    /**
     * Create a suitable record based on two given record type character codes.
     * @param code the first record type character code (typically in position 8
     * of the line describing the record)
     * @param code2 the second record type character code, either blank or a 
     * character in position 9 of the line describing the record.
     * @param line
     * @return the record corresponding to the given codes.
     * @throws Exception if the codes don't match any record.
     */
    public Record createRecord(char code, char code2, String line) throws Exception{
        if(code == 'H')return(new HistoryRecord());        
        else if(code == 'C')return(new CommentRecord(code));
        else if(code == 'c')return(new CommentRecord(code));
        else if(code == 'T' || code2 == 'T')return(new CommentRecord(code));
        else if(code == 'u')return(new CommentRecord(code));
        else if(code == 'U')return(new CommentRecord(code));
        else if(code == 'G' && code2 == 'P')return(new CommentRecord(code));
        else if(code == 'G')return(new GammaRecord());
        else if(code == 'B')return(new BetaRecord());
        else if(code == 'E')return(new ECRecord());
        else if(code == 'N' && code2 == 'P')return(new ProductionNormalizationRecord());
        else if(code == 'N')return(new NormalizationRecord());
        else if(code == 'Q')return(new QValueRecord());
        else if(code == 'D')return(new DelayedParticleRecord());
        else if(code == 'L' && code2 == 'P')return(new CommentRecord(code));
        else if(code == 'L')return(new LevelRecord());
        else if(code == 'P'){
            return(new ParentRecord());
        }
        else if(code == 'X')return(new CrossReferenceRecord());
        else if(code == 'A')return(new AlphaRecord());
        else if(code == 'R')return(new ReferenceRecord());        
        else if(code == ' ' && Character.isDigit(line.charAt(5)) && this.identificationRecord.isContinuation())return(this.identificationRecord);
        else if(code == ' ')return(new CommentRecord('c'));        
        throw new IllegalArgumentException("No record with code '"+code+"'.");
    }

    /**
     * Add a record to this dataset.
     * @param r the record to add.
     */
    public void addRecord(Record r){        
        if(r instanceof HistoryRecord)this.historyRecord = (HistoryRecord)r;
        else if(r instanceof CommentRecord){
//            CommentRecord cr = (CommentRecord)r;
//            //System.out.println("Add comment:\n"+cr.getCTEXT());
//            if(cr.getCode() == 'T' || cr.getCode() == 't'){
//                if(tabularComments == null)tabularComments = cr;
//                else{
//                    print = false;
//                    appendComment(tabularComments, cr);
//                }
//            }
//            else this.commentRecord = cr;
        }
        else if(r instanceof ParentRecord)setParentRecord((ParentRecord)r);
        else if(r instanceof IdentificationRecord)this.identificationRecord = (IdentificationRecord)r;        
        else if(r instanceof ProductionNormalizationRecord){
            //if(this.productionNormalizationRecord != null)throw new RuntimeException("Attempt to overwrite production normalization record!");
            this.productionNormalizationRecord = (ProductionNormalizationRecord)r;
        }
        else if(r instanceof NormalizationRecord){
            //if(this.normalizationRecord != null)throw new RuntimeException("Attempt to overwrite normalization record!");
            this.normalizationRecord = (NormalizationRecord)r;
        }
        else if(r instanceof QValueRecord)this.qValueRecord = (QValueRecord)r;
        
        else this.records.add(r);
    }

    /**
     * Continue a record already added to this dataset. Continuation records are
     * used for comment records.
     * @param r the record.
     */
    public void continueRecord(Record r){        
        if(r instanceof HistoryRecord)this.historyRecord = (HistoryRecord)r;
        else if(r instanceof CommentRecord){
//            r.parse();
//            //System.out.println("Append comment with;\n"+((CommentRecord)r).getCTEXT());
//            appendComment(commentRecord, ((CommentRecord)r));
            //System.out.println("Comment appended:\n"+commentRecord.getCTEXT());
        }
        else if(r instanceof GammaRecord){
            GammaRecord prev = (GammaRecord)this.records.get(records.size()-1);            
            prev.setContent(prev.getContent()+"\n"+r.getContent());            
            prev.parseContinuation();                        
        }
        else if(r instanceof ECRecord){
            ECRecord prev = (ECRecord)this.records.get(records.size()-1);            
            prev.setContent(prev.getContent()+"\n"+r.getContent());            
            prev.parseContinuation();                        
        }
        else if(r instanceof BetaRecord){
            BetaRecord prev = (BetaRecord)this.records.get(records.size()-1);            
            prev.setContent(prev.getContent()+"\n"+r.getContent());            
            prev.parseContinuation();                        
        }
    }

    /**
     * Append comments to a given comment record.
     * @param c1 the comment record to append to
     * @param c2 the comment record to append
     */
    private void appendComment(CommentRecord c1, CommentRecord c2){
        if(c1 == null){
            c1 = new CommentRecord('C');
            commentRecord = c1;
        }
            String s = c1.getCTEXT();
            s += "\n"+c2.getCTEXT();
            c1.setCTEXT(s);
    }

        @Override
    public String toString(){
        return(this.identificationRecord.getNUCID() + " " +this.identificationRecord.getDSID());
    }
    
    /**
     * Get records of given type.
     * @param <T> the type.
     * @param type the type.
     * @return list of records of given type.
     */
    public <T> List<T> getRecordsOfType(Class<T> type){
        List<T> ret = new ArrayList<>();
        for(Record r : records){
            if(r.getClass().isAssignableFrom(type))ret.add((T)r);
        }
        return(ret);
    }

    /**
     * @return the identificationRecord
     */
    public IdentificationRecord getIdentificationRecord() {
        return identificationRecord;
    }

    /**
     * @param identificationRecord the identificationRecord to set
     */
    public void setIdentificationRecord(IdentificationRecord identificationRecord) {
        this.identificationRecord = identificationRecord;
    }

    /**
     * @return the historyRecord
     */
    public HistoryRecord getHistoryRecord() {
        return historyRecord;
    }

    /**
     * @param historyRecord the historyRecord to set
     */
    public void setHistoryRecord(HistoryRecord historyRecord) {
        this.historyRecord = historyRecord;
    }

    /**
     * @return the commentRecord
     */
    public CommentRecord getCommentRecord() {
        return commentRecord;
    }

    /**
     * @param commentRecord the commentRecord to set
     */
    public void setCommentRecord(CommentRecord commentRecord) {
        this.commentRecord = commentRecord;
    }

    /**
     * @return the tabularComments
     */
    public CommentRecord getTabularComments() {
        return tabularComments;
    }

    /**
     * @param tabularComments the tabularComments to set
     */
    public void setTabularComments(CommentRecord tabularComments) {
        this.tabularComments = tabularComments;
    }

    /**
     * @return the normalizationRecord
     */
    public NormalizationRecord getNormalizationRecord() {
        return normalizationRecord;
    }

    /**
     * @param normalizationRecord the normalizationRecord to set
     */
    public void setNormalizationRecord(NormalizationRecord normalizationRecord) {
        this.normalizationRecord = normalizationRecord;
    }

    /**
     * @return the qValueRecord
     */
    public QValueRecord getqValueRecord() {
        return qValueRecord;
    }

    /**
     * @param qValueRecord the qValueRecord to set
     */
    public void setqValueRecord(QValueRecord qValueRecord) {
        this.qValueRecord = qValueRecord;
    }

    /**
     * @return the parentRecord
     */
    public ParentRecord getParentRecord() {
        return parentRecord;
    }

    /**
     * @param parentRecord the parentRecord to set
     */
    public void setParentRecord(ParentRecord parentRecord) {
        if(this.parentRecord == null || this.parentRecord.getE() == null
                || this.parentRecord.getT() == null)this.parentRecord = parentRecord;
    }

    /**
     * @return the endRecord
     */
    public EndRecord getEndRecord() {
        return endRecord;
    }

    /**
     * @param endRecord the endRecord to set
     */
    public void setEndRecord(EndRecord endRecord) {
        this.endRecord = endRecord;
    }

    /**
     * @return the previousAdded
     */
    public Record getPreviousAdded() {
        return previousAdded;
    }

    /**
     * @param previousAdded the previousAdded to set
     */
    public void setPreviousAdded(Record previousAdded) {
        this.previousAdded = previousAdded;
    }
    
    /**
     * Get the dataset begin line number in the datafile.
     * @return the line number. 
     */
    public int getLineNro(){
        return(this.lineNro);
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    /**
     * Set the stored state of this decay.
     * @param stored the stored state.
     */
    public void setStored(boolean stored){
        this.stored = stored;
    }
    
    /**
     * Get the stored state of this decay.
     * @return the stored state.
     */
    public boolean isStored(){
        return(stored);
    }
}
