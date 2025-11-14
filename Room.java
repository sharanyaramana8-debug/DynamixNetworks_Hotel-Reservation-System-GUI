import java.util.Objects;

public class Room {
    private String roomId;     // e.g. R101
    private String type;       // Single, Double, Suite
    private double price;
    private boolean available; // true = available, false = booked

    public Room(String roomId, String type, double price, boolean available) {
        this.roomId = roomId;
        this.type = type;
        this.price = price;
        this.available = available;
    }

    public String getRoomId() { return roomId; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }

    public void setType(String type) { this.type = type; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailable(boolean available) { this.available = available; }

    public String toCSV() {
        return roomId + "," + type + "," + price + "," + available;
    }

    public static Room fromCSV(String line) {
        String[] p = line.split(",");
        return new Room(p[0], p[1], Double.parseDouble(p[2]), Boolean.parseBoolean(p[3]));
    }

    @Override
    public String toString() {
        return roomId + " - " + type + " - ₹" + price + " - " + (available ? "Available" : "Booked");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return Objects.equals(roomId, room.roomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }
}
