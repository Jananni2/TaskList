 

const API_URL = "/server/sample/";

// Get HTML elements
const taskForm = document.getElementById("taskForm");
const taskInput = document.getElementById("taskInput");
const taskList = document.getElementById("taskList");

// Fetch and display all tasks
 
async function loadTasks() {
    try {
        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Failed to fetch tasks");
        }

        const tasks = await response.json();

        taskList.replaceChildren();

        tasks.forEach(task => {
            const li = document.createElement("li");

            const taskName = document.createElement("span");
            taskName.textContent = task.TaskName;
            // Task completion checkbox
const checkbox = document.createElement("input");
checkbox.type = "checkbox";
checkbox.checked = task.Status === "Completed";

checkbox.addEventListener("change", async () => {
    const newStatus = checkbox.checked ? "Completed" : "Pending";

    try {
        const response = await fetch(
            API_URL + "?id=" + encodeURIComponent(task.ROWID),
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    Status: newStatus
                })
            }
        );

        if (!response.ok) {
            throw new Error("Failed to update task status");
        }

        await loadTasks();

    } catch (error) {
        console.error(error);
        alert("Could not update task status");
        checkbox.checked = !checkbox.checked;
    }
});

            // Update button
            const editButton = document.createElement("button");
            editButton.textContent = "Edit";

            editButton.addEventListener("click", async () => {
                const updatedName = prompt("Enter updated task name:", task.TaskName);

                if (updatedName === null || !updatedName.trim()) {
                    return;
                }

                const response = await fetch(
                    API_URL + "?id=" + encodeURIComponent(task.ROWID),
                    {
                        method: "PUT",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            TaskName: updatedName.trim()
                        })
                    }
                );

                if (!response.ok) {
                    alert("Could not update task");
                    return;
                }

                await loadTasks();
            });

            // Delete button
            const deleteButton = document.createElement("button");
            deleteButton.textContent = "Delete";

            deleteButton.addEventListener("click", async () => {
                if (!confirm("Are you sure you want to delete this task?")) {
                    return;
                }

                const response = await fetch(
                    API_URL + "?id=" + encodeURIComponent(task.ROWID),
                    {
                        method: "DELETE"
                    }
                );

                if (!response.ok) {
                    alert("Could not delete task");
                    return;
                }

                await loadTasks();
            });

            li.appendChild(checkbox);
li.appendChild(taskName);
li.appendChild(editButton);
li.appendChild(deleteButton);

            taskList.appendChild(li);
        });

    } catch (error) {
        console.error("Error loading tasks:", error);
    }
}


// Add a new task
taskForm.addEventListener("submit", async function(event) {
    event.preventDefault();

    const taskName = taskInput.value.trim();

    if (!taskName) return;

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                TaskName: taskName
            })
        });

        if (!response.ok) {
            throw new Error("Failed to add task");
        }

        taskInput.value = "";

        await loadTasks();

    } catch (error) {
        console.error("Error adding task:", error);
    }
});

// Load tasks when the page opens
loadTasks();
