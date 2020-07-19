package main.service;

import static main.model.ModerationStatus.ACCEPTED;
import static main.model.ModerationStatus.DECLINED;
import static main.model.ModerationStatus.NEW;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import main.model.Post;
import main.model.PostComment;
import main.repositories.PostCommentsRepository;
import main.model.PostVote;
import main.repositories.PostVotesRepository;
import main.repositories.PostsRepository;
import main.model.Tag;
import main.model.Tag2Post;
import main.repositories.Tag2PostRepository;
import main.repositories.TagsRepository;
import main.model.User;
import main.repositories.UsersRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for working with posts
 */
@Service
public class PostService {

    /**
     * Strings for response to frontend
     */

    final static String NO_TITLE = "Заголовок не установлен";
    final static String SMALL_TEXT = "Текст публикации слишком короткий";
    final static String TODAY = "Сегодня, ";
    final static String YESTERDAY = "Вчера, ";
    final static String SMALL_COMMENT = "Текст комментария не задан или слишком короткий";

    /**
     * Min title length of post
     */

    final static int MIN_TITLE_LENGTH = 5;

    /**
     * Min text length of post
     */

    final static int MIN_TEXT_LENGTH = 50;

    /**
     * Length of announce for post
     */

    final static int ANNOUNCE_LENGTH = 200;

    /**
     * JSON objects for response and request
     */

    private JSONObject response, request = null;

    /**
     * parser string to JSON object
     */

    private final JSONParser parser = new JSONParser();

    /**
     * Repository for tags
     */

    @Autowired
    private TagsRepository tagsRepository;

    /**
     * Repository for posts
     */

    @Autowired
    private PostsRepository postsRepository;

    /**
     * Repository for users
     */

    @Autowired
    private UsersRepository usersRepository;

    /**
     * Repository for connections of tags and posts
     */

    @Autowired
    private Tag2PostRepository tag2PostRepository;

    /**
     * Repository for comments
     */

    @Autowired
    private PostCommentsRepository postCommentsRepository;

    /**
     * Repository for votes
     */

    @Autowired
    private PostVotesRepository postVotesRepository;

    /**
     * Getting all statistics of blog
     *
     * @param httpServletRequest using for detecting user
     * @return JSON sting with statistics
     */

    public String allStatistics(HttpServletRequest httpServletRequest) {
        response = new JSONObject();

        int postCount = postsRepository.getCountOfAllPosts();
        int viewsCount = postsRepository.getSumViewCountOfAllPosts();
        String firstPublication = new SimpleDateFormat("yyyy-MM-dd HH:mm")
            .format(postsRepository.getFirstPublicationDateOfAllPosts());
        int likesCount = postsRepository.getVotesCountOfAllUser(1);
        int dislikesCount = postsRepository.getVotesCountOfAllUser(-1);

        response.put("postsCount", postCount);
        response.put("likesCount", likesCount);
        response.put("dislikesCount", dislikesCount);
        response.put("viewsCount", viewsCount);
        response.put("firstPublication", firstPublication);

        return response.toJSONString();
    }

    /**
     * Getting statistics of user
     *
     * @param httpServletRequest using for detecting user
     * @return JSON sting with statistics
     */

    public String myStatistics(HttpServletRequest httpServletRequest) {
        if (!checkLogin(httpServletRequest.getSession())) {
            return null;
        }

        response = new JSONObject();
        int userId = getLoginUserId(httpServletRequest.getSession());
        int postCount = postsRepository.getCountOfUserPosts(userId);
        int viewsCount = postsRepository.getSumViewCountOfUserPosts(userId);
        String firstPublication = new SimpleDateFormat("yyyy-MM-dd HH:mm")
            .format(postsRepository.getFirstPublicationDateOfUserPosts(userId));
        int likesCount = postsRepository.getVotesCountOfUser(userId, 1);
        int dislikesCount = postsRepository.getVotesCountOfUser(userId, -1);

        response.put("postsCount", postCount);
        response.put("likesCount", likesCount);
        response.put("dislikesCount", dislikesCount);
        response.put("viewsCount", viewsCount);
        response.put("firstPublication", firstPublication);

        return response.toJSONString();
    }

    /**
     * Getting count of post by date and years of publication all posts
     *
     * @param year specific year
     * @return JSON string with array years and list of dates with count of posts
     */

    public String calendar(String year) {
        response = new JSONObject();
        JSONArray yearsArray = new JSONArray();
        yearsArray.addAll(postsRepository.getYearsOfPost());
        JSONObject postsCounts = new JSONObject();

        List<Date> datesOfPosts = postsRepository.getPostDates();
        List<Integer> countsOfPosts = postsRepository.getCountOfPostByDate();

        for (int i = 0; i < datesOfPosts.size(); i++) {
            postsCounts.put(new SimpleDateFormat("yyyy-MM-dd").format(datesOfPosts.get(i)), countsOfPosts.get(i));
        }

        response.put("years", yearsArray);
        response.put("posts", postsCounts);

        return response.toJSONString();
    }


    /**
     * Moderation posts
     *
     * @param body               of request
     * @param httpServletRequest using for detecting user
     * @throws ParseException if can not parse string to JSON object
     */

    public void doModeration(String body, HttpServletRequest httpServletRequest)
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

    /**
     * Edit post of user or while moderation
     *
     * @param id                 of post
     * @param body               of request
     * @param httpServletRequest using for detecting user
     * @return JSON string with result:true and post object in JSON format or result:true wiht list of errors,
     * someting wrong
     * @throws ParseException           if can not parse string to JSON object
     * @throws java.text.ParseException if can not parse sting with date to date object
     */

    public String editPost(int id, String body, HttpServletRequest httpServletRequest)
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

    /**
     * Posts of user
     *
     * @param status  @see ModerationStatus enum
     * @param offset  in list
     * @param limit   in list
     * @param request using for detecting user
     * @return JSON string with list of post and count of all visible user post
     */

    public String myPosts(String status, int offset, int limit,
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

    /**
     * Moderating the post
     *
     * @param status  @see ModerationStatus enum
     * @param offset  in list
     * @param limit   in list
     * @param request using for detecing user
     * @return JSON string with list of post for moderation and count of all visible posts for moderation
     */

    public String moderation(String status, int offset, int limit, HttpServletRequest request) {
        if (!checkLogin(request.getSession())) {
            return null;
        }

        status = status.toUpperCase();
        int userId = getLoginUserId(request.getSession());

        return transformListPostToJsonObject(postsRepository.getPostsForModeration(status, userId, offset, limit),
            postsRepository.getCountOfPostsForModeration(status, userId)).toJSONString();
    }

    /**
     * Posts by tag
     *
     * @param tag    name
     * @param offset in list
     * @param limit  in list
     * @return JSON string with list of post filtering by tag and count of all visible posts with specific tag
     */

    public String postByTag(String tag, int offset, int limit) {
        return transformListPostToJsonObject(postsRepository.getPostsByTag(tag, offset, limit),
            postsRepository.getPostsByTagCount(tag)).toJSONString();
    }

    /**
     * Posts by date
     *
     * @param date   specific date (yyyy-MM-dd)
     * @param offset in list
     * @param limit  in list
     * @return JSON string with list of post with specific date and count of all visible posts with specific date
     */

    public String postByDate(String date, int offset, int limit) {
        return transformListPostToJsonObject(postsRepository.getPostsByDate(date, offset, limit),
            postsRepository.getCountOfPostsByDate(date)).toJSONString();
    }

    /**
     * Commenting some post
     *
     * @param body        of request
     * @param httpRequest using for detecion user
     * @return JSON string with comment
     * @throws ParseException if can not parse string to JSON object
     */

    public String comment(String body, HttpServletRequest httpRequest) throws ParseException {
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

    /**
     * Dislike post
     *
     * @param body        of request
     * @param httpRequest using for detecting user
     * @return JSON sting with the vote
     * @throws ParseException if can not parse string to JSON object
     */

    public String dislikePost(String body, HttpServletRequest httpRequest) throws ParseException {
        return votePost(body, httpRequest, (byte) -1);
    }

    /**
     * Like post
     *
     * @param body        of request
     * @param httpRequest using for detecting user
     * @return JSON sting with the vote
     * @throws ParseException if can not parse string to JSON object
     */

    public String likePost(String body, HttpServletRequest httpRequest) throws ParseException {
        return votePost(body, httpRequest, (byte) 1);
    }


    /**
     * Specific post
     *
     * @param id      of the post
     * @param request using for detecting referer. if referer = edit, do not inc views count and do not transform
     *                date
     * @return JSON string with post object
     */

    public String getPost(int id, HttpServletRequest request) {
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

    /**
     * Searching in post
     *
     * @param query  for serching
     * @param offset in list
     * @param limit  list
     * @return JSON strong of list of searched post with offset and limit and total count of searched posts
     */

    public String search(String query, int offset, int limit) {
        return transformListPostToJsonObject(postsRepository.getPostsByQuery("%" + query + "%", offset, limit),
            postsRepository.getCountOfPostsByQuery("%" + query + "%")).toJSONString();
    }

    /**
     * Post in specific sorting mode
     *
     * @param mode   = new, old, popular(max comments), best(max likes)
     * @param offset in list
     * @param limit  in list
     * @return JSON sting of list of post with specific sorted mode with offset and limit, and total count of
     * visible post
     */

    public String getPosts(String mode, int offset, int limit) {
        List<Post> sortedList = mode.equals("popular") ? postsRepository.getPopularPosts(offset, limit) :
            mode.equals("best") ? postsRepository.getBestPosts(offset, limit) :
                mode.equals("early") ? postsRepository.getVisiblePostsOrderDesc(offset, limit) :
                    postsRepository.getVisiblePosts(offset, limit);

        return transformListPostToJsonObject(sortedList, postsRepository.countOfVisiblePosts()).toJSONString();
    }

    /**
     * Savin the new post
     *
     * @param body               or request
     * @param httpServletRequest using for user detecing
     * @return JSON string with post object and result:true or result:false and list with errors if something wrong
     * @throws ParseException           if can not parse string ot JSON object
     * @throws java.text.ParseException if can not parse string to date object
     */

    public String post(String body, HttpServletRequest httpServletRequest)
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

    /**
     * Tags of post
     *
     * @param request can request specific tag by query
     * @return JSON string with arrays of tags if query empty or JSON string of one tag object
     */

    public String getTags(HttpServletRequest request) {
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


    /**
     * Checing the time, must be in future or now
     *
     * @param time
     * @return max of now() and specific time
     * @throws java.text.ParseException if can not parse string to date object
     */
    private long checkTime(String time) throws java.text.ParseException {
        return Math
            .max(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time).getTime(), System.currentTimeMillis());
    }

    /**
     * Checking erros in post
     *
     * @param title must be above specific parameter
     * @param text  must be above specific parameter
     * @return JSON object with errors
     */

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

    /**
     * Voting the post
     *
     * @param body        of request
     * @param httpRequest using for detecint user
     * @param value       1 = like, -1 = dislile
     * @return JSON string result:true if voting happens of result:false if current user had voting yet this post
     * @throws ParseException
     */

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

    /**
     * Check login user
     *
     * @param session id of user
     * @return true if login, false if user do no authorized
     */

    private boolean checkLogin(HttpSession session) {
        return AuthService.sessions.containsKey(session.getId());
    }

    /**
     * Getting user id by session
     *
     * @param session id of user
     * @return user id of login user
     */
    private int getLoginUserId(HttpSession session) {
        return AuthService.sessions.get(session.getId());
    }

    /**
     * Transform date to specific string
     *
     * @param time
     * @return string in format (yyyy-MM-dd HH:mm) or Today HH:mm of Yesterday HH:mm dependency of parameter time
     */

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

    /**
     * Calculating the weight of tag
     *
     * @param tagId id of tag
     * @return weight of tag (frequencyTag / totalCount)
     */

    private double calculateTagWeight(int tagId) {
        double totalCount = postsRepository.countOfVisiblePosts();
        long frequencyTag = tag2PostRepository.getFrequencyOfTag(tagId);
        return Double.parseDouble(new DecimalFormat("#.##").format(frequencyTag / totalCount).replace(",", "."));
    }


    /**
     * Transforming list of posts to JSON object
     *
     * @param posts list of posts
     * @param size  of all posts
     * @return JSON string of total count of post in specific filtering and array of post in json format
     */

    private JSONObject transformListPostToJsonObject(List<Post> posts, int size) {
        response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        posts.forEach(post -> jsonArray.add(transformPostToJsonObject(post)));
        response.put("count", size);
        response.put("posts", jsonArray);
        return response;
    }

    /**
     * Transforming post to JSON object
     *
     * @param post object
     * @return JSON object of post
     */

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

    /**
     * Clear text from html tags
     *
     * @param string text
     * @return text without html tags
     */

    private String getPlainText(String string) {
        string = string.replaceAll("\\<[^>]*>", "").replaceAll("&nbsp;", " ");
        return string.substring(0, Math.min(ANNOUNCE_LENGTH, string.length() - 1));
    }

    /**
     * Checking voting of current user by specific post
     *
     * @param postId if of post
     * @param userId id of user
     * @param value  1 = like, -1 = disllike
     * @return true if had voted, false if do not vote
     */

    private boolean checkHasLike(int postId, int userId, int value) {
        return postVotesRepository.checkHasLike(postId, userId, value) > 0;
    }

    /**
     * Getting likes of post
     *
     * @param postId id of post
     * @param value  1 = like, -1 = dislike
     * @return count of likes of dislkes dependently parameter value
     */

    private int getLikes(int postId, int value) {
        return postVotesRepository.getPostLikes(postId, value);
    }

    /**
     * Getting coometns of post
     *
     * @param postId id of post
     * @return JSON string with array of comments for specific post
     */

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

    /**
     * Getting post tags
     *
     * @param id of post
     * @return JSON string with array of tags for specific post
     */

    private JSONArray getPostTags(int id) {
        JSONArray arrayOfTags = new JSONArray();
        arrayOfTags.addAll(tagsRepository.getPostTags(id));
        return arrayOfTags;
    }

    /**
     * Getting JSON object of user by id
     *
     * @param id of user
     * @return SON object of user by id
     */

    private JSONObject getUserJson(int id) {
        JSONObject jsonUser = new JSONObject();

        jsonUser.put("id", id);
        jsonUser.put("name", getUserById(id).getName());
        jsonUser.put("photo", getUserById(id).getPhoto());

        return jsonUser;
    }

    /**
     * Getting tag id by his name
     *
     * @param tagName name of tag
     * @return id of tag
     */

    private int getTagIdByName(String tagName) {
        return tagsRepository.getTagIdByName(tagName);
    }

    /**
     * Getting user by id
     *
     * @param id of user
     * @return user object with specific id
     */

    private User getUserById(int id) {
        return usersRepository.findById(id).get();
    }
}
