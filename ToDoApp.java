import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ToDoApp extends JFrame {
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private static final String FILE_NAME = "tasks.dat";

    public ToDoApp() {
        setTitle("To-Do List Application");
        setSize(350, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Set background color of the application
        getContentPane().setBackground(new Color(255, 229, 204)); // Light orange

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 229, 204)); // Light orange
        JLabel heading = new JLabel("To-Do List", SwingConstants.CENTER);
        heading.setFont(new Font("Times New Roman", Font.BOLD, 24));
        topPanel.add(heading, BorderLayout.WEST);

        JComboBox<String> viewDropdown = new JComboBox<>(new String[]{"All", "Completed", "Incomplete"});
        viewDropdown.setPreferredSize(new Dimension(100, 50));
        viewDropdown.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        viewDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                viewTasks(viewDropdown.getSelectedItem().toString());
            }
        });
        topPanel.add(viewDropdown, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        taskListModel = new DefaultListModel<>();
        loadTasks();

        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new TaskListRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setVisibleRowCount(10);
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = taskList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Task task = taskListModel.getElementAt(index);
                        task.setComplete(!task.isComplete());
                        taskListModel.setElementAt(task, index);
                        saveTasks();
                        taskList.repaint(); // Ensure the list repaints after updating
                    }
                }
            }
        });
        JScrollPane taskScrollPane = new JScrollPane(taskList);
        add(taskScrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("+");
        addButton.setFont(new Font("Times New Roman", Font.BOLD, 24));
        addButton.setBackground(new Color(173, 216, 230)); // Light blue
        addButton.setFocusPainted(false);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        JButton updateButton = new JButton("Update");
        updateButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        updateButton.setBackground(new Color(173, 216, 230)); // Light blue
        updateButton.setFocusPainted(false);
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = taskList.getSelectedIndex();
                if (index >= 0) {
                    updateTask(index);
                } else {
                    JOptionPane.showMessageDialog(ToDoApp.this, "Please select a task to update.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        deleteButton.setBackground(new Color(173, 216, 230)); // Light blue
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = taskList.getSelectedIndex();
                if (index >= 0) {
                    deleteTask(index);
                } else {
                    JOptionPane.showMessageDialog(ToDoApp.this, "Please select a task to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(255, 229, 204)); // Light orange
        bottomPanel.add(updateButton);
        bottomPanel.add(addButton);
        bottomPanel.add(deleteButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addTask() {
        String taskDescription = JOptionPane.showInputDialog(this, "Enter task description:");
        if (taskDescription != null && !taskDescription.trim().isEmpty()) {
            Task newTask = new Task(taskDescription.trim());
            taskListModel.addElement(newTask);
            saveTasks();
        } else if (taskDescription != null) {
            JOptionPane.showMessageDialog(this, "Task description cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTask(int index) {
        String taskDescription = JOptionPane.showInputDialog(this, "Update task description:", taskListModel.getElementAt(index).getDescription());
        if (taskDescription != null && !taskDescription.trim().isEmpty()) {
            Task task = taskListModel.getElementAt(index);
            task.setDescription(taskDescription.trim());
            taskListModel.setElementAt(task, index);
            saveTasks();
        } else if (taskDescription != null) {
            JOptionPane.showMessageDialog(this, "Task description cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTask(int index) {
        taskListModel.remove(index);
        saveTasks();
    }

    private void viewTasks(String filter) {
        DefaultListModel<Task> filteredTasks = new DefaultListModel<>();
        for (int i = 0; i < taskListModel.size(); i++) {
            Task task = taskListModel.getElementAt(i);
            if ("All".equals(filter) || ("Completed".equals(filter) && task.isComplete()) || ("Incomplete".equals(filter) && !task.isComplete())) {
                filteredTasks.addElement(task);
            }
        }
        taskList.setModel(filteredTasks);
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            List<Task> tasks = new ArrayList<>();
            for (int i = 0; i < taskListModel.size(); i++) {
                tasks.add(taskListModel.getElementAt(i));
            }
            oos.writeObject(tasks);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            @SuppressWarnings("unchecked")
            List<Task> tasks = (List<Task>) ois.readObject();
            taskListModel.clear();
            for (Task task : tasks) {
                taskListModel.addElement(task);
            }
        } catch (FileNotFoundException e) {
            // No tasks to load
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class TaskListRenderer extends JPanel implements ListCellRenderer<Task> {
        private JLabel taskLabel;

        public TaskListRenderer() {
            setLayout(new BorderLayout());
            setOpaque(true);
            setPreferredSize(new Dimension(300, 30)); // Set size for each item
            taskLabel = new JLabel();
            taskLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));
            taskLabel.setPreferredSize(new Dimension(280, 30));
            add(taskLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task value, int index, boolean isSelected, boolean cellHasFocus) {
            taskLabel.setText(value.getDescription());
            setBackground(value.isComplete() ? new Color(144, 238, 144) : new Color(255, 182, 193)); // Light green for complete, light red for incomplete
            if (isSelected) {
                setBackground(Color.CYAN);
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ToDoApp().setVisible(true);
            }
        });
    }
}

class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private boolean complete;

    public Task(String description) {
        this.description = description;
        this.complete = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public String toString() {
        return description;
    }
}
