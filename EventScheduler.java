import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class Event implements Serializable {
    String title, location, category;
    Date dateTime;

    public Event(String title, Date dateTime, String location, String category) {
        this.title = title;
        this.dateTime = dateTime;
        this.location = location;
        this.category = category;
    }
}

public class EventScheduler extends JFrame {
    private List<Event> eventList = new ArrayList<>();

    private JTextField titleField, locationField, dateField, timeField, searchField;
    private JComboBox<String> categoryBox, searchFilterBox;
    private JTable eventTable;
    private DefaultTableModel eventTableModel;

    private String[] categories = {"Meeting", "Personal", "Work", "Other"};

    public EventScheduler() {
        setTitle("Event Scheduler");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initUI();
        setVisible(true);

        // ✅ Reminder checker using javax.swing.Timer
        javax.swing.Timer reminderTimer = new javax.swing.Timer(60000, e -> checkReminders());
        reminderTimer.start();
    }

    private void initUI() {
        // Top panel: Event form
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        JPanel formPanel = new JPanel();
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Event"));

        formPanel.add(new JLabel("Title:"));
        titleField = new JTextField(10);
        formPanel.add(titleField);

        formPanel.add(new JLabel("Date (dd-MM-yyyy):"));
        dateField = new JTextField(10);
        formPanel.add(dateField);

        formPanel.add(new JLabel("Time (HH:mm):"));
        timeField = new JTextField(6);
        formPanel.add(timeField);

        formPanel.add(new JLabel("Location:"));
        locationField = new JTextField(10);
        formPanel.add(locationField);

        formPanel.add(new JLabel("Category:"));
        categoryBox = new JComboBox<>(categories);
        formPanel.add(categoryBox);

        JButton addBtn = new JButton("Add Event");
        addBtn.addActionListener(e -> addEvent());
        formPanel.add(addBtn);

        topPanel.add(formPanel);

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Events"));

        searchPanel.add(new JLabel("Search by:"));
        searchFilterBox = new JComboBox<>(new String[]{"Title", "Date", "Category"});
        searchPanel.add(searchFilterBox);

        searchField = new JTextField(15);
        searchPanel.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchEvents());
        searchPanel.add(searchBtn);

        JButton showAllBtn = new JButton("Show All");
        showAllBtn.addActionListener(e -> refreshEventTable());
        searchPanel.add(showAllBtn);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveEvents());
        searchPanel.add(saveBtn);

        JButton loadBtn = new JButton("Load");
        loadBtn.addActionListener(e -> loadEvents());
        searchPanel.add(loadBtn);

        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // Event Table
        eventTableModel = new DefaultTableModel(new Object[]{"Title", "Date", "Time", "Location", "Category"}, 0);
        eventTable = new JTable(eventTableModel);
        add(new JScrollPane(eventTable), BorderLayout.CENTER);
    }

    private void addEvent() {
        try {
            String title = titleField.getText().trim();
            String location = locationField.getText().trim();
            String dateStr = dateField.getText().trim();
            String timeStr = timeField.getText().trim();
            String category = (String) categoryBox.getSelectedItem();

            if (title.isEmpty() || dateStr.isEmpty() || timeStr.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            Date eventDateTime = sdf.parse(dateStr + " " + timeStr);

            for (Event e : eventList) {
                if (e.dateTime.equals(eventDateTime)) {
                    JOptionPane.showMessageDialog(this, "Event time overlaps with another event.");
                    return;
                }
            }

            Event event = new Event(title, eventDateTime, location, category);
            eventList.add(event);
            refreshEventTable();
            clearForm();

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Invalid date/time format.");
        }
    }

    private void refreshEventTable() {
        eventTableModel.setRowCount(0);
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");

        for (Event e : eventList) {
            eventTableModel.addRow(new Object[]{
                    e.title, dateFmt.format(e.dateTime), timeFmt.format(e.dateTime),
                    e.location, e.category
            });
        }
    }

    private void searchEvents() {
        String filter = (String) searchFilterBox.getSelectedItem();
        String keyword = searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) return;

        eventTableModel.setRowCount(0);
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");

        for (Event e : eventList) {
            boolean match = switch (filter) {
                case "Title" -> e.title.toLowerCase().contains(keyword);
                case "Date" -> dateFmt.format(e.dateTime).equals(keyword);
                case "Category" -> e.category.toLowerCase().contains(keyword);
                default -> false;
            };
            if (match) {
                eventTableModel.addRow(new Object[]{
                        e.title, dateFmt.format(e.dateTime), timeFmt.format(e.dateTime),
                        e.location, e.category
                });
            }
        }
    }

    private void checkReminders() {
        Date now = new Date();
        for (Event e : eventList) {
            long diff = e.dateTime.getTime() - now.getTime();
            if (diff > 0 && diff < 3600000) { // within next 60 minutes
                JOptionPane.showMessageDialog(this, "Upcoming Event Reminder:\n" +
                        e.title + " at " + new SimpleDateFormat("HH:mm").format(e.dateTime));
            }
        }
    }

    private void saveEvents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("events.dat"))) {
            oos.writeObject(eventList);
            JOptionPane.showMessageDialog(this, "Events saved.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save events.");
        }
    }

    private void loadEvents() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("events.dat"))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                eventList = (List<Event>) obj;
                refreshEventTable();
                JOptionPane.showMessageDialog(this, "Events loaded.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load events.");
        }
    }

    private void clearForm() {
        titleField.setText("");
        dateField.setText("");
        timeField.setText("");
        locationField.setText("");
        categoryBox.setSelectedIndex(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EventScheduler::new);
    }
}
