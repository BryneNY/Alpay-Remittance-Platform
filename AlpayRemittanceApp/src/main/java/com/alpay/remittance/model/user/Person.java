package com.alpay.remittance.model.user;

/**
 * A real person behind a user account: a sender, a staff member or an admin.
 */
public class Person {

    private int personId;
    private String fullName;
    private String email;
    private String phone;
    private String country;

    public Person() {
    }

    public Person(int personId, String fullName, String email, String phone, String country) {
        this.personId = personId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.country = country;
    }

    public int getPersonId() { return personId; }
    public void setPersonId(int personId) { this.personId = personId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    @Override
    public String toString() {
        return fullName;
    }
}