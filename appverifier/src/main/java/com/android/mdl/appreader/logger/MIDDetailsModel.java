package com.android.mdl.appreader.logger;

import java.util.Date;
import java.util.Optional;

public class MIDDetailsModel {
    private String firstName;
    private String lastName;
    private String docId;
    private String email;
    private String imei;
    private String lattitude;
    private String longitude;
    private String midReaderStatus;
    private Optional image;
    private long createdOn;
    private Date dob;

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

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getLattitude() {
        return lattitude;
    }

    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getMidReaderStatus() {
        return midReaderStatus;
    }

    public void setMidReaderStatus(String midReaderStatus) {
        this.midReaderStatus = midReaderStatus;
    }

    public Optional getImage() {
        return image;
    }

    public void setImage(Optional image) {
        this.image = image;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }
}
