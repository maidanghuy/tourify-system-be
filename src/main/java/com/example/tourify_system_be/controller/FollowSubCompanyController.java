package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.security.JwtUtil;
import com.example.tourify_system_be.service.FollowSubCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowSubCompanyController {

    private final FollowSubCompanyService followService;
    private final JwtUtil jwtUtil; // Inject class JwtUtil của bạn

    @PostMapping("/sub-company/{subCompanyId}")
    public ResponseEntity<?> follow(
            @PathVariable String subCompanyId,
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "").trim();
        String customerId = jwtUtil.extractUserId(jwt);
        followService.follow(customerId, subCompanyId);
        return ResponseEntity.ok().body("{\"message\": \"Followed successfully\"}");
    }

    @DeleteMapping("/sub-company/{subCompanyId}")
    public ResponseEntity<?> unfollow(
            @PathVariable String subCompanyId,
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "").trim();
        String customerId = jwtUtil.extractUserId(jwt);
        followService.unfollow(customerId, subCompanyId);
        return ResponseEntity.ok().body("{\"message\": \"Unfollowed successfully\"}");
    }

    @GetMapping("/sub-company/{subCompanyId}/is-followed")
    public ResponseEntity<?> isFollowed(
            @PathVariable String subCompanyId,
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "").trim();
        String customerId = jwtUtil.extractUserId(jwt);
        boolean isFollowed = followService.isFollowed(customerId, subCompanyId);
        return ResponseEntity.ok().body("{\"isFollowed\": " + isFollowed + "}");
    }

    @GetMapping("/sub-company/{subCompanyId}/followers/count")
    public ResponseEntity<?> countFollowers(@PathVariable String subCompanyId) {
        long count = followService.countFollower(subCompanyId);
        return ResponseEntity.ok().body("{\"count\": " + count + "}");
    }

    @GetMapping("/customer/following/count")
    public ResponseEntity<?> countFollowed(
            @RequestHeader("Authorization") String token
    ) {
        String jwt = token.replace("Bearer ", "").trim();
        String customerId = jwtUtil.extractUserId(jwt);
        long count = followService.countFollowed(customerId);
        return ResponseEntity.ok().body("{\"count\": " + count + "}");
    }

    @GetMapping("/sub-company/{subCompanyId}/followers")
    public ResponseEntity<?> getFollowers(
            @PathVariable String subCompanyId) {
        var ids = followService.getFollowerIds(subCompanyId);
        return ResponseEntity.ok().body(ids);
    }
}
