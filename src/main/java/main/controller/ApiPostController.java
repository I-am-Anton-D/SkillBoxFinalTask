package main.controller;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.servlet.http.HttpServletRequest;
import main.model.ModerationStatus;
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


    private JSONObject response, request = null;
    private JSONParser parser = new JSONParser();
    @Autowired
    private TagsRepository tagsRepository;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private Tag2PostRepository tag2PostRepository;

    @GetMapping("/api/post")
    public String getPosts(HttpServletRequest request) {
        response = new JSONObject();
        int offset = Integer.parseInt(request.getParameter("offset"));
        int limit = Integer.parseInt(request.getParameter("limit"));
        String mode = request.getParameter("mode");
        List<Posts> sortedList = getVisiblePost();
        int count = sortedList.size();

        if (mode.equals("recent")) {
            sortedList = sortedList.stream().sorted(Comparator.comparing(Posts::getTime).reversed())
                .collect(Collectors.toList());
        }

        if (mode.equals("popular")) {
            //TODO popular
        }

        if (mode.equals("best")) {
            //TODO best
        }

        if (mode.equals("early")) {
            sortedList = sortedList.stream().sorted(Comparator.comparing(Posts::getTime))
                .collect(Collectors.toList());
        }
        sortedList = sortedList.stream().skip(offset).limit(limit).collect(Collectors.toList());

        JSONArray jsonArray = new JSONArray();
        for (Posts post : sortedList) {
            JSONObject jsonPost = new JSONObject();
            jsonPost.put("id", post.getId());
            jsonPost.put("time", formatTime(post.getTime()));
            JSONObject jsonUser = new JSONObject();
            jsonUser.put("id", post.getUserId());
            jsonUser.put("name", usersRepository.findById(post.getUserId()).get().getName());
            jsonPost.put("user", jsonUser);
            jsonPost.put("title", post.getTitle());
            jsonPost.put("announce", post.getText().replaceAll("\\<[^>]*>","").replaceAll("&nbsp;"," ").substring(0,ANNOUNCE_LENGTH));
            jsonPost.put("likeCount", 0); //TODO LikeCount
            jsonPost.put("dislikeCount", 0); //TODO dislikeCount
            jsonPost.put("commentCount", 0); //TODO commentCount
            jsonPost.put("viewCount", post.getViewCount());
            jsonArray.add(jsonPost);

        }
        response.put("count", count);
        response.put("posts", jsonArray);
        return response.toJSONString();
    }

    @PostMapping("/api/post")
    public String post(@RequestBody String body, HttpServletRequest httpServletRequest)
        throws ParseException, java.text.ParseException {
        response = new JSONObject();
        request = (JSONObject) parser.parse(body);
        long createTime = new SimpleDateFormat("yyyy-MM-dd hh:mm").parse((String) request.get("time")).getTime();
        long now = System.currentTimeMillis();
        createTime = Math.max(createTime, now);
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
            post.setUserId(
                usersRepository.findById(ApiAuthController.sessions.get(httpServletRequest.getSession().getId()))
                    .get().getId());
            post.setTime(new Date(createTime));
            post.setTitle(title);
            post.setText(text);
            post.setViewCount(0);
            int postId = postsRepository.save(post).getId();
            int tagId = -1;
            for (int i = 0; i < tags.size(); i++) {
                Iterable<Tags> savedTags = tagsRepository.findAll();
                for (Tags tag : savedTags) {
                    if (tag.getName().equals(tags.get(i))) {
                        tagId = tag.getId();
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
        Set<Integer> visibleIds = new HashSet<>();
        for (Posts post: getVisiblePost()) {
            visibleIds.add(post.getId());
        }
        Iterable<Tags> tags = tagsRepository.findAll();
        JSONArray tagsArray = new JSONArray();
        for (Tags tag : tags) {
            JSONObject tagObject = new JSONObject();
            tagObject.put("name", tag.getName());
            tagObject.put("weight", calculateTagWeight(tag.getId(),visibleIds));
            tagsArray.add(tagObject);
        }

        if (request.getParameter("query") == null) {
            response.put("tags", tagsArray);
        } else {
            String query = request.getParameter("query");
            for (int i = 0; i <tagsArray.size(); i++) {
                JSONObject jsonTag = (JSONObject) tagsArray.get(i);
                if (query.equals(jsonTag.get("name"))) {
                    response.put("name", jsonTag.get("name"));
                    response.put("weight", jsonTag.get("weight"));
                }
            }
        }
        return response.toJSONString();
    }

    public String formatTime(Date time) {
        Calendar now = Calendar.getInstance();
        Calendar postTime = Calendar.getInstance();
        postTime.setTime(time);
        if (now.get(Calendar.YEAR) == postTime.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == postTime
            .get(Calendar.DAY_OF_YEAR)) {
            return TODAY + new SimpleDateFormat("HH:mm").format(time);
        } else if (now.get(Calendar.YEAR) == postTime.get(Calendar.YEAR)
            && now.get(Calendar.DAY_OF_YEAR) - postTime
            .get(Calendar.DAY_OF_YEAR) == 1) {
            return YESTERDAY + new SimpleDateFormat("HH:mm").format(time);
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
    }

    public List<Posts> getVisiblePost() {
        //TODO Fix Moderation status
        Iterable<Posts> posts = postsRepository.findAll();
        return StreamSupport.stream(posts.spliterator(), false)
            .filter(p -> p.getIsActive() == 1 && p.getModerationStatus() == ModerationStatus.NEW
                && p.getTime().getTime() <= System.currentTimeMillis())
            .collect(Collectors.toList());
    }

    public double calculateTagWeight(int tagId, Set<Integer> visibleIds) {
        double totalCount = visibleIds.size();
        int frequencyTag = 0;
        Iterable<Tag2Post> tag2Posts = tag2PostRepository.findAll();
        for (Tag2Post tag: tag2Posts) {
            if (visibleIds.contains(tag.getPostId()) && tag.getTagId() == tagId) {
                frequencyTag++;
            }
        }
        double weight = frequencyTag/totalCount;
        return Double.parseDouble(new DecimalFormat("#.##").format(weight).replace(",","."));
    }
}
