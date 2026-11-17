 
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.catalyst.advanced.CatalystAdvancedIOHandler;

import com.zc.component.object.ZCObject;
import com.zc.component.object.ZCRowObject;
import com.zc.component.object.ZCRowPagedResponse;
import com.zc.component.object.ZCTable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TaskManager implements CatalystAdvancedIOHandler {

    private static final Logger LOGGER =
            Logger.getLogger(TaskManager.class.getName());

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void runner(HttpServletRequest request,
                       HttpServletResponse response) throws Exception {

        try {

            if (!"/".equals(request.getRequestURI())) {
                response.setStatus(404);
                response.getWriter().write("Endpoint not found");
                return;
            }

            String method = request.getMethod();

            ZCObject object = ZCObject.getInstance();
            ZCTable table = object.getTable("Tasks");

            // CREATE TASK
            if ("POST".equalsIgnoreCase(method)) {

                JsonNode body = mapper.readTree(request.getReader());

                JsonNode taskNode =
                        body == null ? null : body.get("TaskName");

                if (taskNode == null || taskNode.isNull()
                        || !taskNode.isTextual()
                        || taskNode.asText().trim().isEmpty()) {

                    response.setStatus(400);
                    response.getWriter().write("Task name is required");
                    return;
                }

                String taskName = taskNode.asText().trim();

                ZCRowObject row = ZCRowObject.getInstance();
                row.set("TaskName", taskName);
row.set("Status", "Pending");
                table.insertRow(row);

                response.setStatus(201);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"message\":\"Task created successfully\"}"
                );
            }

            // GET ALL TASKS
            else if ("GET".equalsIgnoreCase(method)) {

                List<Map<String, Object>> tasks = new ArrayList<>();

                String nextToken = null;
                ZCRowPagedResponse pagedResponse;

                do {

                    pagedResponse = table.getPagedRows(nextToken, 100);

                    for (ZCRowObject row : pagedResponse.getRows()) {

                        Map<String, Object> task = new LinkedHashMap<>();

                       task.put("ROWID", row.get("ROWID"));
task.put("TaskName", row.get("TaskName"));

// Get task status
Object status = row.get("Status");

// Existing tasks may not have a status yet
if (status == null || status.toString().trim().isEmpty()) {
    status = "Pending";
}

task.put("Status", status);
                        tasks.add(task);
                    }

                    if (pagedResponse.moreRecordsAvailable()) {
                        nextToken = pagedResponse.getNextToken();
                    }

                } while (pagedResponse.moreRecordsAvailable());

                response.setStatus(200);
                response.setContentType("application/json");

                response.getWriter().write(
                        mapper.writeValueAsString(tasks)
                );
            }

            // DELETE TASK
            else if ("DELETE".equalsIgnoreCase(method)) {

                String id = request.getParameter("id");

                if (id == null || id.trim().isEmpty()) {
                    response.setStatus(400);
                    response.getWriter().write("Task ID is required");
                    return;
                }

                try {
                    Long rowId = Long.parseLong(id.trim());

                    table.deleteRow(rowId);

                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Task deleted successfully\"}"
                    );

                } catch (NumberFormatException e) {
                    response.setStatus(400);
                    response.getWriter().write("Invalid task ID");
                }
            }

            
else if ("PUT".equalsIgnoreCase(method)) {

    String id = request.getParameter("id");

    if (id == null || id.trim().isEmpty()) {
        response.setStatus(400);
        response.getWriter().write("Task ID is required");
        return;
    }

    JsonNode body = mapper.readTree(request.getReader());

    if (body == null || !body.isObject()) {
        response.setStatus(400);
        response.getWriter().write("Invalid request body");
        return;
    }

    try {
        Long rowId = Long.parseLong(id.trim());

        ZCRowObject row = ZCRowObject.getInstance();
        row.set("ROWID", rowId);

        boolean hasUpdate = false;

        // Update task name if provided
        JsonNode taskNode = body.get("TaskName");

        if (taskNode != null) {
            if (!taskNode.isTextual()
                    || taskNode.asText().trim().isEmpty()) {
                response.setStatus(400);
                response.getWriter().write("Invalid task name");
                return;
            }

            row.set("TaskName", taskNode.asText().trim());
            hasUpdate = true;
        }

        // Update status if provided
        JsonNode statusNode = body.get("Status");

        if (statusNode != null) {
            if (!statusNode.isTextual()
                    || !(statusNode.asText().equals("Pending")
                    || statusNode.asText().equals("Completed"))) {
                response.setStatus(400);
                response.getWriter().write("Invalid task status");
                return;
            }

            row.set("Status", statusNode.asText());
            hasUpdate = true;
        }

        if (!hasUpdate) {
            response.setStatus(400);
            response.getWriter().write("No valid fields to update");
            return;
        }

        List<ZCRowObject> rows = new ArrayList<>();
        rows.add(row);

        table.updateRows(rows);

        response.setStatus(200);
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"message\":\"Task updated successfully\"}"
        );

    } catch (NumberFormatException e) {
        response.setStatus(400);
        response.getWriter().write("Invalid task ID");
    }
}


        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, "Exception in TaskManager", e);

            response.setStatus(500);
            response.getWriter().write("Internal server error");
        }
    }
}
