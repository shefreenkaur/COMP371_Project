package logic;

import java.sql.Date;

/**
 * Represents a hotel reservation in the system.
 * This class contains all the data related to a guest's reservation.
 */
public class Reservation {
    private int reservationId;
    private int guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Date checkInDate;
    private Date checkOutDate;
    private String status;
    private int totalGuests;
    private int roomTypeId;
    private String roomTypeName;
    private String roomNumber;
    private double ratePerNight;
    private String specialRequests;
    private int createdBy;
    private java.util.Date createdAt;
    private java.util.Date updatedAt;

    /**
     * Default constructor
     */
    public Reservation() {
        // Initialize with default values
        this.status = "Confirmed";
        this.totalGuests = 1;
    }

    /**
     * Constructor with basic reservation details
     *
     * @param firstName Guest's first name
     * @param lastName Guest's last name
     * @param email Guest's email
     * @param phone Guest's phone number
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param roomTypeId Room type ID
     * @param totalGuests Number of guests
     */
    public Reservation(String firstName, String lastName, String email, String phone,
                       Date checkInDate, Date checkOutDate, int roomTypeId, int totalGuests) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.roomTypeId = roomTypeId;
        this.totalGuests = totalGuests;
        this.status = "Confirmed";
    }

    // Getters and Setters

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalGuests() {
        return totalGuests;
    }

    public void setTotalGuests(int totalGuests) {
        this.totalGuests = totalGuests;
    }

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getRatePerNight() {
        return ratePerNight;
    }

    public void setRatePerNight(double ratePerNight) {
        this.ratePerNight = ratePerNight;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public java.util.Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.util.Date createdAt) {
        this.createdAt = createdAt;
    }

    public java.util.Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.util.Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Calculate the total number of nights for this reservation
     *
     * @return Number of nights
     */
    public int getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }

        long diffInMillies = checkOutDate.getTime() - checkInDate.getTime();
        return (int) (diffInMillies / (1000 * 60 * 60 * 24));
    }

    /**
     * Calculate the total cost of the room for this reservation
     *
     * @return Total room cost
     */
    public double getTotalRoomCost() {
        return getRatePerNight() * getNumberOfNights();
    }

    @Override
    public String toString() {
        return "Reservation #" + reservationId + ": " + getFullName() +
                " (" + checkInDate + " to " + checkOutDate + ")";
    }
}