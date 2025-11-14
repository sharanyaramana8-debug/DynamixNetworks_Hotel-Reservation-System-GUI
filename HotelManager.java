import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple manager that keeps rooms and reservations, and persists to CSV files.
 * Files used: rooms.csv, reservations.csv
 */
public class HotelManager {
    private List<Room> rooms = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    private final String ROOMS_FILE = "rooms.csv";
    private final String RES_FILE = "reservations.csv";

    public HotelManager() {
        loadRooms();
        loadReservations();
    }

    // ----- Rooms -----
    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms);
    }

    public List<Room> getAvailableRooms() {
        return rooms.stream().filter(Room::isAvailable).collect(Collectors.toList());
    }

    public Room getRoomById(String roomId) {
        for (Room r : rooms)
            if (r.getRoomId().equals(roomId))
                return r;
        return null;
    }

    public boolean addRoom(Room room) {
        if (getRoomById(room.getRoomId()) != null)
            return false;
        rooms.add(room);
        saveRooms();
        return true;
    }

    public boolean removeRoom(String roomId) {
        Room r = getRoomById(roomId);
        if (r == null)
            return false;
        // if room has an active reservation, disallow removal (simple rule)
        boolean hasRes = reservations.stream().anyMatch(res -> res.getRoomId().equals(roomId));
        if (hasRes)
            return false;
        rooms.remove(r);
        saveRooms();
        return true;
    }

    // ----- Reservations -----
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    private String generateReservationId() {
        int base = 1000;
        if (!reservations.isEmpty()) {
            // get last numeric suffix
            String last = reservations.get(reservations.size() - 1).getReservationId();
            try {
                String[] parts = last.split("-");
                int num = Integer.parseInt(parts[1]);
                return "RES-" + (num + 1);
            } catch (Exception ignored) {
            }
        }
        return "RES-" + base;
    }

    /**
     * Book a room: mark room as booked and create reservation.
     * Returns reservation created or null if fail.
     */
    public Reservation bookRoom(String roomId, String customerName, String phone, LocalDate checkIn,
            LocalDate checkOut) {
        Room r = getRoomById(roomId);
        if (r == null || !r.isAvailable())
            return null;
        String resId = generateReservationId();
        Reservation res = new Reservation(resId, roomId, customerName, phone, checkIn, checkOut);
        reservations.add(res);
        // mark room as booked (simple approach: a room is either available or booked)
        r.setAvailable(false);
        saveReservations();
        saveRooms();
        return res;
    }

    /**
     * Cancel reservation by reservationId. Returns true if success.
     */
    public boolean cancelReservation(String reservationId) {
        Reservation toRemove = null;
        for (Reservation r : reservations) {
            if (r.getReservationId().equals(reservationId)) {
                toRemove = r;
                break;
            }
        }
        if (toRemove == null)
            return false;
        // free the room
        Room room = getRoomById(toRemove.getRoomId());
        if (room != null)
            room.setAvailable(true);
        reservations.remove(toRemove);
        saveReservations();
        saveRooms();
        return true;
    }

    // ----- Persistence -----
    private void saveRooms() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ROOMS_FILE))) {
            for (Room r : rooms)
                pw.println(r.toCSV());
        } catch (IOException e) {
            System.out.println("Error saving rooms file: " + e.getMessage());
        }
    }

    private void loadRooms() {
        rooms.clear();
        File f = new File(ROOMS_FILE);
        if (!f.exists()) {
            // create some sample rooms for first run
            rooms.add(new Room("R101", "Single", 1200.0, true));
            rooms.add(new Room("R102", "Single", 1200.0, true));
            rooms.add(new Room("R201", "Double", 2000.0, true));
            rooms.add(new Room("R202", "Double", 2000.0, true));
            rooms.add(new Room("R301", "Suite", 4500.0, true));
            saveRooms();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(ROOMS_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isBlank()) {
                rooms.add(Room.fromCSV(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    private void saveReservations() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(RES_FILE))) {
            for (Reservation r : reservations)
                pw.println(r.toCSV());
        } catch (IOException e) {
            System.out.println("Error saving reservations file: " + e.getMessage());
        }
    }

    private void loadReservations() {
        reservations.clear();
        File f = new File(RES_FILE);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(RES_FILE))) {
            String line;
            while ((line = br.readLine()) != null && !line.isBlank()) {
                reservations.add(Reservation.fromCSV(line));
            }
        } catch (IOException e) {
            System.out.println("Error loading reservations: " + e.getMessage());
        }
    }
}
