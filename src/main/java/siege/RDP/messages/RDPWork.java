package siege.RDP.messages;

public class RDPWork extends IdentifiableMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1634619331626222859L;
	public int RDPId;
	public int parentSegmentID;
	public int segmentID;
	public int segmentStartIndex;
	public int endIndex;
	public int partition_ancestors;
	
	public RDPWork(int RDPId, int segmentID, int parentSegmentID, int startIndex, int endIndex, int partition_ancestors) {
		 this.RDPId = RDPId;
		 this.segmentID = segmentID;
		 this.segmentStartIndex = startIndex;
		 this.endIndex = endIndex;
		 this.parentSegmentID = parentSegmentID;
		 this.partition_ancestors = partition_ancestors;
		 
	}
	
	@Override
	public String toString(){
		return String.format("%d:%d:%d\t\t%d-%d", RDPId, parentSegmentID, segmentID, segmentStartIndex, endIndex);
	}
	
	public int getPartitionID(String partition){
		String[] segments = partition.split("-");
		return Integer.parseInt(segments[1]);
	}
	
	public String createNewPartitionString(){
		return String.format("%d-%d", RDPId, segmentID);
	}
	
	
}
