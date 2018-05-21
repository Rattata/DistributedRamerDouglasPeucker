package siege.RDP.messages;

import java.util.Random;

public class RDPWork extends IdentifiableMessage {
	public int RDPId;
	public int parentSegmentID;
	public int segmentID;
	public int segmentStartIndex;
	public int endIndex;
	public int partition_ancestors;
	
	public RDPWork(int RDPId, int segmentID, int parentSegmentID, int segmentStartIndex, int endIndex, int partition_ancestors) {
		 this.RDPId = RDPId;
		 this.segmentID = segmentID;
		 this.segmentStartIndex = segmentStartIndex;
		 this.endIndex = endIndex;
		 this.parentSegmentID = parentSegmentID;
		 this.partition_ancestors = partition_ancestors;
		 
	}
	
	@Override
	public String Identifier(){
		return String.format("%d:%d:%d", RDPId, parentSegmentID, segmentID);
	}
	
	public int getPartitionID(String partition){
		String[] segments = partition.split("-");
		return Integer.parseInt(segments[1]);
	}
	
	public String createNewPartitionString(){
		Random r = new Random();
		int randomInt = r.nextInt(2048);
		return String.format("%d-%d", RDPId, randomInt);
	}
	
	
}
