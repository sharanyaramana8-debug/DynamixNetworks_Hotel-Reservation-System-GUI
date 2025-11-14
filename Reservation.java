import java.time.LocalDate;

public class Reservation {
    private String reservationId; // e.g. RES-1001
    private String roomId;
    private String customerName;
    private String phone;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public Reservation(String reservationId, String roomId, String customerName, String phone,
            LocalDate checkIn, LocalDate checkOut) {
        this.reservationId = reservationId;
        this.roomId = roomId;
        this.customerName = customerName;
        this.phone = phone;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public String toCSV() {
        return reservationId + "," + roomId + "," + customerName + "," + phone + "," + checkIn + "," + checkOut;
    }

    public static Reservation fromCSV(String line) {
        String[] p = line.split(",");
        return new Reservation(
                p[0],
                p[1],
                p[2],
                p[3],
                LocalDate.parse(p[4]),
                LocalDate.parse(p[5]));
    }
}
