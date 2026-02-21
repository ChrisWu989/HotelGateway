package com.synex.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Review {
	@Id
	private int id;
	
	@OneToOne
	private Hotel hotel;
	
	private String userId;
	
	private int rating;
	
	private String reviewDesc;
	
	private LocalDate reviewDate;
	
	public Review() {
		super();
	}

	public Review(int id, Hotel hotel, String userId, int rating, String reviewDesc, LocalDate reviewDate) {
		super();
		this.id = id;
		this.hotel = hotel;
		this.userId = userId;
		this.rating = rating;
		this.reviewDesc = reviewDesc;
		this.reviewDate = reviewDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Hotel getHotel() {
		return hotel;
	}

	public void setHotel(Hotel hotel) {
		this.hotel = hotel;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getReviewDesc() {
		return reviewDesc;
	}

	public void setReviewDesc(String reviewDesc) {
		this.reviewDesc = reviewDesc;
	}

	public LocalDate getReviewDate() {
		return reviewDate;
	}

	public void setReviewDate(LocalDate reviewDate) {
		this.reviewDate = reviewDate;
	}
}

