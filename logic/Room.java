package logic;

/**
 * Represents a room in the hotel.
 * This class contains all the data related to a hotel room.
 */
public class Room {
    private int roomId;
    private String roomNumber;
    private int typeId;
    private String typeName;
    private int floor;
    private String status;
    private java.util.Date lastCleaned;
    private String notes;
    private double basePrice;
    private int capacity;
    private String amenities;

    /**
     * Default constructor
     */
    public Room() {
        this.status = "Available";
    }

    /**
     * Constructor with essential room details
     *
     * @param roomNumber Room number
     * @param typeId Room type ID
     * @param floor Floor number
     */
    public Room(String roomNumber, int typeId, int floor) {
        this.roomNumber = roomNumber;
        this.typeId = typeId;
        this.floor = floor;
        this.status = "Available";
    }

    // Getters and Setters

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.util.Date getLastCleaned() {
        return lastCleaned;
    }

    public void setLastCleaned(java.util.Date lastCleaned) {
        this.lastCleaned = lastCleaned;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    /**
     * Checks if the room is currently available
     *
     * @return True if available, false otherwise
     */
    public boolean isAvailable() {
        return "Available".equals(status);
    }

    /**
     * Checks if the room needs cleaning
     *
     * @return True if needs cleaning, false otherwise
     */
    public boolean needsCleaning() {
        return "Cleaning".equals(status);
    }

    /**
     * Formats the room number with floor for display
     *
     * @return Formatted room identifier
     */
    public String getFormattedRoomNumber() {
        return "Room " + roomNumber + " (Floor " + floor + ")";
    }

    @Override
    public String toString() {
        return getFormattedRoomNumber() + " - " + typeName;
    }
}