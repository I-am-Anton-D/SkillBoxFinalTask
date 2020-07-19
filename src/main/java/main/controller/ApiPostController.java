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

/**
 * Rest Controller for posts
 */

@RestController
public class ApiPostController {

    /**
     * Service for posts manipulation
     */

    @Autowired
    private PostService postService;

    /**
     * Getting statistics of all blog
     *
     * @param httpServletRequest using for detecting user
     * @return response in JSON, @see PostService.allStatistics()
     */

    @GetMapping("/api/statistics/all")
    public String allStatistics(HttpServletRequest httpServletRequest) {
        return postService.allStatistics(httpServletRequest);
    }

    /**
     * Gettong statistics of user
     *
     * @param httpServletRequest using for detecting user
     * @return response in JSON, @see PostService.allStatistics()
     */

    @GetMapping("/api/statistics/my")
    public String myStatistics(HttpServletRequest httpServletRequest) {
        return postService.myStatistics(httpServletRequest);
    }

    /**
     * Getting the count of posts by day in year
     *
     * @param year from request
     * @return response in JSON, @see PostService.calendar()
     */

    @GetMapping("/api/calendar")
    public String calendar(@RequestParam("year") String year) {
        return postService.calendar(year);
    }

    /**
     * Moderation some posts
     *
     * @param body               request body in JSON
     * @param httpServletRequest using for detecting user
     * @throws ParseException if can not parse Response Body to Json
     */

    @PostMapping("/api/moderation")
    public void doModeration(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException {
        postService.doModeration(body, httpServletRequest);
    }

    /**
     * Edit post of user
     *
     * @param id                 user id in DB
     * @param body               request body in JSON
     * @param httpServletRequest using for detecting user
     * @return response in JSON, @see PostService.editPost()
     * @throws ParseException           if can not parse Response Body to Json
     * @throws java.text.ParseException if can not translate string of date in object Date
     */

    @PutMapping("/api/post/{id}")
    public String editPost(@PathVariable int id, @RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        return postService.editPost(id, body, httpServletRequest);
    }

    /**
     * List of users posts
     *
     * @param status  of user posts. @see ModerationStatus enum
     * @param offset  of user in posts list
     * @param limit   ot user in posts list
     * @param request using for detecting user
     * @return response in JSON, @see PostService.myPosts()
     */

    @GetMapping("/api/post/my")
    public String myPosts(@RequestParam("status") String status, @RequestParam int offset, @RequestParam int limit,
        HttpServletRequest request) {
        return postService.myPosts(status, offset, limit, request);
    }

    /**
     * List posts for moderation
     *
     * @param status  of user posts. @see ModerationStatus enum
     * @param offset  of user posts in list
     * @param limit   ot user posts in list
     * @param request using for detecting user
     * @return response in JSON, @see PostService.moderation()
     */

    @GetMapping("/api/post/moderation")
    public String moderation(@RequestParam("status") String status, @RequestParam int offset,
        @RequestParam int limit, HttpServletRequest request) {
        return postService.moderation(status, offset, limit, request);
    }

    /**
     * Getting list of post by tag
     *
     * @param tag    name of tag
     * @param offset in posts list
     * @param limit  in posts list
     * @return response in JSON, @see PostService.postByTag()
     */

    @GetMapping("/api/post/byTag")
    public String postByTag(@RequestParam("tag") String tag, @RequestParam int offset, @RequestParam int limit) {
        return postService.postByTag(tag, offset, limit);
    }

    /**
     * Getting list of post by date (day)
     *
     * @param date   specific date (yyyy-MM-dd)
     * @param offset in posts list
     * @param limit  n posts list
     * @return response in JSON, @see PostService.postByDate()
     */

    @GetMapping("/api/post/byDate")
    public String postByDate(@RequestParam("date") String date, @RequestParam int offset,
        @RequestParam int limit) {
        return postService.postByDate(date, offset, limit);
    }

    /**
     * Make the comment for a post
     *
     * @param body        request in JSON
     * @param httpRequest using for detecting user
     * @return response in JSON, @see PostService.comment()
     * @throws ParseException if can not parse Response Body to Json
     */

    @PostMapping("/api/comment")
    public String comment(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return postService.comment(body, httpRequest);
    }

    /**
     * Dislike post
     *
     * @param body        request in JSON
     * @param httpRequest using for detecting user
     * @return response in JSON, @see PostService.dislikePost()
     * @throws ParseException if can not parse Response Body to Json
     */

    @PostMapping("/api/post/dislike")
    public String dislikePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return postService.dislikePost(body, httpRequest);
    }

    /**
     * Like post
     *
     * @param body        request in JSON
     * @param httpRequest using for detecting user
     * @return response in JSON, @see PostService.likePost()
     * @throws ParseException if can not parse Response Body to Json
     */

    @PostMapping("/api/post/like")
    public String likePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return postService.likePost(body, httpRequest);
    }

    /**
     * Getting the post
     *
     * @param id      of the post in DB
     * @param request here using for detecting referer, @see more PostService.getPost()
     * @return response in JSON, @see PostService.getPost()
     */

    @GetMapping("/api/post/{id}")
    public String getPost(@PathVariable int id, HttpServletRequest request) {
        return postService.getPost(id, request);
    }

    /**
     * Searching posts
     *
     * @param query  for seaching
     * @param offset of list posts
     * @param limit  of list posts
     * @return response in JSON, @see PostService.search()
     */
    @GetMapping("/api/post/search")
    public String search(@RequestParam("query") String query, @RequestParam int offset, @RequestParam int limit) {
        return postService.search(query, offset, limit);
    }

    /**
     * Gettin list of posts
     *
     * @param mode   for sorting posts in list
     * @param offset of list of posts
     * @param limit  ot list of posts
     * @return response in JSON, @see PostService.getPosts()
     */

    @GetMapping("/api/post")
    public String getPosts(@RequestParam("mode") String mode, @RequestParam int offset, @RequestParam int limit) {
        return postService.getPosts(mode, offset, limit);
    }

    /**
     * Create and publicate new post
     *
     * @param body               request in JSON
     * @param httpServletRequest using for detecting user
     * @return response in JSON, @see PostService.post()
     * @throws ParseException           if can not parse Response Body to Json
     * @throws java.text.ParseException if can not translate string of date in object Date
     */

    @PostMapping("/api/post")
    public String post(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        return postService.post(body, httpServletRequest);
    }

    /**
     * Getting tags of blog. Using on index.html
     *
     * @param request request in JSON
     * @return response in JSON, @see PostService.getTags()
     */

    @GetMapping("/api/tag")
    public String getTags(HttpServletRequest request) {
        return postService.getTags(request);
    }
}
