package main.controller;

import javax.servlet.http.HttpServletRequest;
import main.service.PostService;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiPostController {

    @Autowired
    private PostService postService;

    @GetMapping("/api/statistics/all")
    public String allStatistics(HttpServletRequest httpServletRequest) {
        return postService.allStatistics(httpServletRequest);
    }

    @GetMapping("/api/statistics/my")
    public String myStatistics(HttpServletRequest httpServletRequest) {
        return postService.myStatistics(httpServletRequest);
    }

    @GetMapping("/api/calendar")
    public String calendar(HttpServletRequest httpServletRequest) {
        return postService.calendar(httpServletRequest);
    }

    @PostMapping("/api/moderation")
    public void doModeration(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException {
        postService.doModeration(body, httpServletRequest);
    }

    @PutMapping("/api/post/{id}")
    public String editPost(@PathVariable int id, @RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        return postService.editPost(id, body, httpServletRequest);
    }

    @GetMapping("/api/post/my")
    public String myPosts(@RequestParam("status") String status, @RequestParam int offset, @RequestParam int limit,
        HttpServletRequest request) {
        return postService.myPosts(status,offset,limit,request);
    }

    @GetMapping("/api/post/moderation")
    public String moderation(@RequestParam("status") String status, @RequestParam int offset,
        @RequestParam int limit, HttpServletRequest request) {
        return postService.moderation(status,offset,limit,request);
    }

    @GetMapping("/api/post/byTag")
    public String postByTag(@RequestParam("tag") String tag, @RequestParam int offset, @RequestParam int limit) {
        return postService.postByTag(tag,offset,limit);
    }

    @GetMapping("/api/post/byDate")
    public String postByDate(@RequestParam("date") String date, @RequestParam int offset, @RequestParam int limit) {
        return postService.postByDate(date, offset, limit);
    }

    @PostMapping("/api/comment")
    public String comment(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return postService.comment(body,httpRequest);
    }

    @PostMapping("/api/post/dislike")
    public String dislikePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return postService.dislikePost(body,httpRequest);
    }

    @PostMapping("/api/post/like")
    public String likePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return postService.likePost(body,httpRequest);
    }

    @GetMapping("/api/post/{id}")
    public String getPost(@PathVariable int id, HttpServletRequest request) {
        return postService.getPost(id, request);
    }

    @GetMapping("/api/post/search")
    public String search(@RequestParam("query") String query, @RequestParam int offset, @RequestParam int limit) {
        return postService.search(query,offset,limit);
    }

    @GetMapping("/api/post")
    public String getPosts(@RequestParam("mode") String mode, @RequestParam int offset, @RequestParam int limit) {
        return postService.getPosts(mode,offset,limit);
    }

    @PostMapping("/api/post")
    public String post(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        return postService.post(body, httpServletRequest);
    }

    @GetMapping("/api/tag")
    public String getTags(HttpServletRequest request) {
        return postService.getTags(request);
    }
}
