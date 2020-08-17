package stockholm.bicycles.resultanalysis;

import java.util.Arrays;
import java.util.HashMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.charts.XYLineChart;

public class LinkVolumeHandler implements LinkEnterEventHandler {

	HashMap<Id<Link>, double[]> LinkVolume;
	
	public LinkVolumeHandler() {
		reset(0);
	}
	
	
    public HashMap<Id<Link>, double[]> getLinkVolume() {
		return LinkVolume;
	}
	
	
	private int getSlot(double time){
		int timeHour=(int) (time/3600);
		if (timeHour>=24) {
			timeHour=timeHour-24;
		}
		return timeHour;
	}
	
	
	public void writeChart(Id<Link> linkID, String filename) {
		double[] hours = new double[24];
		for (double i = 0.0; i < 24.0; i++){
			hours[(int)i] = i;
		}
		
		
		XYLineChart chart = new XYLineChart(linkID.toString(), "hour", "departures");
		chart.addSeries("times", hours, this.LinkVolume.get(linkID));
		chart.saveAsPng(filename, 800, 600);
	}
	
	
	
	@Override
	public void reset(int iteration) {
		this.LinkVolume = new HashMap<Id<Link>, double[]>();
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		// TODO Auto-generated method stub
		
		Id<Link> linkID = event.getLinkId();
		
		if (this.LinkVolume.containsKey(linkID)) {
			double[] oneLinkVolume = this.LinkVolume.get(linkID);
			int hour=getSlot(event.getTime());
			oneLinkVolume[hour]=oneLinkVolume[hour]+1;
			this.LinkVolume.put(linkID,oneLinkVolume);
		} else {
			double[] oneLinkVolume = new double [24];
			Arrays.fill(oneLinkVolume, 0.0); 
			int hour=getSlot(event.getTime());
			oneLinkVolume[hour]=oneLinkVolume[hour]+1;
			this.LinkVolume.put(linkID, oneLinkVolume);
		}
	}

}
