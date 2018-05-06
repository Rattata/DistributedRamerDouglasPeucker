package siege.RDP.node;

import java.util.Stack;

import com.google.inject.Inject;

import siege.RDP.data.RMIManager;
import siege.RDP.registrar.ISegmentIDGenerator;

public class SegmentIDManager {
	Stack<Integer> unusedIDS = new Stack<>();
	ISegmentIDGenerator idGen;
	
	@Inject
	public SegmentIDManager(RMIManager rmiman) {
		this.idGen = rmiman.getIDGen();
		try {
			unusedIDS.addAll(idGen.getRange(2000));			
		} catch (Exception e) {
			e.getStackTrace();
		}
	}
	
	public Integer next(){
		Integer res = 0;
		try {
			if(unusedIDS.empty()){
				unusedIDS.addAll(idGen.getRange(2000));
			}			
			res = unusedIDS.pop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
}
