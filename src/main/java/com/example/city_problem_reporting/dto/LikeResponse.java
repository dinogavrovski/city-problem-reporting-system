package com.example.city_problem_reporting.dto;

public class LikeResponse {
    private int likeCount;
    private boolean likedByMe;

    public LikeResponse(int likeCount, boolean likedByMe) {
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
    }

    public int getLikeCount() { return likeCount; }
    public boolean isLikedByMe() { return likedByMe; }
}