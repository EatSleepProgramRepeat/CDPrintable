/*
 * CDPrintable: A program that prints labels with track listings for your CD cases.
 * Copyright (C) 2025 Alexander McLean
 *
 * This source code is licensed under the GNU General Public License v3.0
 * found in the LICENSE file in the root directory of this source tree.
 *
 * This is a class that stores info about an artist from the MusicBrainz API.
 */

package com.CDPrintable.MusicBrainzResources;

public class MusicBrainzArtist {
    private String name;
    private String dateOrganized;   // May only be present for groups
    private String birthDate;       // May only be present for people
    private String id;
    private String sortName;
    private Gender gender;
    private String type;            // Band, Person, etc.
    private String disambiguation;
    private String lifeSpan;
    private String country;

    public MusicBrainzArtist(String name, String dateOrganized, String birthDate, String id, String sortName, Gender gender, String type, String disambiguation, String lifeSpan, String country) {
        this.name = name;
        this.dateOrganized = dateOrganized;
        this.birthDate = birthDate;
        this.id = id;
        this.sortName = sortName;
        this.gender = gender;
        this.type = type;
        this.disambiguation = disambiguation;
        this.lifeSpan = lifeSpan;
        this.country = country;
    }

    public MusicBrainzArtist() {
        this.name = "";
        this.dateOrganized = "";
        this.birthDate = "";
        this.id = "";
        this.sortName = "";
        this.gender = Gender.UNKNOWN;
        this.type = "";
        this.disambiguation = "";
        this.lifeSpan = "";
        this.country = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOrganized() {
        return dateOrganized;
    }

    public void setDateOrganized(String dateOrganized) {
        this.dateOrganized = dateOrganized;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisambiguation() {
        return disambiguation;
    }

    public void setDisambiguation(String disambiguation) {
        this.disambiguation = disambiguation;
    }

    public String getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(String lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}