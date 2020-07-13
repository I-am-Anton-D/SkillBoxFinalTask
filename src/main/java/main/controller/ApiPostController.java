package main.controller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import main.model.ModerationStatus;
import main.model.PostComments;
import main.model.PostCommentsRepository;
import main.model.PostVotes;
import main.model.PostVotesRepository;
import main.model.Posts;
import main.model.PostsRepository;
import main.model.Tag2Post;
import main.model.Tag2PostRepository;
import main.model.Tags;
import main.model.TagsRepository;
import main.model.UsersRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiPostController {

    final static String NO_TITLE = "Заголовок не установлен";
    final static String SMALL_TEXT = "Текст публикации слишком короткий";
    final static String TODAY = "Сегодня, ";
    final static String YESTERDAY = "Вчера, ";
    final static int ANNOUNCE_LENGTH = 200;
    final static String SMALL_COMMENT = "Текст комментария не задан или слишком короткий";

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


    @PostMapping("/api/comment")
    public String comment(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        response = new JSONObject();
        if (checkLogin(httpRequest.getSession())) {
            request = (JSONObject) parser.parse(body);
            Integer parentID = request.get("parent_id")!=null ? (int)((long)request.get("parent_id")) : null;
            int postID = (int)((long)request.get("post_id"));
            String text = (String)request.get("text");
            int userId = getLoginUserId(httpRequest.getSession());
            if (text.isEmpty() || text.length()<10) {
                response.put("result",false);
                JSONObject errors = new JSONObject();
                errors.put("text", SMALL_COMMENT);
                response.put("errors", errors);
                return response.toJSONString();
            }

            int commentId = postCommentsRepository.save(new PostComments(parentID, postID, userId,new Date(),text)).getId();
            response.put("id",commentId);
        }
        return response.toJSONString();
    }

    @PostMapping("/api/post/dislike")
    public String dislikePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return votePost(body, httpRequest, (byte)-1);
    }

    @PostMapping("/api/post/like")
    public String likePost(@RequestBody String body, HttpServletRequest httpRequest) throws ParseException {
        return votePost(body, httpRequest, (byte)1);
    }

    public String votePost(String body, HttpServletRequest httpRequest, byte value) throws ParseException {
        response = new JSONObject();
        if (ApiAuthController.sessions.containsKey(httpRequest.getSession().getId())) {
            JSONObject request = (JSONObject) parser.parse(body);
            int postId = (int) ((long) request.get("post_id"));
            int userID = getLoginUserId(httpRequest.getSession());
            if (!checkHasLike(postId, userID, value)) {
                postVotesRepository.save(new PostVotes(userID, postId, new Date(), value));
                response.put("result", true);
                return response.toJSONString();
            }
        }
        response.put("result", false);
        return response.toJSONString();
    }


    @GetMapping("/api/post/{id}")
    public String getPost(@PathVariable int id) {
        response = new JSONObject();
        response = transformPostToJsonObject(postsRepository.findById(id).get());
        Posts post = postsRepository.findById(id).get();
        post.setViewCount(post.getViewCount() + 1);
        postsRepository.save(post);
        response.put("comments", getComments(id));
        response.put("tags", getTags(id));
        return response.toJSONString();
    }

    @GetMapping("/api/post/search")
    public String search(HttpServletRequest request) {
        response = new JSONObject();
        int offset = Integer.parseInt(request.getParameter("offset"));
        int limit = Integer.parseInt(request.getParameter("limit"));
        String query = request.getParameter("query");
        List<Posts> searchedPosts = getVisiblePost().stream()
            .filter(p -> p.getText().contains(query) || p.getTitle().contains(query))
            .collect(Collectors.toList());
        if (searchedPosts.size() == 0) {
            response.put("result", false);
        } else {
            response = transformListPostToJsonObject(searchedPosts.stream().skip(offset)
                .limit(limit).collect(Collectors.toList()));
        }
        return response.toJSONString();
    }

    @GetMapping("/api/post")
    public String getPosts(HttpServletRequest request) {
        response = new JSONObject();
        int offset = Integer.parseInt(request.getParameter("offset"));
        int limit = Integer.parseInt(request.getParameter("limit"));
        String mode = request.getParameter("mode");
        List<Posts> sortedList = getVisiblePost();

        if (mode.equals("recent")) {
            sortedList = sortedList.stream().sorted(Comparator.comparing(Posts::getTime).reversed())
                .collect(Collectors.toList());
        }
        if (mode.equals("popular")) {
            sortedList = sortedList.stream().sorted(Comparator.comparing(posts -> getComments(posts.getId()).size()))
                .collect(Collectors.toList());
            Collections.reverse(sortedList);
        }
        if (mode.equals("best")) {
            sortedList = sortedList.stream().sorted(Comparator.comparing(posts -> getLikes(posts.getId(), 1)))
                .collect(Collectors.toList());
            Collections.reverse(sortedList);
        }
        if (mode.equals("early")) {
            sortedList = sortedList.stream().sorted(Comparator.comparing(Posts::getTime))
                .collect(Collectors.toList());
        }
        response = transformListPostToJsonObject(sortedList.stream().skip(offset)
            .limit(limit).collect(Collectors.toList()));
        return response.toJSONString();
    }

    @PostMapping("/api/post")
    public String post(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        response = new JSONObject();
        request = (JSONObject) parser.parse(body);
        long createTime = new SimpleDateFormat("yyyy-MM-dd hh:mm").parse((String) request.get("time")).getTime();
        createTime = Math.max(createTime, System.currentTimeMillis());
        long active = (long) request.get("active");
        String title = (String) request.get("title");
        JSONArray tags = (JSONArray) request.get("tags");
        String text = (String) request.get("text");

        JSONObject errors = new JSONObject();
        if (title.length() < 4) {
            errors.put("title", NO_TITLE);
        }
        if (text.length() < 50) {
            errors.put("text", SMALL_TEXT);
        }
        if (errors.size() != 0) {
            response.put("result", false);
            response.put("errors", errors);
        } else {
            Posts post = new Posts();
            post.setIsActive((byte) active);
            post.setModerationStatus(ModerationStatus.NEW);
            post.setUserId(getLoginUserId(httpServletRequest.getSession()));
            post.setTime(new Date(createTime));
            post.setTitle(title);
            post.setText(text);
            post.setViewCount(0);
            int postId = postsRepository.save(post).getId();
            int tagId = -1;
            for (int i = 0; i < tags.size(); i++) {
                for (Tags tag : tagsRepository.findAll()) {
                    if (tag.getName().equals(tags.get(i))) {
                        tagId = tag.getId();
                        break;
                    }
                }
                tag2PostRepository.save(new Tag2Post(postId, tagId));
            }
            response.put("result", true);
        }
        return response.toJSONString();
    }

    @GetMapping("/api/tag")
    public String tag(HttpServletRequest request) {
        response = new JSONObject();
        String query = request.getParameter("query");
        JSONArray tagsArray = new JSONArray();
        for (Tags tag : tagsRepository.findAll()) {
            JSONObject tagObject = new JSONObject();
            tagObject.put("name", tag.getName());
            tagObject.put("weight", calculateTagWeight(tag.getId()));
            if (query!=null && query.equals(tag.getName())) {
                return tagObject.toJSONString();
            } else {
                tagsArray.add(tagObject);
            }
        }
        response.put("tags", tagsArray);
        return response.toJSONString();
    }

    private boolean checkLogin(HttpSession session) {
        return ApiAuthController.sessions.containsKey(session.getId());
    }

    private int getLoginUserId(HttpSession session){
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

    private List<Posts> getVisiblePost() {
        //TODO Fix Moderation status
        Iterable<Posts> posts = postsRepository.findAll();
        return StreamSupport.stream(posts.spliterator(), false)
            .filter(p -> p.getIsActive() == 1 && p.getModerationStatus() == ModerationStatus.NEW
                && p.getTime().getTime() <= System.currentTimeMillis())
            .collect(Collectors.toList());
    }

    private double calculateTagWeight(int tagId) {
        Set<Integer> visibleIds = new HashSet<>();
        for (Posts post : getVisiblePost()) {
            visibleIds.add(post.getId());
        }
        double totalCount = visibleIds.size();
        long frequencyTag = StreamSupport.stream(tag2PostRepository.findAll().spliterator(), false).
            filter(tag -> visibleIds.contains(tag.getPostId()) && tag.getTagId() == tagId).count();
        return Double.parseDouble(new DecimalFormat("#.##").format(frequencyTag / totalCount).replace(",", "."));
    }

    private JSONObject transformListPostToJsonObject(List<Posts> posts) {
        response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        posts.forEach(post -> jsonArray.add(transformPostToJsonObject(post)));
        response.put("count", posts.size());
        response.put("posts", jsonArray);
        return response;
    }

    private JSONObject transformPostToJsonObject(Posts post) {
        JSONObject jsonPost = new JSONObject();
        jsonPost.put("id", post.getId());
        jsonPost.put("time", formatTime(post.getTime()));
        jsonPost.put("user", getUserJson(post.getUserId()));
        jsonPost.put("title", post.getTitle());
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
        return string.substring(0, Math.min(ANNOUNCE_LENGTH, string.length()-1));
    }

    private boolean checkHasLike(int postId, int userId, int value) {
        for (PostVotes v : postVotesRepository.findAll()) {
            if (v.getUserId() == userId && v.getPostId() == postId && v.getValue() == value) {
                return true;
            }
        }
        return false;
    }

    private int getLikes(int postId, int value) {
        return (int) StreamSupport.stream(postVotesRepository.findAll().spliterator(), false)
            .filter(v -> v.getValue() == value && v.getPostId() == postId).count();
    }

    private JSONArray getComments(int postId) {
        JSONArray arrayOfComments = new JSONArray();
        for (PostComments c : postCommentsRepository.findAll()) {
            if (c.getPostId() == postId) {
                JSONObject jsonComment = new JSONObject();
                jsonComment.put("id", c.getId());
                jsonComment.put("time", formatTime(c.getTime()));
                jsonComment.put("text", c.getText());
                jsonComment.put("parent_id", c.getParentId());
                jsonComment.put("user", getUserJson(c.getUserId()));
                arrayOfComments.add(jsonComment);
            }
        }
        return arrayOfComments;
    }

    private JSONArray getTags(int id) {
        JSONArray arrayOfTags = new JSONArray();
        for (Tag2Post tag : tag2PostRepository.findAll()) {
            if (tag.getPostId() == id) {
                arrayOfTags.add(tagsRepository.findById(tag.getTagId()).get().getName());
            }
        }
        return arrayOfTags;
    }

    private JSONObject getUserJson(int id) {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("id", id);
        jsonUser.put("name", usersRepository.findById(id).get().getName());
        jsonUser.put("photo", usersRepository.findById(id).get().getPhoto());
        return jsonUser;
    }
}
