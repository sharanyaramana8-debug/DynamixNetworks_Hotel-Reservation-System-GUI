import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Simple Swing GUI for Hotel Reservation System
 */
public class HotelReservationGUI extends JFrame {

    private HotelManager manager = new HotelManager();

    // Table models
    private DefaultTableModel roomsModel;
    private DefaultTableModel reservationsModel;
    private JTable roomsTable;
    private JTable reservationsTable;

    public HotelReservationGUI() {
        setTitle("Hotel Reservation System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        setVisible(true);
    }

    private void initUI() {
        JTabbedPane tabs = new JTabbedPane();

        // --- Rooms Tab ---
        JPanel roomsPanel = new JPanel(new BorderLayout());
        String[] roomCols = { "Room ID", "Type", "Price", "Status" };
        roomsModel = new DefaultTableModel(roomCols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        roomsTable = new JTable(roomsModel);
        refreshRoomsTable();
        roomsPanel.add(new JScrollPane(roomsTable), BorderLayout.CENTER);

        JPanel roomsControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField roomIdField = new JTextField(6);
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "Single", "Double", "Suite" });
        JTextField priceField = new JTextField(6);
        JButton addRoomBtn = new JButton("Add Room");
        JButton removeRoomBtn = new JButton("Remove Selected Room");

        roomsControl.add(new JLabel("Room ID:"));
        roomsControl.add(roomIdField);
        roomsControl.add(new JLabel("Type:"));
        roomsControl.add(typeBox);
        roomsControl.add(new JLabel("Price:"));
        roomsControl.add(priceField);
        roomsControl.add(addRoomBtn);
        roomsControl.add(removeRoomBtn);

        roomsPanel.add(roomsControl, BorderLayout.SOUTH);

        addRoomBtn.addActionListener(e -> {
            String id = roomIdField.getText().trim();
            String type = (String) typeBox.getSelectedItem();
            String priceText = priceField.getText().trim();
            if (id.isEmpty() || priceText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Room ID and Price.");
                return;
            }
            try {
                double price = Double.parseDouble(priceText);
                boolean ok = manager.addRoom(new Room(id, type, price, true));
                if (!ok)
                    JOptionPane.showMessageDialog(this, "Room ID already exists.");
                else {
                    refreshRoomsTable();
                    roomIdField.setText("");
                    priceField.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price.");
            }
        });

        removeRoomBtn.addActionListener(e -> {
            int sel = roomsTable.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(this, "Select a room to remove.");
                return;
            }
            String roomId = (String) roomsModel.getValueAt(sel, 0);
            boolean ok = manager.removeRoom(roomId);
            if (!ok)
                JOptionPane.showMessageDialog(this, "Cannot remove room (it may have reservations).");
            else
                refreshRoomsTable();
        });

        // --- Booking Tab ---
        JPanel bookingPanel = new JPanel(new BorderLayout());
        JPanel bookForm = new JPanel(new GridLayout(6, 2, 8, 8));
        JComboBox<String> availableRoomsBox = new JComboBox<>();
        refreshAvailableRoomsCombo(availableRoomsBox);
        JTextField custNameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField checkInField = new JTextField("2025-01-01");
        JTextField checkOutField = new JTextField("2025-01-02");
        JButton bookBtn = new JButton("Book Room");

        bookForm.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bookForm.add(new JLabel("Available Room:"));
        bookForm.add(availableRoomsBox);
        bookForm.add(new JLabel("Customer Name:"));
        bookForm.add(custNameField);
        bookForm.add(new JLabel("Phone:"));
        bookForm.add(phoneField);
        bookForm.add(new JLabel("Check-in (YYYY-MM-DD):"));
        bookForm.add(checkInField);
        bookForm.add(new JLabel("Check-out (YYYY-MM-DD):"));
        bookForm.add(checkOutField);
        bookForm.add(new JLabel(""));
        bookForm.add(bookBtn);

        bookingPanel.add(bookForm, BorderLayout.NORTH);

        bookBtn.addActionListener(e -> {
            String roomId = (String) availableRoomsBox.getSelectedItem();
            if (roomId == null) {
                JOptionPane.showMessageDialog(this, "No available rooms.");
                return;
            }
            String name = custNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String in = checkInField.getText().trim();
            String out = checkOutField.getText().trim();
            if (name.isEmpty() || phone.isEmpty() || in.isEmpty() || out.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }
            try {
                LocalDate checkIn = LocalDate.parse(in);
                LocalDate checkOut = LocalDate.parse(out);
                if (!checkOut.isAfter(checkIn)) {
                    JOptionPane.showMessageDialog(this, "Check-out must be after check-in.");
                    return;
                }
                Reservation res = manager.bookRoom(roomId, name, phone, checkIn, checkOut);
                if (res == null) {
                    JOptionPane.showMessageDialog(this, "Booking failed. Room not available.");
                } else {
                    JOptionPane.showMessageDialog(this, "Booked! Reservation ID: " + res.getReservationId());
                    custNameField.setText("");
                    phoneField.setText("");
                    refreshReservationsTable();
                    refreshRoomsTable();
                    refreshAvailableRoomsCombo(availableRoomsBox);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.");
            }
        });

        // --- Reservations Tab ---
        JPanel resPanel = new JPanel(new BorderLayout());
        String[] resCols = { "Reservation ID", "Room ID", "Customer", "Phone", "Check-in", "Check-out" };
        reservationsModel = new DefaultTableModel(resCols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        reservationsTable = new JTable(reservationsModel);
        refreshReservationsTable();
        resPanel.add(new JScrollPane(reservationsTable), BorderLayout.CENTER);

        JPanel resControl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cancelResBtn = new JButton("Cancel Selected Reservation");
        resControl.add(cancelResBtn);
        resPanel.add(resControl, BorderLayout.SOUTH);

        cancelResBtn.addActionListener(e -> {
            int sel = reservationsTable.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(this, "Select a reservation to cancel.");
                return;
            }
            String resId = (String) reservationsModel.getValueAt(sel, 0);
            boolean ok = manager.cancelReservation(resId);
            if (!ok)
                JOptionPane.showMessageDialog(this, "Cancellation failed.");
            else {
                JOptionPane.showMessageDialog(this, "Reservation cancelled.");
                refreshReservationsTable();
                refreshRoomsTable();
                refreshAvailableRoomsCombo(availableRoomsBox);
            }
        });

        // Add tabs
        tabs.addTab("Rooms", roomsPanel);
        tabs.addTab("Book Room", bookingPanel);
        tabs.addTab("Reservations", resPanel);

        add(tabs, BorderLayout.CENTER);
    }

    // helpers to populate UI
    private void refreshRoomsTable() {
        roomsModel.setRowCount(0);
        List<Room> list = manager.getAllRooms();
        for (Room r : list) {
            roomsModel.addRow(new Object[] {
                    r.getRoomId(),
                    r.getType(),
                    r.getPrice(),
                    r.isAvailable() ? "Available" : "Booked"
            });
        }
    }

    private void refreshReservationsTable() {
        reservationsModel.setRowCount(0);
        for (Reservation res : manager.getAllReservations()) {
            reservationsModel.addRow(new Object[] {
                    res.getReservationId(),
                    res.getRoomId(),
                    res.getCustomerName(),
                    res.getPhone(),
                    res.getCheckIn(),
                    res.getCheckOut()
            });
        }
    }

    private void refreshAvailableRoomsCombo(JComboBox<String> box) {
        box.removeAllItems();
        for (Room r : manager.getAvailableRooms()) {
            box.addItem(r.getRoomId());
        }
    }

    public static void main(String[] args) {
        // start GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(HotelReservationGUI::new);
    }
}
