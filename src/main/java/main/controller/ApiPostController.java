package main.controller;

import static main.model.ModerationStatus.ACCEPTED;
import static main.model.ModerationStatus.DECLINED;
import static main.model.ModerationStatus.NEW;

import java.io.PipedOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import main.model.Post;
import main.model.PostComment;
import main.model.PostCommentsRepository;
import main.model.PostVote;
import main.model.PostVotesRepository;
import main.model.PostsRepository;
import main.model.Tag;
import main.model.Tag2Post;
import main.model.Tag2PostRepository;
import main.model.TagsRepository;
import main.model.User;
import main.model.UsersRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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

    final static String NO_TITLE = "Заголовок не установлен";
    final static String SMALL_TEXT = "Текст публикации слишком короткий";
    final static String TODAY = "Сегодня, ";
    final static String YESTERDAY = "Вчера, ";
    final static String SMALL_COMMENT = "Текст комментария не задан или слишком короткий";
    final static int MIN_TITLE_LENGTH = 5;
    final static int MIN_TEXT_LENGTH = 50;
    final static int ANNOUNCE_LENGTH = 200;

    private JSONObject response, request = null;
    private final JSONParser parser = new JSONParser();
    @Autowired
    private TagsRepository tagsRepository;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private Tag2PostRepository tag2PostRepository;
    @Autowired
    private PostCommentsRepository postCommentsRepository;
    @Autowired
    private PostVotesRepository postVotesRepository;

    @GetMapping("/api/statistics/all")
    public String allStatistics(HttpServletRequest httpServletRequest) {
        response = new JSONObject();
        int postCount = getVisiblePost(0, (int) postsRepository.count()).size();
        int viewsCount = getVisiblePost(0, (int) postsRepository.count()).stream().mapToInt(Post::getViewCount)
            .sum();
        String firstPublication = getVisiblePost(0, (int) postsRepository.count()).stream()
            .sorted(Comparator.comparing(Post::getTime))
            .map(p -> new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getTime())).findFirst().get();

        List<Integer> visiblePostIds = getVisiblePost(0, (int) postsRepository.count()).stream()
            .mapToInt(Post::getId).boxed().collect(
                Collectors.toList());

        int likesCount = (int) StreamSupport.stream(postVotesRepository.findAll()
            .spliterator(), false).filter(v -> visiblePostIds.contains(v.getPostId()) && v.getValue() == 1)
            .count();

        int dislikesCount = (int) StreamSupport.stream(postVotesRepository.findAll()
            .spliterator(), false).filter(v -> visiblePostIds.contains(v.getPostId()) && v.getValue() == -1)
            .count();

        response.put("postsCount", postCount);
        response.put("likesCount", likesCount);
        response.put("dislikesCount", dislikesCount);
        response.put("viewsCount", viewsCount);
        response.put("firstPublication", firstPublication);
        return response.toJSONString();
    }

    @GetMapping("/api/statistics/my")
    public String myStatistics(HttpServletRequest httpServletRequest) {
        if (!checkLogin(httpServletRequest.getSession())) {
            return null;
        }
        response = new JSONObject();
        int userId = getLoginUserId(httpServletRequest.getSession());
        int postCount = (int) getVisiblePost(0, (int) postsRepository.count()).stream()
            .filter(p -> p.getUserId() == userId).count();
        int viewsCount = getVisiblePost(0, (int) postsRepository.count()).stream()
            .filter(p -> p.getUserId() == userId).mapToInt(Post::getViewCount).sum();
        String firstPublication = getVisiblePost(0, (int) postsRepository.count()).stream()
            .filter(p -> p.getUserId() == userId)
            .sorted(Comparator.comparing(Post::getTime))
            .map(p -> new SimpleDateFormat("yyyy-MM-dd HH:mm").format(p.getTime())).findFirst().get();

        List<Integer> myPostIds = getVisiblePost(0, (int) postsRepository.count()).stream()
            .filter(p -> p.getUserId() == userId).mapToInt(Post::getId).boxed().collect(
                Collectors.toList());

        int likesCount = (int) StreamSupport.stream(postVotesRepository.findAll()
            .spliterator(), false).filter(v -> myPostIds.contains(v.getPostId()) && v.getValue() == 1).count();

        int dislikesCount = (int) StreamSupport.stream(postVotesRepository.findAll()
            .spliterator(), false).filter(v -> myPostIds.contains(v.getPostId()) && v.getValue() == -1).count();

        response.put("postsCount", postCount);
        response.put("likesCount", likesCount);
        response.put("dislikesCount", dislikesCount);
        response.put("viewsCount", viewsCount);
        response.put("firstPublication", firstPublication);

        return response.toJSONString();
    }

    @GetMapping("/api/calendar")
    public String calendar(HttpServletRequest httpServletRequest) {
        response = new JSONObject();
        String year = httpServletRequest.getParameter("year");
        JSONArray yearsArray = new JSONArray();
        yearsArray.addAll(postsRepository.getYearsOfPost());
        JSONObject postsCounts = new JSONObject();
        List<Date> datesOfPosts = postsRepository.getPostDates();
        List<Integer> countsOfPosts = postsRepository.getCountOfPostByDate();
        for (int i = 0; i <datesOfPosts.size() ; i++) {
            postsCounts.put(new SimpleDateFormat("yyyy-MM-dd").format(datesOfPosts.get(i)), countsOfPosts.get(i));
        }
        response.put("years", yearsArray);
        response.put("posts", postsCounts);
        return response.toJSONString();
    }

    @PostMapping("/api/moderation")
    public void doModeration(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException {
        if (!checkLogin(httpServletRequest.getSession())) {
            return;
        }
        int UserId = getLoginUserId(httpServletRequest.getSession());
        if (!getUserById(UserId).isModerator()) {
            return;
        }
        request = (JSONObject) parser.parse(body);
        int postID = (int) ((long) request.get("post_id"));
        String decision = (String) request.get("decision");

        Post post = postsRepository.findById(postID).get();
        post.setModeratorId(UserId);
        if (decision.equals("accept")) {
            post.setModerationStatus(ACCEPTED);
        } else {
            post.setModerationStatus(DECLINED);
        }
        postsRepository.save(post);
    }

    @PutMapping("/api/post/{id}")
    public String editPost(@PathVariable int id, @RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        if (!checkLogin(httpServletRequest.getSession())) {
            return null;
        }
        int UserId = getLoginUserId(httpServletRequest.getSession());
        response = new JSONObject();
        request = (JSONObject) parser.parse(body);
        long createTime = checkTime((String) request.get("time"));
        String title = (String) request.get("title");
        JSONArray tags = (JSONArray) request.get("tags");
        String text = (String) request.get("text");
        if (checkPostErrors(title, text).size() != 0) {
            response.put("result", false);
            response.put("errors", checkPostErrors(title, text));
        } else {
            Post post = postsRepository.findById(id).get();
            post.setActive((byte) ((long) request.get("active")));
            if (!getUserById(UserId).isModerator()) {
                post.setModerationStatus(NEW);
            }
            post.setTime(new Date(createTime));
            post.setTitle(title);
            post.setText(text);
            postsRepository.save(post);
            for (Tag2Post tag : tag2PostRepository.findAll()) {
                if (tag.getPostId() == id) {
                    tag2PostRepository.delete(tag);
                }
            }
            tags.forEach(t -> tag2PostRepository.save(new Tag2Post(id, getTagIdByName((String) t))));
            response.put("result", true);
        }
        return response.toJSONString();
    }

    @GetMapping("/api/post/my")
    public String myPosts(@RequestParam("status") String status, @RequestParam int offset, @RequestParam int limit,
        HttpServletRequest request) {
        if (!checkLogin(request.getSession())) {
            return null;
        }
        int userId = getLoginUserId(request.getSession());
        byte active = (byte) (status.equals("inactive") ? 0 : 1);
        String moderationStatus = status.equals("inactive") || status.equals("pending") ? "NEW"
            : status.equals("declined") ? "DECLINED" : "ACCEPTED";
        return transformListPostToJsonObject(
            postsRepository.getPostsOfUser(moderationStatus, userId, active, offset, limit),
            postsRepository.getCountOfPostsOfUser(moderationStatus, userId, active)).toJSONString();
    }

    @GetMapping("/api/post/moderation")
    public String moderation(@RequestParam("status") String status, @RequestParam int offset,
        @RequestParam int limit, HttpServletRequest request) {
        if (!checkLogin(request.getSession())) {
            return null;
        }
        status = status.toUpperCase();
        int userId = getLoginUserId(request.getSession());
        return transformListPostToJsonObject(postsRepository.getPostsForModeration(status, userId, offset, limit),
            postsRepository.getCountOfPostsForModeration(status, userId)).toJSONString();
    }

    @GetMapping("/api/post/byTag")
    public String postByTag(@RequestParam("tag") String tag, @RequestParam int offset, @RequestParam int limit) {
        return transformListPostToJsonObject(postsRepository.getPostsByTag(tag, offset, limit),
            postsRepository.getPostsByTagCount(tag)).toJSONString();
    }

    @GetMapping("/api/post/byDate")
    public String postByDate(@RequestParam("date") String date, @RequestParam int offset,
        @RequestParam int limit) {
        return transformListPostToJsonObject(postsRepository.getPostsByDate(date, offset, limit),
            postsRepository.getCountOfPostsByDate(date)).toJSONString();
    }

    @PostMapping("/api/comment")
    public String comment(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        response = new JSONObject();
        if (checkLogin(httpRequest.getSession())) {
            request = (JSONObject) parser.parse(body);
            Integer parentID = request.get("parent_id") != null ? (int) ((long) request.get("parent_id")) : null;
            String text = (String) request.get("text");
            int postID = (int) ((long) request.get("post_id"));
            int userId = getLoginUserId(httpRequest.getSession());
            if (text.isEmpty() || text.length() < 10) {
                response.put("result", false);
                JSONObject errors = new JSONObject();
                errors.put("text", SMALL_COMMENT);
                response.put("errors", errors);
                return response.toJSONString();
            }
            int commentId = postCommentsRepository
                .save(new PostComment(parentID, postID, userId, new Date(), text)).getId();
            response.put("id", commentId);
        }
        return response.toJSONString();
    }

    @PostMapping("/api/post/dislike")
    public String dislikePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return votePost(body, httpRequest, (byte) -1);
    }

    @PostMapping("/api/post/like")
    public String likePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return votePost(body, httpRequest, (byte) 1);
    }

    @GetMapping("/api/post/{id}")
    public String getPost(@PathVariable int id, HttpServletRequest request) {
        response = new JSONObject();
        String referer = request.getHeader("referer");
        response = transformPostToJsonObject(postsRepository.findById(id).get());
        Post post = postsRepository.findById(id).get();
        if (referer != null && referer.contains("edit")) {
            response.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(post.getTime()));
        } else {
            post.setViewCount(post.getViewCount() + 1);
            postsRepository.save(post);
        }
        response.put("comments", getComments(id));
        response.put("tags", getPostTags(id));
        return response.toJSONString();
    }

    @GetMapping("/api/post/search")
    public String search(@RequestParam("query") String query, @RequestParam int offset, @RequestParam int limit) {
        return transformListPostToJsonObject(postsRepository.getPostsByQuery("%" + query + "%", offset, limit),
            postsRepository.getCountOfPostsByQuery("%" + query + "%")).toJSONString();
    }

    @GetMapping("/api/post")
    public String getPosts(@RequestParam("mode") String mode, @RequestParam int offset, @RequestParam int limit) {
        List<Post> sortedList = mode.equals("popular") ? postsRepository.getPopularPosts(offset, limit) :
            mode.equals("best") ? postsRepository.getBestPosts(offset, limit) :
                mode.equals("early") ? postsRepository.getVisiblePostsOrderDesc(offset, limit) :
                    postsRepository.getVisiblePosts(offset, limit);
        return transformListPostToJsonObject(sortedList, postsRepository.countOfVisiblePosts()).toJSONString();
    }

    @PostMapping("/api/post")
    public String post(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        response = new JSONObject();
        request = (JSONObject) parser.parse(body);
        long createTime = checkTime((String) request.get("time"));
        String title = (String) request.get("title");
        JSONArray tags = (JSONArray) request.get("tags");
        String text = (String) request.get("text");

        if (checkPostErrors(title, text).size() != 0) {
            response.put("result", false);
            response.put("errors", checkPostErrors(title, text));
        } else {
            Post post = new Post();
            post.setActive((byte) ((long) request.get("active")));
            post.setModerationStatus(NEW);
            post.setUserId(getLoginUserId(httpServletRequest.getSession()));
            post.setTime(new Date(createTime));
            post.setTitle(title);
            post.setText(text);
            post.setViewCount(0);
            int postId = postsRepository.save(post).getId();
            tags.forEach(t -> tag2PostRepository.save(new Tag2Post(postId, getTagIdByName((String) t))));
            response.put("result", true);
        }
        return response.toJSONString();
    }

    @GetMapping("/api/tag")
    public String tag(HttpServletRequest request) {
        response = new JSONObject();
        double maxWeight = StreamSupport.stream(tagsRepository.findAll().spliterator(), false)
            .map(t -> calculateTagWeight(t.getId())).max(Comparator.comparing(Double::doubleValue)).get();
        String query = request.getParameter("query");
        JSONArray tagsArray = new JSONArray();
        for (Tag tag : tagsRepository.findAll()) {
            JSONObject tagObject = new JSONObject();
            tagObject.put("name", tag.getName());
            tagObject.put("weight", calculateTagWeight(tag.getId()) / maxWeight);
            if (query != null && query.equals(tag.getName())) {
                return tagObject.toJSONString();
            } else {
                tagsArray.add(tagObject);
            }
        }
        response.put("tags", tagsArray);
        return response.toJSONString();
    }

    private long checkTime(String time) throws java.text.ParseException {
        return Math
            .max(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time).getTime(), System.currentTimeMillis());
    }

    private JSONObject checkPostErrors(String title, String text) {
        JSONObject errors = new JSONObject();
        if (title.length() < MIN_TITLE_LENGTH) {
            errors.put("title", NO_TITLE);
        }
        if (text.length() < MIN_TEXT_LENGTH) {
            errors.put("text", SMALL_TEXT);
        }
        return errors;
    }

    public String votePost(String body, HttpServletRequest httpRequest, byte value) throws ParseException {
        response = new JSONObject();
        if (checkLogin(httpRequest.getSession())) {
            JSONObject request = (JSONObject) parser.parse(body);
            int postId = (int) ((long) request.get("post_id"));
            int userID = getLoginUserId(httpRequest.getSession());
            if (!checkHasLike(postId, userID, value)) {
                postVotesRepository.save(new PostVote(userID, postId, new Date(), value));
                response.put("result", true);
                return response.toJSONString();
            }
        }
        response.put("result", false);
        return response.toJSONString();
    }

    private boolean checkLogin(HttpSession session) {
        return ApiAuthController.sessions.containsKey(session.getId());
    }

    private int getLoginUserId(HttpSession session) {
        return ApiAuthController.sessions.get(session.getId());
    }

    private String formatTime(Date time) {
        Calendar now = Calendar.getInstance();
        Calendar postTime = Calendar.getInstance();
        postTime.setTime(time);
        if (now.get(Calendar.YEAR) == postTime.get(Calendar.YEAR)) {
            if (now.get(Calendar.DAY_OF_YEAR) == postTime
                .get(Calendar.DAY_OF_YEAR)) {
                return TODAY + new SimpleDateFormat("HH:mm").format(time);
            }
            if (now.get(Calendar.DAY_OF_YEAR) - postTime
                .get(Calendar.DAY_OF_YEAR) == 1) {
                return YESTERDAY + new SimpleDateFormat("HH:mm").format(time);
            }
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
    }

    private List<Post> getVisiblePost(int offset, int limit) {
        return postsRepository.getVisiblePosts(offset, limit);
    }

    private List<Post> getAllVisiblePost() {
        return postsRepository.getAllVisiblePosts();
    }

    private double calculateTagWeight(int tagId) {
        double totalCount = postsRepository.countOfVisiblePosts();
        long frequencyTag = tag2PostRepository.getFrequencyOfTag(tagId);
        return Double.parseDouble(new DecimalFormat("#.##").format(frequencyTag / totalCount).replace(",", "."));
    }

    private JSONObject transformListPostToJsonObject(List<Post> posts, int size) {
        response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        posts.forEach(post -> jsonArray.add(transformPostToJsonObject(post)));
        response.put("count", size);
        response.put("posts", jsonArray);
        return response;
    }

    private JSONObject transformPostToJsonObject(Post post) {
        JSONObject jsonPost = new JSONObject();
        jsonPost.put("id", post.getId());
        jsonPost.put("time", formatTime(post.getTime()));
        jsonPost.put("user", getUserJson(post.getUserId()));
        jsonPost.put("title", post.getTitle());
        jsonPost.put("active", post.getActive());
        jsonPost.put("announce", getPlainText(post.getText()));
        jsonPost.put("text", post.getText());
        jsonPost.put("likeCount", getLikes(post.getId(), 1));
        jsonPost.put("dislikeCount", getLikes(post.getId(), -1));
        jsonPost.put("commentCount", getComments(post.getId()).size());
        jsonPost.put("viewCount", post.getViewCount());
        return jsonPost;
    }

    private String getPlainText(String string) {
        string = string.replaceAll("\\<[^>]*>", "").replaceAll("&nbsp;", " ");
        return string.substring(0, Math.min(ANNOUNCE_LENGTH, string.length() - 1));
    }

    private boolean checkHasLike(int postId, int userId, int value) {
        return postVotesRepository.checkHasLike(postId, userId, value) > 0;
    }

    private int getLikes(int postId, int value) {
        return postVotesRepository.getPostLikes(postId, value);
    }

    private JSONArray getComments(int postId) {
        JSONArray arrayOfComments = new JSONArray();
        postCommentsRepository.getPostComments(postId).forEach(c -> {
            JSONObject jsonComment = new JSONObject();
            jsonComment.put("id", c.getId());
            jsonComment.put("time", formatTime(c.getTime()));
            jsonComment.put("text", c.getText());
            jsonComment.put("parent_id", c.getParentId());
            jsonComment.put("user", getUserJson(c.getUserId()));
            arrayOfComments.add(jsonComment);
        });
        return arrayOfComments;
    }

    private JSONArray getPostTags(int id) {
        JSONArray arrayOfTags = new JSONArray();
        arrayOfTags.addAll(tagsRepository.getPostTags(id));
        return arrayOfTags;
    }

    private JSONObject getUserJson(int id) {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("id", id);
        jsonUser.put("name", getUserById(id).getName());
        jsonUser.put("photo", getUserById(id).getPhoto());
        return jsonUser;
    }

    private int getTagIdByName(String tagName) {
        return tagsRepository.getTagIdByName(tagName);
    }

    private User getUserById(int id) {
        return usersRepository.findById(id).get();
    }

}
