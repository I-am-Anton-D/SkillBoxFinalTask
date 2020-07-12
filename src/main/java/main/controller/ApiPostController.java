package main.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;
import main.model.ModerationStatus;
import main.model.Posts;
import main.model.PostsRepository;
import main.model.Tag2Post;
import main.model.Tag2PostRepository;
import main.model.Tags;
import main.model.TagsRepository;
import main.model.Users;
import main.model.UsersRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiPostController {
    final static String NO_TITLE = "Заголовок не установлен";
    final static String SMALL_TEXT = "Текст публикации слишком короткий";


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

        return response.toJSONString();
    }

    @PostMapping("/api/post")
    public String post(@RequestBody String body, HttpServletRequest httpServletRequest) throws ParseException, java.text.ParseException {
        response = new JSONObject();
        request = (JSONObject) parser.parse(body);
        long createTime = new SimpleDateFormat("yyyy-MM-dd hh:mm").parse((String)request.get("time")).getTime();
        long now = System.currentTimeMillis();
        createTime = Math.max(createTime, now);
        long active = (long)request.get("active");
        String title = (String)request.get("title");
        JSONArray tags = (JSONArray) request.get("tags");
        String text = (String)request.get("text");
        JSONObject errors = new JSONObject();

        if (title.length()<4) {
            errors.put("title", NO_TITLE );
        }
        if (text.length()<50) {
            errors.put("text",SMALL_TEXT );
        }
        if (errors.size()!=0) {
            response.put("result",false);
            response.put("errors",errors);
        } else {
            Posts post = new Posts();
            post.setIsActive((byte) active);
            post.setModerationStatus(ModerationStatus.NEW);
            post.setUserId(usersRepository.findById(ApiAuthController.sessions.get(httpServletRequest.getSession().getId())).get().getId());
            post.setTime(new Date(createTime));
            post.setTitle(title);
            post.setText(text);
            post.setViewCount(0);
            int postId = postsRepository.save(post).getId();
            int tagId = -1;
            for (int i = 0; i <tags.size(); i++) {
                Iterable<Tags> savedTags = tagsRepository.findAll();
                for (Tags tag: savedTags) {
                    if (tag.getName().equals(tags.get(i))) {
                        tagId = tag.getId();
                    }
                }
                tag2PostRepository.save(new Tag2Post(postId, tagId));
            }
            response.put("result",true);
        }
        return response.toJSONString();
    }

    @GetMapping("/api/tag")
    public String tag(HttpServletRequest request){
        response = new JSONObject();
        if (request.getParameter("query")==null) {
            Iterable<Tags> tags = tagsRepository.findAll();
            JSONArray tagsArray = new JSONArray();
            for (Tags tag:tags) {
                JSONObject tagObject = new JSONObject();
                tagObject.put("name", tag.getName());
                tagObject.put("weight", 0);  //TODO Weight calculate
                tagsArray.add(tagObject);
            }
            response.put("tags",tagsArray);
        }
        else {
            //TODO Query
        }
        return response.toJSONString();
    }
}
