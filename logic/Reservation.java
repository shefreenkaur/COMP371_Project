package logic;

public class Reservation {
    private String guestName;
    private java.util.Date checkIn;
    private java.util.Date checkOut;
    private String roomType;
    private String paymentStatus;

    public Reservation(String guestName, java.util.Date checkIn, java.util.Date checkOut, String roomType, String paymentStatus) {
        this.guestName = guestName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomType = roomType;
        this.paymentStatus = paymentStatus;
    }

    // Getters and Setters
    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public java.util.Date getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(java.util.Date checkIn) {
        this.checkIn = checkIn;
    }

    public java.util.Date getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(java.util.Date checkOut) {
        this.checkOut = checkOut;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }
}
