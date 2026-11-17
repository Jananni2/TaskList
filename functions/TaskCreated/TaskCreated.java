
import com.catalyst.Context;
import com.catalyst.event.EVENT_STATUS;
import com.catalyst.event.EventRequest;
import com.catalyst.event.CatalystEventHandler;
import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class TaskCreated implements CatalystEventHandler{
	
	private static final Logger LOGGER = Logger.getLogger(TaskCreated.class.getName());

	@Override
	public EVENT_STATUS handleEvent(EventRequest paramEventRequest, Context paramContext) throws Exception {
		
		try
		{
			ZCProject.initProject();

			 Object rawData = paramEventRequest.getRawData();

if (rawData != null) {
    ObjectMapper mapper = new ObjectMapper();

    JsonNode root = mapper.readTree(rawData.toString());

    JsonNode events = root.path("events");

     if (events.isArray() && events.size() > 0) {

      for (JsonNode event : events) {
    JsonNode taskData = event.path("data");

    String taskName = taskData.path("TaskName").asText();
    String rowId = taskData.path("ROWID").asText();
    String status = taskData.path("Status").asText();

    LOGGER.log(Level.INFO,
        "Task: {0}, Row ID: {1}, Status: {2}",
        new Object[]{taskName, rowId, status});
}
    }
}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error processing TaskCreated event", e);
		}
		
		return EVENT_STATUS.SUCCESS;
	}
}
